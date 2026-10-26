package com.stbrevin.ordering;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class OrderDeskTest {

    OrderDesk desk;

    @BeforeEach
    void setUp() throws Exception {
        Field f = StockRegistry.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, null);
        desk = new OrderDesk();
    }

    @Test
    void privateCustomerBuysSomeBottles() {
        Confirmation c = desk.process("P-1001", "PRIVATE", 0, LocalDate.of(2024, 3, 12), "10 BOTTLE BLO");

        assertThat(c.getStatus()).isEqualTo(0);
        assertThat(c.getLines()).containsExactly("10 x BLO bottle 19.00");
        assertThat(c.getTotal()).isEqualTo(24.00);
    }

    @Test
    void cafeOrdersCrates() {
        Confirmation c = desk.process("CAFE-0012", "CAFE", 2, LocalDate.of(2024, 3, 12), "4 CRATE TRI", "2 CRATE DUB");

        assertThat(c.getStatus()).isEqualTo(0);
        assertThat(c.getTotal()).isGreaterThan(0);
    }

    @Test
    void notEnoughStock() {
        Confirmation c = desk.process("SH-220", "SHOP", 1, LocalDate.of(2024, 12, 10), "30 CRATE NOX");

        assertThat(c.getStatus()).isEqualTo(1);
        assertThat(c.getLines().get(1)).startsWith("BACKORDER");
    }

    @Test
    void numberHasYear() {
        Confirmation c = desk.process("SH-220", "SHOP", 1, LocalDate.of(2024, 3, 12), "12 BOTTLE DUB");

        assertThat(c.getNumber()).isEqualTo("SB" + LocalDate.now().getYear() + "-0001");
        assertThat(c.getText()).contains("issued " + LocalDate.now());
    }

    @Test
    void noDeliveryOnSunday() {
        Confirmation c = desk.process("SH-220", "SHOP", 1, LocalDate.of(2024, 3, 10), "12 BOTTLE DUB");

        assertThat(c.getDeliveryDate().getDayOfWeek()).isNotEqualTo(DayOfWeek.SUNDAY);
    }

    @Test
    void emptiesAreTakenBack() {
        Confirmation c = desk.process("SH-118", "SHOP", 2, LocalDate.of(2024, 5, 14), "6 CRATE BLO", "4 RETURN CRATE");

        assertThat(c.getLines()).anyMatch(l -> l.startsWith("RETURN"));
        assertThat(c.getText()).contains("Empties returned");
    }

    @Test
    void brotherOrder() {
        Confirmation c = desk.process("BRO-ANSELM", "PRIVATE", 0, LocalDate.of(2024, 5, 14), "24 BOTTLE TRI");

        assertThat(c.getStatus()).isEqualTo(0);
        assertThat(c.getText()).contains("abbey");
    }

    @Test
    void orderForNextMonth() {
        LocalDate wanted = LocalDate.now().plusMonths(1);
        Confirmation c = desk.process("SH-220", "SHOP", 1, wanted, "12 BOTTLE DUB");

        assertThat(c.getStatus()).isEqualTo(0);
        assertThat(c.getDeliveryDate()).isAfterOrEqualTo(wanted);
    }

    @Test
    void emptyOrder() {
        Confirmation c = desk.process("SH-220", "SHOP", 1, LocalDate.of(2024, 3, 12));

        assertThat(c.getStatus()).isEqualTo(2);
        assertThat(c.getText()).isNotNull();
    }
}
