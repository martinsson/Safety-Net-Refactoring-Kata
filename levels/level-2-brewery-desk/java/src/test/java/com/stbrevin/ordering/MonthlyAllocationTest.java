package com.stbrevin.ordering;

import com.tallowdb.TallowDb;
import io.ferrow.connect.sandbox.Sandbox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlyAllocationTest {

    OrderDesk desk;

    @BeforeEach
    void fresh() {
        Sandbox.reset();
        TallowDb.dropAll();
        TallowDb.open("cellar").table("customers").insert(Map.of("code", "SH-301", "type", "SHOP"));
        TallowDb.open("cellar").table("customers").insert(Map.of("code", "SH-302", "type", "SHOP"));
        desk = new OrderDesk();
    }

    @AfterEach
    void stop() {
        Sandbox.stop();
    }

    @Test
    void firstShopGetsEverything() {
        Confirmation c = desk.process("SH-301", 2, LocalDate.of(2024, 11, 5), "30 CRATE TRI");

        assertThat(c.getStatus()).isEqualTo(0);
    }

    @Test
    void secondShopGetsTheRest() {
        desk.process("SH-301", 2, LocalDate.of(2024, 11, 5), "30 CRATE TRI");
        Confirmation c = desk.process("SH-302", 2, LocalDate.of(2024, 11, 6), "300 BOTTLE DUB");

        assertThat(c.getStatus()).isEqualTo(1);
        assertThat(c.getLines()).contains("BACKORDER 20 x DUB bottle");
    }

    @Test
    void nextMonthIsFreshAgain() {
        desk.process("SH-301", 2, LocalDate.of(2024, 11, 5), "30 CRATE TRI");
        desk.process("SH-302", 2, LocalDate.of(2024, 11, 6), "300 BOTTLE DUB");
        Confirmation c = desk.process("SH-302", 2, LocalDate.of(2024, 12, 3), "60 BOTTLE DUB");

        assertThat(c.getLines()).isNotEmpty();
    }
}
