package com.mossgate.boarding;

import java.util.HashMap;

public class LoyaltyRegistry {

    private static LoyaltyRegistry instance;

    private HashMap points = new HashMap();

    private LoyaltyRegistry() {
        reset();
    }

    public static synchronized LoyaltyRegistry getInstance() {
        if (instance == null) {
            instance = new LoyaltyRegistry();
        }
        return instance;
    }

    public int points(String card) {
        Integer p = (Integer) points.get(card);
        return p == null ? 0 : p.intValue();
    }

    public void add(String card, int n) {
        points.put(card, points(card) + n);
    }

    // cards carried over from the paper system
    public void reset() {
        points.clear();
        points.put("G-0042", 250);
        points.put("L-1187", 100);
        points.put("L-2203", 95);
    }
}
