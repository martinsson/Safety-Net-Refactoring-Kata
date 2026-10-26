package com.stbrevin.ordering;

import com.tallowdb.Database;
import com.tallowdb.TallowDb;

import java.util.Map;

/** Customers and stock used by the tests. Same data as the cellar of the test machine in the office. */
class TestCellar {

    static void fill() {
        Database db = TallowDb.open("cellar");
        if (db.table("customers").count() > 0) return;
        db.table("customers").insert(Map.of("code", "CAFE-0012", "type", "CAFE", "zone", 1, "email", "bar@cafe12.example", "account", "CAFE-0012", "terms", "INVOICE"));
        db.table("customers").insert(Map.of("code", "SH-0101", "type", "SHOP", "zone", 2, "email", "orders@sh0101.example", "account", "SH-0101", "terms", "INVOICE"));
        db.table("customers").insert(Map.of("code", "P-77", "type", "PRIVATE", "zone", 0, "email", "p77@mail.example", "account", "P-77", "terms", "ORDER"));
        db.table("customers").insert(Map.of("code", "P-310", "type", "PRIVATE", "zone", 3, "email", "p310@mail.example", "account", "P-310", "terms", "ORDER"));
        db.table("stock").insert(Map.of("beer", "TRI", "bottles", 6000, "kegs", 30));
        db.table("stock").insert(Map.of("beer", "DUB", "bottles", 8000, "kegs", 40));
        db.table("stock").insert(Map.of("beer", "BLO", "bottles", 10000, "kegs", 50));
        db.table("stock").insert(Map.of("beer", "NOX", "bottles", 900, "kegs", 0));
    }
}
