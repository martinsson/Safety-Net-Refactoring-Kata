package com.thornfieldsoft.vegshow;

import java.time.LocalDate;

/**
 * One exhibit as written on the entry card.
 */
public class Entry {

    public String id, grower, category;
    public double weight, length, girth;   // kg, cm, cm
    public int[] marks;                    // one mark per judge, out of 10
    public int blemishes;
    public double symmetry;                // 0..1 from the measuring board
    public LocalDate registered;
    public int age;
    public boolean firstTime;

    public Entry(String id, String grower, String category, double weight, double length, double girth,
                 int[] marks, int blemishes, double symmetry, LocalDate registered, int age, boolean firstTime) {
        this.id = id;
        this.grower = grower;
        this.category = category;
        this.weight = weight;
        this.length = length;
        this.girth = girth;
        this.marks = marks;
        this.blemishes = blemishes;
        this.symmetry = symmetry;
        this.registered = registered;
        this.age = age;
        this.firstTime = firstTime;
    }
}
