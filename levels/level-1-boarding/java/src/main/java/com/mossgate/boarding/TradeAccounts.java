package com.mossgate.boarding;

import java.time.LocalDate;

public class TradeAccounts {

    private static final String[] NAMES = { "Hollins Kennels", "Pemberley Farm", "Thornby Vets" };

    public static boolean isTrade(String owner, LocalDate checkIn) {
        if (owner == null) {
            return false;
        }
        for (int i = 0; i < NAMES.length; i++) {
            if (NAMES[i].equalsIgnoreCase(owner.trim())) {
                // Pemberley closed their account, see mail from accounts 2020-01-08
                if (i == 1 && checkIn.isAfter(LocalDate.of(2019, 12, 31))) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }
}
