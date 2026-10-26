package com.halvermoor.postings;

public class AmountUtil {

    public static double roundDown5(double amount) {
        return Math.floor(amount / 5) * 5;
    }

    public static double round2(double amount) {
        return Math.round(amount * 100) / 100.0;
    }

    // formats with two decimals and a space every three digits
    public static String format(double amount) {
        long cents = (long) (amount * 100);
        String units = String.valueOf(cents / 100);
        String out = "";
        for (int i = units.length() - 1, c = 1; i >= 0; i--, c++) {
            out = units.charAt(i) + out;
            if (c % 3 == 0 && i > 0) out = " " + out;
        }
        return out + "." + (cents % 100 < 10 ? "0" : "") + cents % 100;
    }
}
