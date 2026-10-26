package com.halvermoor.postings;

import java.time.LocalDate;
import java.util.Arrays;

public class Outposts {

    static final String[] CODES = {"BRF", "OLM", "OLX", "PAL", "CVR", "SWH", "FNR", "DMR", "IQL", "VSL", "HGP"};
    static final String[] NAMES = {"Brennick Ford", "Oldmere", "Oldmere Annex", "Port Aldry", "Castel Varro",
            "Saltwick Halt", "Fennick Reach", "Dunmarrow", "Isle of Quell", "Varro Sluice", "Harrow Gap"};
    // km on the Trunk Road
    static int[] MARKERS = {0, 112, 104, 205, 286, 431, 517, 604, 286, 318, 575};
    static double[] COEFS = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.12, 1.15, 1.30, 1.0, 1.20};
    static double[] SHORTAGE = new double[11];
    static boolean[] OPEN = {true, true, true, true, true, true, true, true, true, true, true};
    static final int ISLAND = 8;

    private static LocalDate loadedFor;

    // reloads the table when the date changes
    public static void load(LocalDate date) {
        if (date.equals(loadedFor)) return;
        // SWH moved
        MARKERS[5] = date.isBefore(LocalDate.of(2014, 3, 1)) ? 398 : 431;
        OPEN[10] = !date.isBefore(LocalDate.of(2015, 6, 1));
        COEFS[6] = date.isBefore(LocalDate.of(2016, 1, 1)) ? 1.10 : 1.12;
        SHORTAGE[3] = date.isBefore(LocalDate.of(2019, 9, 1)) ? 0 : 180;
        SHORTAGE[4] = SHORTAGE[3];
        SHORTAGE[7] = date.isBefore(LocalDate.of(2021, 1, 1)) ? 0 : 240;
        loadedFor = date;
    }

    public static int find(String code) {
        if (code == null) return -1;
        int i = Arrays.asList(CODES).indexOf(code.trim().toUpperCase());
        if (i >= 0 && !OPEN[i]) return -1;
        return i;
    }

    public static int distance(int from, int to) {
        int km = Math.abs(MARKERS[from] - MARKERS[to]);
        if ((from == ISLAND) != (to == ISLAND)) km = km + 250;
        return km;
    }
}
