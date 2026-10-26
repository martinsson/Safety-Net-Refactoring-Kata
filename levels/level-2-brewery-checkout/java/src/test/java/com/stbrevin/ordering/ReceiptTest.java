package com.stbrevin.ordering;

import com.tallowdb.TallowDb;
import io.ferrow.connect.sandbox.Sandbox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ReceiptTest {

    @BeforeAll
    static void cellar() {
        Sandbox.start();
        TallowDb.dropAll();
        TestCellar.fill();
    }

    @Test
    void orderForNextMonth() {
        LocalDate wanted = LocalDate.now().plusMonths(1);
        Receipt r = new Checkout().place("SH-0101", wanted, "2 CRATE TRI");

        assertThat(r.getStatus()).isEqualTo("PLACED");
        assertThat(r.getDeliveryDate()).isAfterOrEqualTo(wanted);
        assertThat(r.getText()).contains("issued " + LocalDate.now());
    }

    @Test
    void sundayDelivery() {
        Receipt r = new Checkout().place("P-77", LocalDate.of(2026, 3, 8), "6 BOTTLE DUB");

        assertThat(r.getDeliveryDate().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }
}
