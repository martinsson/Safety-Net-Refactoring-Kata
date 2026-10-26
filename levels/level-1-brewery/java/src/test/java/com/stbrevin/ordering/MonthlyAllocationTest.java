package com.stbrevin.ordering;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlyAllocationTest {

    OrderDesk desk = new OrderDesk();

    @BeforeEach
    void fresh() {
        StockRegistry.getInstance().load();
    }

    @Test
    void firstShopGetsEverything() {
        Confirmation c = desk.process("SH-301", "SHOP", 2, LocalDate.of(2024, 11, 5), "30 CRATE TRI");

        assertThat(c.getStatus()).isEqualTo(0);
    }

    @Test
    void secondShopGetsTheRest() {
        desk.process("SH-301", "SHOP", 2, LocalDate.of(2024, 11, 5), "30 CRATE TRI");
        Confirmation c = desk.process("SH-302", "SHOP", 2, LocalDate.of(2024, 11, 6), "300 BOTTLE DUB");

        assertThat(c.getStatus()).isEqualTo(1);
        assertThat(c.getLines()).contains("BACKORDER 20 x DUB bottle");
    }

    @Test
    void nextMonthIsFreshAgain() {
        desk.process("SH-301", "SHOP", 2, LocalDate.of(2024, 11, 5), "30 CRATE TRI");
        desk.process("SH-302", "SHOP", 2, LocalDate.of(2024, 11, 6), "300 BOTTLE DUB");
        Confirmation c = desk.process("SH-302", "SHOP", 2, LocalDate.of(2024, 12, 3), "60 BOTTLE DUB");

        assertThat(c.getLines()).isNotEmpty();
    }
}
