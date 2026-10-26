package com.mossgate.boarding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class StayBillingTest {

    private StayBilling billing = new StayBilling();

    @Test
    void cat_three_week_nights() {
        List<Pet> pets = Arrays.asList(new Pet("Mimi", "CAT", 1, false));
        Invoice inv = billing.invoice("Durand", null, LocalDate.of(2024, 3, 4), LocalDate.of(2024, 3, 7), pets);
        assertThat(inv.getTotal()).isEqualTo(54.0);
        assertThat(inv.getNights()).isEqualTo(3);
    }

    @Test
    void dog_over_a_weekend() {
        List<Pet> pets = Arrays.asList(new Pet("Rex", "DOG", 2, false));
        Invoice inv = billing.invoice("Durand", null, LocalDate.of(2024, 3, 8), LocalDate.of(2024, 3, 11), pets);
        assertThat(inv.getTotal()).isGreaterThan(81.0);
        assertThat(inv.getLines()).hasSize(1);
    }

    @Test
    void rate_comes_from_the_rate_book() {
        RateBook rateBook = mock(RateBook.class);
        when(rateBook.nightlyRate(anyString(), anyInt())).thenReturn(10.0);
        StayBilling b = new StayBilling(rateBook);
        List<Pet> pets = Arrays.asList(new Pet("Rex", "dog", 2, false), new Pet("Tom", "CAT", 1, false));
        b.invoice("Durand", null, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 3), pets);
        verify(rateBook, times(1)).nightlyRate("DOG", 2);
        verify(rateBook, times(1)).nightlyRate("CAT", 1);
    }

    @Test
    void tortoise_is_cheap() {
        List<Pet> pets = Arrays.asList(new Pet("Leon", "TORTOISE", 1, false));
        Invoice inv = billing.invoice("Petit", null, LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 3), pets);
        assertThat(inv.getTotal()).isLessThan(30.0);
    }

    @Test
    void medication_costs_more() {
        List<Pet> plain = Arrays.asList(new Pet("Rex", "DOG", 3, false));
        List<Pet> treated = Arrays.asList(new Pet("Rex", "DOG", 3, true));
        double a = billing.invoice("Petit", null, LocalDate.of(2024, 5, 6), LocalDate.of(2024, 5, 9), plain).getTotal();
        double b = billing.invoice("Petit", null, LocalDate.of(2024, 5, 6), LocalDate.of(2024, 5, 9), treated).getTotal();
        assertThat(b).isGreaterThan(a);
    }

    @Test
    void summer_is_more_expensive() {
        List<Pet> pets = Arrays.asList(new Pet("Coco", "PARROT", 1, false));
        double may = billing.invoice("Roux", null, LocalDate.of(2023, 5, 1), LocalDate.of(2023, 5, 5), pets).getTotal();
        double july = billing.invoice("Roux", null, LocalDate.of(2023, 7, 3), LocalDate.of(2023, 7, 7), pets).getTotal();
        assertThat(july).isGreaterThan(may);
    }

    @Test
    void two_cats() {
        List<Pet> pets = Arrays.asList(new Pet("Mimi", "CAT", 1, false), new Pet("Tom", "CAT", 1, false));
        Invoice inv = billing.invoice("Durand", null, LocalDate.of(2024, 3, 4), LocalDate.of(2024, 3, 6), pets);
        assertThat(inv.getLines()).hasSize(2);
        assertThat(inv.getTotal()).isLessThan(72.0);
    }

    @Test
    void reference_is_set() {
        List<Pet> pets = Arrays.asList(new Pet("Mimi", "CAT", 1, false));
        Invoice inv = billing.invoice("Durand", null, LocalDate.of(2024, 3, 4), LocalDate.of(2024, 3, 5), pets);
        assertThat(inv.getReference()).startsWith("MG" + LocalDate.now().getYear());
    }

    @Test
    void no_pets_does_not_fail() {
        assertThatCode(() -> billing.invoice("Durand", null, LocalDate.of(2024, 3, 4), LocalDate.of(2024, 3, 5),
                new ArrayList<Pet>())).doesNotThrowAnyException();
    }

    @Test
    void estimate_for_next_week() {
        List<Pet> pets = Arrays.asList(new Pet("Rex", "DOG", 2, false));
        LocalDate in = LocalDate.now().plusDays(7);
        double estimate = billing.estimate("Durand", null, in, in.plusDays(3), pets);
        assertThat(estimate).isBetween(81.0, 112.0);
    }

    @Test
    void estimate_is_close_to_invoice() {
        List<Pet> pets = Arrays.asList(new Pet("Mimi", "CAT", 1, false));
        double estimate = billing.estimate("Durand", null, LocalDate.of(2024, 3, 4), LocalDate.of(2024, 3, 7), pets);
        assertThat(estimate).isCloseTo(54.0, within(2.0));
    }

    @Test
    void trade_customer_gets_a_better_price() {
        List<Pet> pets = Arrays.asList(new Pet("Rex", "DOG", 3, false));
        double normal = billing.invoice("Durand", null, LocalDate.of(2024, 6, 3), LocalDate.of(2024, 6, 6), pets).getTotal();
        Invoice trade = billing.invoice("Hollins Kennels", null, LocalDate.of(2024, 6, 3), LocalDate.of(2024, 6, 6), pets);
        assertThat(trade.getTotal()).isLessThan(normal);
        assertThat(trade.getReference()).startsWith("MGT");
    }

    @Test
    void tortoise_hibernation() {
        List<Pet> pets = Arrays.asList(new Pet("Leon", "TORTOISE", 1, false));
        Invoice inv = billing.invoice("Petit", null, LocalDate.of(2024, 11, 15), LocalDate.of(2025, 1, 15), pets);
        assertThat(inv.getLines().get(0)).endsWith("hibernation");
        assertThat(inv.getTotal()).isGreaterThan(60.0);
    }

    @Test
    void vip_parrot() {
        List<Pet> pets = Arrays.asList(new Pet("Coco", "PARROT", 1, false));
        Invoice inv = billing.invoice("Brindlecombe", null, LocalDate.of(2024, 3, 4), LocalDate.of(2024, 3, 8), pets);
        assertThat(inv.getTotal()).isLessThan(80.0);
    }

    @Test
    void estimate_for_a_long_winter_stay() {
        List<Pet> pets = Arrays.asList(new Pet("Leon", "TORTOISE", 1, false), new Pet("Rex", "DOG", 3, true),
                new Pet("Max", "DOG", 1, false));
        double estimate = billing.estimate("Thornby Vets", null, LocalDate.of(2024, 1, 8), LocalDate.of(2024, 2, 9), pets);
        assertThat(estimate).isPositive();
    }
}
