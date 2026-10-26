package com.stbrevin.ordering;

import com.tallowdb.Database;
import com.tallowdb.Row;
import com.tallowdb.StoreException;
import com.tallowdb.Table;
import com.tallowdb.TallowDb;
import io.ferrow.connect.Connect;
import io.ferrow.connect.CourierClient;
import io.ferrow.connect.DeclinedException;
import io.ferrow.connect.MailClient;
import io.ferrow.connect.PaymentClient;
import io.ferrow.connect.RemoteException;
import io.ferrow.connect.TimeoutException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Checkout {

    static int[][] FEASTS = {{1, 6}, {8, 15}, {10, 3}, {11, 1}, {12, 25}};

    // the platform is slow on Monday mornings, try once more (ticket 5120)
    static boolean RETRY = true;

    Database db;
    PaymentClient payments;
    CourierClient courier;
    MailClient mail;

    public Checkout() {
        db = TallowDb.open("cellar");
        payments = Connect.payments();
        courier = Connect.courier();
        mail = Connect.mail();
    }

    public Receipt place(String customerCode, LocalDate deliveryDate, String... lines) {
        Receipt r = new Receipt();
        r.deliveryDate = deliveryDate;
        List<Row> found = db.table("customers").find("code", customerCode);
        if (found.isEmpty()) {
            r.status = "REFUSED";
            r.message = "unknown customer";
            return r;
        }
        Row customer = found.get(0);
        String type = customer.getString("type");
        if (!"CAFE".equals(type) && !"SHOP".equals(type)) type = "PRIVATE";
        int zone = customer.getInt("zone");
        LocalDate d = r.deliveryDate = nextOpenDay(deliveryDate);
        if (!d.equals(deliveryDate)) r.note = "moved from " + deliveryDate;
        String month = d.getYear() + "-" + (d.getMonthValue() < 10 ? "0" : "") + d.getMonthValue();
        String key = type + "/" + month;
        int quota = type.equals("CAFE") ? 1500 : 1000;
        if (type.equals("PRIVATE")) {
            key = customerCode + "/" + month;
            quota = 120;
        }
        boolean winter = d.getMonthValue() >= 11 || d.getMonthValue() == 1;
        Table stock = db.table("stock");
        double goods = 0, disc = 0, deposit = 0, credit = 0;
        int bottles = 0, crates = 0, kegs = 0, allocated = 0;
        ArrayList taken = new ArrayList();
        for (int i = 0; i < lines.length; i++) {
            String[] l = lines[i].trim().split(" +");
            int qty = Integer.parseInt(l[0]);
            String unit = l[1].toUpperCase(), beer = l[2];
            if (unit.equals("CASE")) unit = "CRATE"; // old order forms
            if (unit.equals("RETURN")) {
                String what = beer.toUpperCase();
                if (qty <= 0) continue;
                if (what.equals("CRATE")) credit += qty * 5.40;
                else if (what.equals("KEG")) credit += qty * 30.0;
                else continue;
                r.lines.add("RETURN " + qty + " x " + what.toLowerCase());
                continue;
            }
            double price = bottlePrice(beer);
            int size = unit.equals("BOTTLE") ? 1 : unit.equals("CRATE") ? 24 : unit.equals("KEG") ? 60 : 0;
            if (price < 0 || qty <= 0 || size == 0) continue;
            List<Row> rows = stock.find("beer", beer);
            Row s = rows.isEmpty() ? null : rows.get(0);
            String column = size == 60 ? "kegs" : "bottles";
            int left = s == null ? 0 : s.getInt(column) / (size == 60 ? 1 : size);
            if (beer.equals("NOX") && !winter) left = 0;
            int avail = Math.min(left, (quota - used(key)) / size);
            int n = Math.min(qty, Math.max(avail, 0));
            if (n > 0) {
                int units = size == 60 ? n : n * size;
                stock.update(s.id(), Map.of(column, s.getLong(column) - units));
                taken.add(new Object[]{s.id(), column, units});
                try {
                    allocate(key, n * size);
                    allocated += n * size;
                } catch (StoreException e) {
                    // the monthly report recounts from the orders anyway (ticket 2214)
                }
                double amount;
                if (size == 1) {
                    amount = round2(n * price);
                    if (n >= 48) disc += amount * 0.05;
                    deposit += n * 0.10;
                    bottles += n;
                } else if (size == 24) {
                    amount = round2(n * 24 * price * 0.92);
                    if (n > 10) disc += amount * 0.04;
                    deposit += n * 5.40;
                    crates += n;
                } else {
                    amount = n * kegPrice(beer);
                    if (n >= 5) disc += amount * 0.03;
                    deposit += n * 30.0;
                    kegs += n;
                }
                goods += amount;
                r.lines.add(n + " x " + beer + " " + unit.toLowerCase() + " " + fmt(amount));
            }
            if (n < qty) r.lines.add("BACKORDER " + (qty - n) + " x " + beer + " " + unit.toLowerCase());
        }
        if (goods == 0) {
            r.status = "REFUSED";
            r.message = "nothing available";
            return r;
        }
        if (type.equals("PRIVATE") && credit > 30) credit = 30;
        disc = round2(disc);
        double net = goods - disc;
        double ship = 0;
        if (zone > 0 && (type.equals("PRIVATE") || net < 250)) {
            int kilos = (bottles + 1) / 2 + crates * 16 + kegs * 30;
            try {
                ship = courier.quote(zone, Math.min(kilos, 1000)) / 100.0;
            } catch (RemoteException e) {
                ship = 4.00 + zone * 5.00; // tariff of 2019, until the courier answers again
            }
        }
        double rate = type.equals("PRIVATE") ? 0.21 : 0.06;
        r.goods = round2(goods);
        r.discount = disc;
        r.deposit = round2(deposit - credit);
        r.shipping = ship;
        r.tax = round2((net + ship) * rate);
        r.total = round2(net + ship + r.tax + r.deposit);
        long cents = Math.round(r.total * 100);
        String number = "SB-" + String.format("%05d", db.nextValue("order_no"));
        String email = customer.getString("email");
        Table orders = db.table("orders");
        HashMap row = new HashMap();
        row.put("number", number);
        row.put("customer", customerCode);
        row.put("delivery", d);
        row.put("total", cents);
        row.put("status", "NEW");
        long id;
        try {
            id = orders.insert(row);
        } catch (StoreException e) {
            // bug 3907: put the beer back, there is no order to deliver
            for (Object o : taken) {
                Object[] t = (Object[]) o;
                Row s = stock.get((Long) t[0]).get();
                stock.update(s.id(), Map.of((String) t[1], s.getLong((String) t[1]) + (Integer) t[2]));
            }
            return notPlaced(r, "ERROR", "order not saved");
        }
        String status = "PLACED";
        String charge = null;
        if ("ORDER".equals(customer.getString("terms")) && cents > 0) {
            try {
                charge = charge(customer.getString("account"), cents, number);
            } catch (DeclinedException e) {
                // ticket 6031: no more ghost orders when the bank says no
                for (Object o : taken) {
                    Object[] t = (Object[]) o;
                    Row s = stock.get((Long) t[0]).get();
                    stock.update(s.id(), Map.of((String) t[1], s.getLong((String) t[1]) + (Integer) t[2]));
                }
                if (allocated > 0) allocate(key, -allocated);
                orders.delete(id);
                try {
                    mail.send(email, "Order not placed", "We could not place your order for delivery on " + d
                            + ".\nPlease contact the abbey office.\n");
                } catch (RemoteException ex) {
                    // nothing more we can do
                }
                return notPlaced(r, "DECLINED", "payment declined");
            } catch (RemoteException e) {
                status = "TO_CHARGE"; // the evening run takes the payment
            }
        }
        String tracking = null;
        if (zone > 0) {
            int parcels = crates + kegs + (bottles + 11) / 12;
            try {
                tracking = courier.book(number, zone, d, parcels).trackingId();
            } catch (RemoteException e) {
                status = "TO_BOOK"; // Brother Anselm books it by phone
            }
        }
        HashMap done = new HashMap();
        done.put("status", status);
        done.put("charge", charge);
        done.put("tracking", tracking);
        try {
            orders.update(id, done);
        } catch (StoreException e) {
            // never keep the money of an order we lost
            if (charge != null) {
                try {
                    payments.refund(charge);
                } catch (Exception ignored) {
                }
            }
            throw e;
        }
        r.number = number;
        r.status = "PLACED";
        if (status.equals("TO_CHARGE")) r.message = "payment pending";
        if (status.equals("TO_BOOK")) r.message = "delivery to be arranged";
        String body = "Order " + number + " for delivery on " + d + "\n\n";
        for (Object line : r.lines) body += line + "\n";
        body += "\nTotal " + fmt(r.total) + "\n";
        if (charge != null) body += "Paid, payment " + charge + "\n";
        else if (status.equals("TO_CHARGE")) body += "Your payment will be taken shortly\n";
        else if ("ORDER".equals(customer.getString("terms"))) body += "Nothing to pay\n";
        else body += "Invoice to follow\n";
        if (tracking != null) body += "Courier tracking " + tracking + "\n";
        else if (zone > 0) body += "The abbey will call you to arrange the delivery\n";
        else body += "Collect your order at the abbey\n";
        mail.send(email, "Order " + number, body);
        return r;
    }

    String charge(String account, long cents, String reference) {
        try {
            return payments.charge(account, cents, reference).id();
        } catch (TimeoutException e) {
            if (!RETRY) throw e;
            return payments.charge(account, cents, reference).id();
        }
    }

    void allocate(String key, int n) {
        Table t = db.table("allocations");
        List<Row> rows = t.find("key", key);
        if (rows.isEmpty()) t.insert(Map.of("key", key, "used", n));
        else t.update(rows.get(0).id(), Map.of("used", rows.get(0).getLong("used") + n));
    }

    int used(String key) {
        List<Row> rows = db.table("allocations").find("key", key);
        return rows.isEmpty() ? 0 : rows.get(0).getInt("used");
    }

    static Receipt notPlaced(Receipt r, String status, String message) {
        r.status = status;
        r.message = message;
        r.lines.clear();
        r.goods = r.discount = r.deposit = r.shipping = r.tax = r.total = 0;
        return r;
    }

    static double bottlePrice(String beer) {
        if ("TRI".equals(beer)) return 2.40;
        if ("DUB".equals(beer)) return 2.10;
        if ("BLO".equals(beer)) return 1.90;
        if ("NOX".equals(beer)) return 3.20;
        return -1;
    }

    // price per keg (20 l)
    static double kegPrice(String beer) {
        if ("TRI".equals(beer)) return 145.0;
        if ("DUB".equals(beer)) return 120.0;
        if ("BLO".equals(beer)) return 110.0;
        return 0;
    }

    static double round2(double d) {
        return Math.round(d * 100) / 100.0;
    }

    static String fmt(double d) {
        return String.format(java.util.Locale.ROOT, "%.2f", d);
    }

    static LocalDate nextOpenDay(LocalDate d) {
        for (int i = 0; i < 2; i++) {
            if (d.getDayOfWeek() == DayOfWeek.SUNDAY) d = d.plusDays(1);
            for (int j = 0; j < FEASTS.length; j++) {
                if (FEASTS[j][0] == d.getMonthValue() && FEASTS[j][1] == d.getDayOfMonth()) d = d.plusDays(1);
            }
        }
        return d;
    }
}
