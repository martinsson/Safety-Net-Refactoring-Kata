package com.stbrevin.ordering;

import com.tallowdb.Database;
import com.tallowdb.Row;
import com.tallowdb.TallowDb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cellar {

    Database db;

    public Cellar() {
        db = TallowDb.open("cellar");
        if (db.table("beers").count() == 0) load();
    }

    // first start of the desk: the cellar as it was counted in spring
    void load() {
        String[] p = {"TRI", "DUB", "BLO", "NOX"};
        int[] b = {6000, 8000, 10000, 900};
        int[] k = {30, 40, 50, 0};
        int[] bp = {240, 210, 190, 320};
        int[] kp = {14500, 12000, 11000, 0};
        for (int i = 0; i < p.length; i++) {
            HashMap row = new HashMap();
            row.put("beer", p[i]);
            row.put("bottles", b[i]);
            row.put("kegs", k[i]);
            row.put("bottle_price", bp[i]);
            row.put("keg_price", kp[i]);
            db.table("beers").insert(row);
        }
    }

    private Row beer(String product) {
        List<Row> rows = db.table("beers").find("beer", product);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public int bottlesLeft(String product) {
        Row r = beer(product);
        return r == null ? 0 : r.getInt("bottles");
    }

    public void takeBottles(String product, int n) {
        Row r = beer(product);
        db.table("beers").update(r.id(), Map.of("bottles", r.getLong("bottles") - n));
    }

    public int kegsLeft(String product) {
        Row r = beer(product);
        return r == null ? 0 : r.getInt("kegs");
    }

    public void takeKegs(String product, int n) {
        Row r = beer(product);
        db.table("beers").update(r.id(), Map.of("kegs", r.getLong("kegs") - n));
    }

    public int used(String key) {
        List<Row> rows = db.table("allocations").find("key", key);
        return rows.isEmpty() ? 0 : rows.get(0).getInt("used");
    }

    public void addUsed(String key, int n) {
        List<Row> rows = db.table("allocations").find("key", key);
        if (rows.isEmpty()) {
            HashMap row = new HashMap();
            row.put("key", key);
            row.put("used", n);
            db.table("allocations").insert(row);
        } else {
            db.table("allocations").update(rows.get(0).id(), Map.of("used", rows.get(0).getLong("used") + n));
        }
    }
}
