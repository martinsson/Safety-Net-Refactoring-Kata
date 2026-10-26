package com.stbrevin.ordering;

import io.ferrow.connect.sandbox.Sandbox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import com.tallowdb.TallowDb;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CheckoutTest {

    static Checkout checkout;

    @BeforeAll
    static void start() {
        Sandbox.start();
        TallowDb.dropAll();
        TestCellar.fill();
        checkout = new Checkout();
    }

    @Test
    @Order(1)
    void cafeOrdersCrates() {
        Receipt r = checkout.place("CAFE-0012", LocalDate.of(2026, 3, 10), "4 CRATE TRI", "2 CRATE DUB");

        assertThat(r.getStatus()).isEqualTo("PLACED");
        assertThat(r.getNumber()).isEqualTo("SB-00001");
        assertThat(r.getTotal()).isGreaterThan(0);
    }

    @Test
    @Order(2)
    void privateCustomerPaysAtOnce() {
        Receipt r = checkout.place("P-77", LocalDate.of(2026, 3, 10), "10 BOTTLE BLO");

        assertThat(r.getLines()).containsExactly("10 x BLO bottle 19.00");
        assertThat(Sandbox.charges().get(0).cents()).isEqualTo(Math.round(r.getTotal() * 100));
    }

    @Test
    @Order(3)
    void confirmationMailIsSent() {
        checkout.place("SH-0101", LocalDate.of(2026, 3, 11), "12 BOTTLE DUB");

        assertThat(Sandbox.mails()).isNotEmpty();
        assertThat(Sandbox.bookings()).isNotEmpty();
    }

    @Test
    @Order(4)
    void privateAllocationIsSmall() {
        Receipt r = checkout.place("P-310", LocalDate.of(2026, 3, 12), "200 BOTTLE TRI");

        assertThat(r.getLines().get(1)).startsWith("BACKORDER");
    }

    @Test
    @Order(5)
    void unknownCustomer() {
        Receipt r = checkout.place("NOBODY", LocalDate.of(2026, 3, 12), "1 BOTTLE TRI");

        assertThat(r.getStatus()).isEqualTo("REFUSED");
    }

    @Test
    @Order(6)
    void emptiesComeBack() {
        Receipt r = checkout.place("CAFE-0012", LocalDate.of(2026, 3, 12), "6 CRATE BLO", "4 RETURN CRATE");

        assertThat(r.getLines()).anyMatch(l -> l.startsWith("RETURN"));
        assertThat(r.getText()).contains("Deposit");
    }

    @Test
    @Order(8)
    void noxOnlyInWinter() {
        Receipt r = checkout.place("SH-0101", LocalDate.of(2026, 6, 9), "24 BOTTLE NOX");

        assertThat(r.getStatus()).isEqualTo("REFUSED");
    }
}
