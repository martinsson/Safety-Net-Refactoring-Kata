package com.halvermoor.postings;

public class AllowanceNotice {

    int status, instalments;
    double amount, advance, instalmentAmount, lastInstalment;
    String reference, text;

    public int getStatus() { return status; }
    public double getAmount() { return amount; }
    public double getAdvance() { return advance; }
    public int getInstalments() { return instalments; }
    public double getInstalmentAmount() { return instalmentAmount; }
    public double getLastInstalment() { return lastInstalment; }
    public String getReference() { return reference; }
    public String getText() { return text; }
}
