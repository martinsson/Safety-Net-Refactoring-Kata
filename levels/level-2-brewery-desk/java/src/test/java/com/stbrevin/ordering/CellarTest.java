package com.stbrevin.ordering;

import com.tallowdb.TallowDb;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CellarTest {

    @BeforeAll
    static void emptyCellar() {
        TallowDb.dropAll();
    }

    @Test
    @Order(1)
    void takeBottles() {
        Cellar cellar = new Cellar();
        cellar.takeBottles("BLO", 100);
        assertThat(cellar.bottlesLeft("BLO")).isEqualTo(9900);
    }

    @Test
    @Order(2)
    void takeMoreBottles() {
        Cellar cellar = new Cellar();
        cellar.takeBottles("BLO", 100);
        assertThat(cellar.bottlesLeft("BLO")).isEqualTo(9800);
    }

    @Test
    @Order(3)
    void allocation() {
        Cellar cellar = new Cellar();
        cellar.addUsed("CAFE/2024-3", 24);
        cellar.addUsed("CAFE/2024-3", 24);
        assertThat(cellar.used("CAFE/2024-3")).isEqualTo(48);
    }
}
