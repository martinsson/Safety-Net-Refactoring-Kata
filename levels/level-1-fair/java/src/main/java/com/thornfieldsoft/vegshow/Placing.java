package com.thornfieldsoft.vegshow;

/**
 * A line of the results board.
 */
public class Placing {

    private String category, entryId, grower;
    String note;
    private int rank;
    private double score;
    double prize;
    String certificate;

    public Placing(String category, int rank, String entryId, String grower, double score, String note) {
        this.category = category;
        this.rank = rank;
        this.entryId = entryId;
        this.grower = grower;
        this.score = score;
        this.note = note;
    }

    public String getCategory() { return category; }
    public int getRank() { return rank; }
    public String getEntryId() { return entryId; }
    public String getGrower() { return grower; }
    public double getScore() { return score; }
    public double getPrize() { return prize; }
    public String getNote() { return note; }
    public String getCertificate() { return certificate; }
}
