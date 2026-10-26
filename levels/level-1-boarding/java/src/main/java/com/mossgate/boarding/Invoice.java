package com.mossgate.boarding;

import java.util.ArrayList;
import java.util.List;

public class Invoice {

    String reference;
    int nights;
    double total;
    int pointsEarned;
    List<String> lines = new ArrayList<String>();

    public String getReference() {
        return reference;
    }

    public int getNights() {
        return nights;
    }

    // total in EUR, VAT excluded
    public double getTotal() {
        return total;
    }

    public int getPointsEarned() {
        return pointsEarned;
    }

    public List<String> getLines() {
        return lines;
    }
}
