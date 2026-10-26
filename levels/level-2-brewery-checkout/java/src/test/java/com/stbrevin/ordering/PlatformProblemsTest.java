package com.stbrevin.ordering;

import com.tallowdb.TallowDb;
import io.ferrow.connect.RemoteException;
import io.ferrow.connect.sandbox.Sandbox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlatformProblemsTest {

    @BeforeEach
    void setUp() {
        Sandbox.reset();
        TallowDb.dropAll();
        TestCellar.fill();
    }

    @Test
    void declinedPayment() {
        Sandbox.payments().decline().forAccount("P-77");

        Receipt r = new Checkout().place("P-77", LocalDate.of(2026, 3, 10), "6 BOTTLE TRI");

        assertThat(r.getStatus()).isEqualTo("DECLINED");
        assertThat(Sandbox.mails()).hasSize(1);
    }

    @Test
    void paymentPlatformDown() {
        Sandbox.payments().unreachable();

        Receipt r = new Checkout().place("P-310", LocalDate.of(2026, 3, 10), "6 BOTTLE TRI");

        assertThat(r.getMessage()).isEqualTo("payment pending");
    }

    @Test
    void courierDown() {
        Sandbox.courier().book().unreachable();

        Receipt r = new Checkout().place("CAFE-0012", LocalDate.of(2026, 3, 10), "2 CRATE TRI");

        assertThat(r.getMessage()).isEqualTo("delivery to be arranged");
    }

    @Test
    void noQuoteFromTheCourier() {
        Sandbox.courier().quote().unreachable();

        Receipt r = new Checkout().place("CAFE-0012", LocalDate.of(2026, 3, 10), "2 CRATE TRI");

        assertThat(r.getText()).contains("Shipping 9.00");
    }

    @Test
    void mailDown() {
        Sandbox.mail().unreachable();

        assertThatThrownBy(() -> new Checkout().place("CAFE-0012", LocalDate.of(2026, 3, 10), "2 CRATE TRI"))
                .isInstanceOf(RemoteException.class);
    }
}
