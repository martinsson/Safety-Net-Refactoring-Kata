package com.mossgate.boarding;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LoyaltyCardTest {

    private static StayBilling billing;

    @BeforeAll
    static void setUp() {
        LoyaltyRegistry.getInstance().reset();
        billing = new StayBilling();
    }

    @Test
    void card_with_100_points_gets_discount() {
        List<Pet> pets = Arrays.asList(new Pet("Rex", "DOG", 2, false));
        Invoice inv = billing.invoice("Martin", "L-1187", LocalDate.of(2024, 3, 4), LocalDate.of(2024, 3, 8), pets);
        assertThat(inv.getLines()).contains("Loyalty -5%");
    }

    @Test
    void points_are_earned() {
        List<Pet> pets = Arrays.asList(new Pet("Rex", "DOG", 2, false));
        Invoice inv = billing.invoice("Martin", "L-1187", LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 5), pets);
        assertThat(inv.getPointsEarned()).isGreaterThan(0);
    }

    @Test
    void gold_card() {
        List<Pet> pets = Arrays.asList(new Pet("Mimi", "CAT", 1, false));
        Invoice inv = billing.invoice("Weber", "G-0042", LocalDate.of(2024, 3, 4), LocalDate.of(2024, 3, 7), pets);
        assertThat(inv.getPointsEarned()).isEqualTo(10);
    }

    @Test
    void estimate_does_not_use_points() {
        List<Pet> pets = Arrays.asList(new Pet("Mimi", "CAT", 1, false));
        double estimate = billing.estimate("Weber", "L-2203", LocalDate.of(2024, 3, 4), LocalDate.of(2024, 3, 7), pets);
        assertThat(estimate).isEqualTo(54.0);
    }
}
