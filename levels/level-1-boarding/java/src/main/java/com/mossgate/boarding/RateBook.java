package com.mossgate.boarding;

public class RateBook {

    // prices per night, 2009 grid
    public double nightlyRate(String species, int size) {
        if (species.equals("CAT"))
            return 18.0;
        if (species.equals("DOG")) {
            if (size <= 1)
                return 22.0;
            return size == 2 ? 27.0 : 34.0;
        }
        if (species.equals("PARROT"))
            return 15.5;
        if (species.equals("TORTOISE"))
            return 9.0;
        if (species.equals("FERRET"))
            return 16.0;
        if (species.equals("SNAKE"))
            return size >= 3 ? 16.0 : 12.0;
        return 20.0;
    }
}
