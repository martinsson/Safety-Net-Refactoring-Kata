package com.mossgate.boarding;

/**
 * A boarder.
 */
public class Pet {

    private String name;
    private String species;
    private int size;
    private boolean medication;

    public Pet(String name, String species, int size, boolean medication) {
        this.name = name;
        this.species = species;
        this.size = size;
        this.medication = medication;
    }

    public String getName() {
        return name;
    }

    public String getSpecies() {
        return species;
    }

    // 1 = small, 2 = medium, 3 = large (dogs only)
    public int getSize() {
        return size;
    }

    public boolean isMedication() {
        return medication;
    }
}
