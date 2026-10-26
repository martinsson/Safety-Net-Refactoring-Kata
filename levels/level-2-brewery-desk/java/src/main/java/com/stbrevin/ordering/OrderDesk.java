package com.stbrevin.ordering;

import com.tallowdb.Database;
import com.tallowdb.Row;
import com.tallowdb.TallowDb;
import io.ferrow.connect.Charge;
import io.ferrow.connect.Connect;
import io.ferrow.connect.CourierClient;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OrderDesk {

    static int[][] FEASTS = {{1, 6}, {8, 15}, {10, 3}, {11, 1}, {12, 25}};

    // bottles kept back for the cafes
    static final int RESERVE = 300;

    Database db = TallowDb.open("cellar");
    Cellar cellar = new Cellar();
    CourierClient courier = Connect.courier();

    public Confirmation process(String customerCode, int zone, LocalDate deliveryDate, String... lines) {
        Confirmation c = new Confirmation();
        String number = "SB" + LocalDate.now().getYear() + "-" + String.format("%04d", db.nextValue("order_no"));
        String code = customerCode == null ? "" : customerCode;
        Row customer = findCustomer(code);
        String type = "PRIVATE";
        if (customer != null) {
            String t = customer.getString("type");
            if ("CAFE".equalsIgnoreCase(t) || "SHOP".equalsIgnoreCase(t)) type = t.toUpperCase();
        }
        LocalDate d = c.deliveryDate = nextOpenDay(deliveryDate, zone);
        String key = type + "/" + d.getYear() + "-" + d.getMonthValue();
        int quota = type.equals("CAFE") ? 1500 : 1000;
        if (type.equals("CAFE") && d.getMonthValue() == 12) quota = 2000;
        if (type.equals("PRIVATE")) {
            key = code + "/" + d.getYear() + "-" + d.getMonthValue();
            quota = 48;
        }
        boolean oldDeal = code.equals("CAFE-0031");
        boolean winter = d.getMonthValue() >= 11 || d.getMonthValue() == 1;
        double goods = 0, disc = 0, deposit = 0, credit = 0, crateGoods = 0;
        ArrayList kinds = new ArrayList();
        int kegs = 0, crates = 0, bots = 0, shortLines = 0;
        for (int i = 0; i < lines.length; i++) {
            String[] l = lines[i].trim().split(" +");
            int qty = Integer.parseInt(l[0]);
            String unit = l[1].toUpperCase(), product = l[2];
            if (unit.equals("CASE")) unit = "CRATE"; // old order forms
            if (unit.equals("RETURN")) {
                String what = product.toUpperCase();
                double back = 0;
                if (qty <= 0) continue;
                if (what.equals("BOTTLE")) back = qty * 0.10;
                else if (what.equals("CRATE")) back = qty * (3.00 + 24 * 0.10);
                else if (what.equals("KEG")) back = qty * 30.0;
                else continue;
                credit += back;
                c.lines.add("RETURN " + qty + " x " + what.toLowerCase() + " -" + fmt(back));
                continue;
            }
            double price = bottlePrice(product);
            if (price < 0 || qty <= 0) continue;
            if ("BOTTLE".equals(unit)) {
                int left = cellar.bottlesLeft(product);
                if (!type.equals("CAFE")) left = left - RESERVE;
                if (product.equals("NOX") && !winter && !code.startsWith("BRO-")) left = 0;
                int avail = Math.min(left, quota - cellar.used(key));
                int n = Math.min(qty, Math.max(avail, 0));
                if (n > 0) {
                    double amount = round2(n * price);
                    double tier = n >= 120 ? 0.08 : n >= 48 ? 0.05 : 0;
                    if (code.startsWith("GUEST-") && tier < 0.10) tier = 0.10;
                    goods += amount;
                    disc += amount * tier;
                    deposit += n * (product.equals("NOX") ? 0.15 : 0.10);
                    bots += n;
                    cellar.takeBottles(product, n);
                    cellar.addUsed(key, n);
                    saveLine(number, n, "BOTTLE", product, amount);
                    c.lines.add(n + " x " + product + " bottle " + fmt(amount));
                }
                if (n < qty) {
                    backorder(code, qty - n, "BOTTLE", product, d);
                    c.lines.add("BACKORDER " + (qty - n) + " x " + product + " bottle");
                    shortLines++;
                }
            } else if ("CRATE".equals(unit)) {
                int left = cellar.bottlesLeft(product);
                if (type.equals("CAFE") == false) left -= RESERVE;
                if (product.equals("NOX") && (d.getMonthValue() > 1 && d.getMonthValue() < 11)) left = 0;
                int avail = Math.min(left, quota - cellar.used(key)) / 24;
                int n = Math.min(qty, Math.max(avail, 0));
                if (n > 0) {
                    double factor = d.getYear() < 2012 ? 0.94 : 0.92;
                    double amount = round2(n * 24 * price * factor);
                    double tier = n >= 25 ? 0.07 : n > 10 ? 0.04 : 0;
                    goods += amount;
                    disc += amount * tier;
                    deposit += n * (3.00 + 24 * 0.10);
                    crates += n;
                    crateGoods += amount;
                    if (!kinds.contains(product)) kinds.add(product);
                    cellar.takeBottles(product, n * 24);
                    cellar.addUsed(key, n * 24);
                    saveLine(number, n, "CRATE", product, amount);
                    c.lines.add(n + " x " + product + " crate " + fmt(amount));
                }
                if (n < qty) {
                    backorder(code, qty - n, "CRATE", product, d);
                    c.lines.add("BACKORDER " + (qty - n) + " x " + product + " crate");
                    shortLines++;
                }
            } else if ("KEG".equals(unit)) {
                double kp = kegPrice(product);
                if (oldDeal && product.equals("DUB")) kp = 99.0;
                int avail = cellar.kegsLeft(product);
                if (!oldDeal && (quota - cellar.used(key)) / 60 < avail) avail = (quota - cellar.used(key)) / 60;
                int n = Math.min(qty, Math.max(avail, 0));
                if (type.equals("CAFE") && kegs + n > 8) n = 8 - kegs;
                if (n > 0) {
                    goods += n * kp;
                    if (n >= 5) disc += n * kp * 0.03;
                    deposit += n * 30.0;
                    kegs += n;
                    cellar.takeKegs(product, n);
                    if (!oldDeal) cellar.addUsed(key, n * 60);
                    saveLine(number, n, "KEG", product, n * kp);
                    c.lines.add(n + " x " + product + " keg " + fmt(n * kp));
                }
                if (n < qty) {
                    backorder(code, qty - n, "KEG", product, d);
                    c.lines.add("BACKORDER " + (qty - n) + " x " + product + " keg");
                    shortLines++;
                }
            }
        }
        if (kinds.size() >= 3) disc += crateGoods * 0.02;
        if (zone <= 0 && !type.equals("PRIVATE")) disc += goods * 0.02;
        // since 2009
        if (code.startsWith("BRO-") && LocalDate.now().getYear() > 2008) disc = Math.min(goods * 0.15, 40);
        disc = round2(disc);
        double net = goods - disc;
        double ship = 0;
        if (zone > 0 && goods > 0) ship = shipping(zone, bots + crates * 16 + kegs * 30);
        if (zone >= 3 && net < 60) ship += 6.00;
        if (zone < 4 && net >= (type.equals("PRIVATE") ? 120 : 250)) ship = 0;
        // ticket 3318: village shop
        if (code.equals("SH-0007") && zone <= 2) ship = 0;
        if (oldDeal) ship = 4.50;
        if (goods == 0) ship = 0;
        if (type.equals("PRIVATE") && credit > 30) credit = 30;
        double rate = type.equals("PRIVATE") && !code.startsWith("BRO-") ? 0.21 : 0.06;
        c.goods = round2(goods);
        c.discount = disc;
        c.deposit = round2(deposit - credit);
        c.empties = credit;
        c.shipping = ship;
        c.tax = round2((net + ship) * rate);
        c.total = round2(net + ship + c.tax + c.deposit);
        String account = customer == null ? null : customer.getString("account");
        if (code.startsWith("BRO-")) c.terms = "at the abbey";
        else if (account != null) c.terms = "charged to account";
        else if (type.equals("PRIVATE")) c.terms = "on delivery";
        else if (type.equals("CAFE")) c.terms = "30 days";
        else if (goods >= 1000) c.terms = "30 days";
        else c.terms = "45 days";
        if (c.terms.equals("on delivery")) c.total = Math.round(c.total * 20) / 20.0;
        if (!d.equals(deliveryDate)) c.note = "moved from " + deliveryDate;
        if (goods == 0) {
            c.status = 2;
            return c;
        }
        c.status = shortLines > 0 ? 1 : 0;
        c.number = number;
        HashMap order = new HashMap();
        order.put("number", number);
        order.put("customer", code);
        order.put("delivery", d);
        order.put("status", c.status);
        order.put("total", Math.round(c.total * 100));
        order.put("terms", c.terms);
        if (c.terms.equals("charged to account") && c.total > 0) {
            Charge charge = Connect.payments().charge(account, Math.round(c.total * 100), number);
            order.put("charge", charge.id());
        }
        if (zone > 0) {
            c.tracking = courier.book(number, zone, d, crates + kegs + (bots + 11) / 12).trackingId();
            order.put("tracking", c.tracking);
        }
        db.table("orders").insert(order);
        if (customer != null && customer.getString("email") != null) {
            Connect.mail().send(customer.getString("email"), "Order " + number, c.getText());
        }
        return c;
    }

    static Row findCustomer(String code) {
        List<Row> rows = TallowDb.open("cellar").table("customers").find("code", code);
        return rows.isEmpty() ? null : rows.get(0);
    }

    void saveLine(String number, int n, String unit, String product, double amount) {
        HashMap row = new HashMap();
        row.put("number", number);
        row.put("qty", n);
        row.put("unit", unit);
        row.put("beer", product);
        row.put("amount", Math.round(amount * 100));
        TallowDb.open("cellar").table("order_lines").insert(row);
    }

    void backorder(String code, int qty, String unit, String product, LocalDate d) {
        HashMap row = new HashMap();
        row.put("customer", code);
        row.put("qty", qty);
        row.put("unit", unit);
        row.put("beer", product);
        row.put("wanted", d);
        TallowDb.open("cellar").table("backorders").insert(row);
    }

    static double bottlePrice(String product) {
        List<Row> rows = TallowDb.open("cellar").table("beers").find("beer", product);
        if (rows.isEmpty()) return -1;
        return rows.get(0).getLong("bottle_price") / 100.0;
    }

    // price per keg (20 l)
    static double kegPrice(String product) {
        List<Row> rows = TallowDb.open("cellar").table("beers").find("beer", product);
        if (rows.isEmpty() || !rows.get(0).has("keg_price")) return 0;
        return rows.get(0).getLong("keg_price") / 100.0;
    }

    // the courier takes 1000 kg at most per booking
    static double shipping(int zone, int kilos) {
        if (kilos > 1000) kilos = 1000;
        return Connect.courier().quote(zone, kilos) / 100.0;
    }

    static double round2(double d) {
        return Math.round(d * 100) / 100.0;
    }

    static String fmt(double d) {
        return String.format(java.util.Locale.ROOT, "%.2f", d);
    }

    static LocalDate nextOpenDay(LocalDate d, int zone) {
        LocalDate e = easter(d.getYear());
        for (int i = 0; i < 2; i++) {
            if (zone >= 3 && d.getDayOfWeek() == DayOfWeek.SATURDAY) d = d.plusDays(2);
            if (zone >= 4) {
                while (d.getDayOfWeek() != DayOfWeek.TUESDAY && d.getDayOfWeek() != DayOfWeek.THURSDAY) d = d.plusDays(1);
            }
            if (d.getDayOfWeek() == DayOfWeek.SUNDAY) d = d.plusDays(1);
            for (int j = 0; j < FEASTS.length; j++) {
                if (FEASTS[j][0] == d.getMonthValue() && FEASTS[j][1] == d.getDayOfMonth()) d = d.plusDays(1);
            }
            if (d.equals(e.plusDays(1)) || d.equals(e.plusDays(39))) d = d.plusDays(1);
        }
        return d;
    }

    static LocalDate easter(int y) {
        int a = y % 19, b = y / 100, c = y % 100;
        int d = b / 4, e = b % 4, f = (b + 8) / 25, g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4, k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int n = h + l - 7 * m + 114;
        return LocalDate.of(y, n / 31, n % 31 + 1);
    }
}
