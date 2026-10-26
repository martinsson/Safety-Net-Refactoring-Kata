package com.thornfieldsoft.vegshow;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GrowerHistoryTest {

    @Test
    void growerIsRememberedForTheYear() {
        GrowerHistory h = new GrowerHistory();
        h.add("Ada Pennock", 2021);
        assertThat(h.isNew("Ada Pennock", 2021)).isFalse();
        assertThat(h.isNew("Ada Pennock", 2022)).isTrue();
    }

    @Test
    void singleton() {
        assertThat(GrowerHistory.getInstance()).isSameAs(GrowerHistory.getInstance());
    }

    @Test
    void addGrowerToSeason() {
        GrowerHistory.getInstance().add("Joan Ferrar", 2023);
        assertThat(GrowerHistory.getInstance()).isNotNull();
    }

    @Test
    void returningGrowerIsKnown() {
        assertThat(GrowerHistory.getInstance().isNew(" joan ferrar", 2023)).isFalse();
    }
}
