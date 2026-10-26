package com.stbrevin.ordering;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StockRegistryTest {

    @Test
    void takeBottles() {
        StockRegistry r = new StockRegistry();
        r.load();
        r.takeBottles("BLO", 100);
        assertThat(r.bottlesLeft("BLO")).isEqualTo(9900);
    }

    @Test
    void quota() {
        StockRegistry r = new StockRegistry();
        r.addQuota("CAFE/2024-3", 24);
        r.addQuota("CAFE/2024-3", 24);
        assertThat(r.quotaUsed("CAFE/2024-3")).isEqualTo(48);
    }

    @Test
    void numbers() {
        StockRegistry r = new StockRegistry();
        assertThat(r.nextNumber()).isLessThan(r.nextNumber());
    }
}
