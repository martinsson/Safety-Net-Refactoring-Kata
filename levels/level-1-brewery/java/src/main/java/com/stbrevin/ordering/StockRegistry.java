package com.stbrevin.ordering;

import java.util.HashMap;

public class StockRegistry {

    private static StockRegistry instance;

    private HashMap bottles = new HashMap();
    private HashMap kegs = new HashMap();
    private HashMap used = new HashMap();
    private int counter;

    public static StockRegistry getInstance() {
        if (instance == null) {
            instance = new StockRegistry();
            instance.load();
        }
        return instance;
    }

    void load() {
        String[] p = {"TRI", "DUB", "BLO", "NOX"};
        int[] b = {6000, 8000, 10000, 900};
        int[] k = {30, 40, 50, 0};
        for (int i = 0; i < p.length; i++) {
            bottles.put(p[i], b[i]);
            kegs.put(p[i], k[i]);
        }
        used.clear();
        counter = 0;
    }

    private int get(HashMap m, String k) {
        Integer n = (Integer) m.get(k);
        return n == null ? 0 : n;
    }

    public int bottlesLeft(String product) { return get(bottles, product); }
    public void takeBottles(String product, int n) { bottles.put(product, get(bottles, product) - n); }
    public int kegsLeft(String product) { return get(kegs, product); }
    public void takeKegs(String product, int n) { kegs.put(product, get(kegs, product) - n); }
    public int quotaUsed(String key) { return get(used, key); }
    public void addQuota(String key, int n) { used.put(key, get(used, key) + n); }

    public int nextNumber() {
        return ++counter;
    }
}
