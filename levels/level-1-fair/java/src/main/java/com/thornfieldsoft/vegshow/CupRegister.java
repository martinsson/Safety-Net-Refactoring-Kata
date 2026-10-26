package com.thornfieldsoft.vegshow;

import java.util.HashMap;

/**
 * Who holds which challenge cup. One line per cup and year.
 */
public class CupRegister {

    private static HashMap holders = new HashMap();

    public static String award(String cup, String grower, int year) {
        String name = grower.trim().toLowerCase();
        String note = cup;
        if (name.equals(holders.get(cup + "/" + (year - 1))) && name.equals(holders.get(cup + "/" + (year - 2)))) {
            note = cup + " (won outright)";
        }
        holders.put(cup + "/" + year, name);
        return note;
    }

    public static String holder(String cup, int year) {
        return (String) holders.get(cup + "/" + year);
    }
}
