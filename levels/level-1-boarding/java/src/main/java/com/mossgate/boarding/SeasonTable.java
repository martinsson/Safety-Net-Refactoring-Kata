package com.mossgate.boarding;

import java.util.Calendar;
import java.util.HashMap;

public class SeasonTable {

    public static HashMap<Integer, Double> CURRENT = new HashMap<Integer, Double>();

    static {
        CURRENT.put(Calendar.JULY, 1.25);
        CURRENT.put(Calendar.AUGUST, 1.25);
    }

    // month is 0 based
    public static double factor(int month, int day) {
        if (month == Calendar.DECEMBER && day >= 20)
            return 1.3;
        Double f = CURRENT.get(month);
        return f == null ? 1.0 : f.doubleValue();
    }
}
