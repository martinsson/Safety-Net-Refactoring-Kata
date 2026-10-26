package com.stbrevin.ordering;

import com.tallowdb.TallowDb;
import io.ferrow.connect.sandbox.Sandbox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class OrderDeskTest {

    OrderDesk desk;

    @BeforeAll
    static void platform() {
        Sandbox.start();
    }

    @BeforeEach
    void setUp() {
        TallowDb.dropAll();
        desk = new OrderDesk();
        customer("CAFE-0012", "CAFE", null, null);
        customer("SH-220", "SHOP", null, null);
    }

    static void customer(String code, String type, String email, String account) {
        HashMap row = new HashMap();
        row.put("code", code);
        row.put("type", type);
        row.put("email", email);
        row.put("account", account);
        TallowDb.open("cellar").table("customers").insert(row);
    }

    @Test
    void privateCustomerBuysSomeBottles() {
        Confirmation c = desk.process("P-1001", 0, LocalDate.of(2024, 3, 12), "10 BOTTLE BLO");

        assertThat(c.getStatus()).isEqualTo(0);
        assertThat(c.getLines()).containsExactly("10 x BLO bottle 19.00");
        assertThat(c.getTotal()).isEqualTo(24.00);
    }

    @Test
    void cafeOrdersCrates() {
        Confirmation c = desk.process("CAFE-0012", 2, LocalDate.of(2024, 3, 12), "4 CRATE TRI", "2 CRATE DUB");

        assertThat(c.getStatus()).isEqualTo(0);
        assertThat(c.getTotal()).isGreaterThan(0);
    }

    @Test
    void notEnoughStock() {
        Confirmation c = desk.process("SH-220", 1, LocalDate.of(2024, 12, 10), "30 CRATE NOX");

        assertThat(c.getStatus()).isEqualTo(1);
        assertThat(c.getLines().get(1)).startsWith("BACKORDER");
    }

    @Test
    void numberHasYear() {
        Confirmation c = desk.process("SH-220", 1, LocalDate.of(2024, 3, 12), "12 BOTTLE DUB");

        assertThat(c.getNumber()).isEqualTo("SB" + LocalDate.now().getYear() + "-0001");
        assertThat(c.getText()).contains("issued " + LocalDate.now());
    }

    @Test
    void noDeliveryOnSunday() {
        Confirmation c = desk.process("SH-220", 1, LocalDate.of(2024, 3, 10), "12 BOTTLE DUB");

        assertThat(c.getDeliveryDate().getDayOfWeek()).isNotEqualTo(DayOfWeek.SUNDAY);
    }

    @Test
    void brotherOrder() {
        Confirmation c = desk.process("BRO-ANSELM", 0, LocalDate.of(2024, 5, 14), "24 BOTTLE TRI");

        assertThat(c.getStatus()).isEqualTo(0);
        assertThat(c.getText()).contains("abbey");
    }

    @Test
    void orderForNextMonth() {
        LocalDate wanted = LocalDate.now().plusMonths(1);
        Confirmation c = desk.process("SH-220", 1, wanted, "12 BOTTLE DUB");

        assertThat(c.getStatus()).isEqualTo(0);
        assertThat(c.getDeliveryDate()).isAfterOrEqualTo(wanted);
    }

    @Test
    void emptyOrder() {
        Confirmation c = desk.process("SH-220", 1, LocalDate.of(2024, 3, 12));

        assertThat(c.getStatus()).isEqualTo(2);
        assertThat(c.getText()).isNotNull();
    }

    @Test
    void accountCustomerIsCharged() {
        customer("CAFE-0040", "CAFE", null, "ACC-40");

        Confirmation c = desk.process("CAFE-0040", 1, LocalDate.of(2024, 3, 12), "2 CRATE BLO");

        assertThat(Sandbox.charges()).hasSize(1);
        assertThat(Sandbox.charges().get(0).reference()).isEqualTo(c.getNumber());
    }

    @Test
    void confirmationIsMailed() {
        customer("SH-0050", "SHOP", "orders@shop50.example", null);

        desk.process("SH-0050", 2, LocalDate.of(2024, 3, 12), "2 CRATE BLO");

        assertThat(Sandbox.mails().get(0).to()).isEqualTo("orders@shop50.example");
    }
}
