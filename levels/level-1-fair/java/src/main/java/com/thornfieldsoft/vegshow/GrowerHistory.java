package com.thornfieldsoft.vegshow;

import java.util.HashSet;

/**
 * Growers already seen at a show. Kept for the whole season.
 */
public class GrowerHistory {

    private static GrowerHistory instance;
    private HashSet seen = new HashSet();

    public static GrowerHistory getInstance() {
        if (instance == null) instance = new GrowerHistory();
        return instance;
    }

    static void setInstance(GrowerHistory h) {
        instance = h;
    }

    public boolean isNew(String grower, int year) {
        return !seen.contains(grower.trim().toLowerCase() + "/" + year);
    }

    public void add(String grower, int year) {
        seen.add(grower.trim().toLowerCase() + "/" + year);
    }

    public void clear() {
        seen.clear();
    }
}
