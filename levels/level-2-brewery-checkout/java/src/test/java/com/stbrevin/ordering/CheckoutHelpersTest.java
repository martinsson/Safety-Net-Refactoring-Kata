package com.stbrevin.ordering;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CheckoutHelpersTest {

    @Test
    void christmasMovesToBoxingDay() {
        assertThat(Checkout.nextOpenDay(LocalDate.of(2026, 12, 25))).isEqualTo(LocalDate.of(2026, 12, 26));
    }

    @Test
    void weekdayStays() {
        assertThat(Checkout.nextOpenDay(LocalDate.of(2026, 3, 11))).isEqualTo(LocalDate.of(2026, 3, 11));
    }

    @Test
    void prices() {
        assertThat(Checkout.bottlePrice("TRI")).isGreaterThan(Checkout.bottlePrice("BLO"));
        assertThat(Checkout.kegPrice("NOX")).isZero();
        assertThat(Checkout.bottlePrice("IPA")).isNegative();
    }

    @Test
    void format() {
        assertThat(Checkout.fmt(3.5)).isEqualTo("3.50");
        assertThat(Checkout.round2(2.345)).isEqualTo(2.35);
    }
}
