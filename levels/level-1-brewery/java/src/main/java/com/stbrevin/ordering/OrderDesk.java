package com.stbrevin.ordering;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;

public class OrderDesk {

    static int[][] FEASTS = {{1, 6}, {8, 15}, {10, 3}, {11, 1}, {12, 25}};

    // bottles kept back for the cafes
    static final int RESERVE = 300;

    // old rate, still used by the yearly report
    static final double OLD_RATE = 0.195;

    StockRegistry registry = StockRegistry.getInstance();

    public Confirmation process(String customerCode, String customerType, int zone, LocalDate deliveryDate, String... lines) {
        Confirmation c = new Confirmation();
        String type = "PRIVATE";
        if ("CAFE".equalsIgnoreCase(customerType) || "SHOP".equalsIgnoreCase(customerType)) type = customerType.toUpperCase();
        String code = customerCode == null ? "" : customerCode;
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
        int kegs = 0, crates = 0, shortLines = 0;
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
                int left = registry.bottlesLeft(product);
                if (!type.equals("CAFE")) left = left - RESERVE;
                if (product.equals("NOX") && !winter && !code.startsWith("BRO-")) left = 0;
                int avail = Math.min(left, quota - registry.quotaUsed(key));
                int n = Math.min(qty, Math.max(avail, 0));
                if (n > 0) {
                    double amount = round2(n * price);
                    double tier = n >= 120 ? 0.08 : n >= 48 ? 0.05 : 0;
                    if (code.startsWith("GUEST-") && tier < 0.10) tier = 0.10;
                    goods += amount;
                    disc += amount * tier;
                    deposit += n * (product.equals("NOX") ? 0.15 : 0.10);
                    registry.takeBottles(product, n);
                    registry.addQuota(key, n);
                    c.lines.add(n + " x " + product + " bottle " + fmt(amount));
                }
                if (n < qty) {
                    c.lines.add("BACKORDER " + (qty - n) + " x " + product + " bottle");
                    shortLines++;
                }
            } else if ("CRATE".equals(unit)) {
                int left = registry.bottlesLeft(product);
                if (type.equals("CAFE") == false) left -= RESERVE;
                if (product.equals("NOX") && (d.getMonthValue() > 1 && d.getMonthValue() < 11)) left = 0;
                int avail = Math.min(left, quota - registry.quotaUsed(key)) / 24;
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
                    registry.takeBottles(product, n * 24);
                    registry.addQuota(key, n * 24);
                    c.lines.add(n + " x " + product + " crate " + fmt(amount));
                }
                if (n < qty) {
                    c.lines.add("BACKORDER " + (qty - n) + " x " + product + " crate");
                    shortLines++;
                }
            } else if ("KEG".equals(unit)) {
                double kp = kegPrice(product);
                if (oldDeal && product.equals("DUB")) kp = 99.0;
                int avail = registry.kegsLeft(product);
                if (!oldDeal && (quota - registry.quotaUsed(key)) / 60 < avail) avail = (quota - registry.quotaUsed(key)) / 60;
                int n = Math.min(qty, Math.max(avail, 0));
                if (type.equals("CAFE") && kegs + n > 8) n = 8 - kegs;
                if (n > 0) {
                    goods += n * kp;
                    if (n >= 5) disc += n * kp * 0.03;
                    deposit += n * 30.0;
                    kegs += n;
                    registry.takeKegs(product, n);
                    if (!oldDeal) registry.addQuota(key, n * 60);
                    c.lines.add(n + " x " + product + " keg " + fmt(n * kp));
                }
                if (n < qty) {
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
        double ship = 15.00 + kegs * 6.0 + crates * 0.50;
        if (zone <= 0) ship = 0;
        else if (zone == 1) ship = 4.50;
        else if (zone == 2) ship = 9.00 + kegs * 5.0;
        else if (zone == 3) ship = 15.00 + kegs * 5.0;
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
        if (code.startsWith("BRO-")) c.terms = "at the abbey";
        else if (type.equals("PRIVATE")) c.terms = "on delivery";
        else if (type.equals("CAFE")) c.terms = "30 days";
        else if (goods >= 1000) c.terms = "30 days";
        else c.terms = "45 days";
        if (c.terms.equals("on delivery")) c.total = Math.round(c.total * 20) / 20.0;
        if (!d.equals(deliveryDate)) c.note = "moved from " + deliveryDate;
        if (goods == 0) {
            c.status = 2;
        } else {
            c.status = shortLines > 0 ? 1 : 0;
            c.number = "SB" + LocalDate.now().getYear() + "-" + String.format("%04d", registry.nextNumber());
        }
        return c;
    }

    static double bottlePrice(String product) {
        if ("TRI".equals(product)) return 2.40;
        if ("DUB".equals(product)) return 2.10;
        if ("BLO".equals(product)) return 1.90;
        if ("NOX".equals(product)) return 3.20;
        return -1;
    }

    // price per keg (20 l)
    static double kegPrice(String product) {
        if ("TRI".equals(product)) return 145.0;
        if ("DUB".equals(product)) return 120.0;
        if ("BLO".equals(product)) return 110.0;
        return 0;
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
