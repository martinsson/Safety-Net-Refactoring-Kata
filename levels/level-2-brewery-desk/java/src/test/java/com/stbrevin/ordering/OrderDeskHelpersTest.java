package com.stbrevin.ordering;

import io.ferrow.connect.sandbox.Sandbox;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class OrderDeskHelpersTest {

    @Test
    void sundayMovesToMonday() {
        assertThat(OrderDesk.nextOpenDay(LocalDate.of(2024, 3, 10), 1)).isEqualTo(LocalDate.of(2024, 3, 11));
    }

    @Test
    void weekdayStays() {
        assertThat(OrderDesk.nextOpenDay(LocalDate.of(2024, 3, 13), 1)).isEqualTo(LocalDate.of(2024, 3, 13));
    }

    @Test
    void easterMonday() {
        assertThat(OrderDesk.easter(2024)).isEqualTo(LocalDate.of(2024, 3, 31));
        assertThat(OrderDesk.nextOpenDay(LocalDate.of(2024, 4, 1), 1)).isNotEqualTo(LocalDate.of(2024, 4, 1));
    }

    @Test
    void prices() {
        new Cellar();
        assertThat(OrderDesk.bottlePrice("TRI")).isGreaterThan(OrderDesk.bottlePrice("BLO"));
        assertThat(OrderDesk.kegPrice("NOX")).isZero();
    }

    @Test
    void shippingFollowsTheCourierTariff() {
        Sandbox.start();
        assertThat(OrderDesk.shipping(2, 25)).isEqualTo(21.00);
    }

    @Test
    void format() {
        assertThat(OrderDesk.fmt(3.5)).isEqualTo("3.50");
    }
}
