package com.stbrevin.ordering;

import com.tallowdb.TallowDb;
import io.ferrow.connect.sandbox.Sandbox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderDeskStockTest {

    @Mock
    Cellar cellar;

    OrderDesk desk;

    @BeforeEach
    void setUp() {
        Sandbox.start();
        desk = new OrderDesk();
        desk.cellar = cellar;
        TallowDb.open("cellar").table("customers").insert(Map.of("code", "CAFE-0077", "type", "CAFE"));
    }

    @Test
    void takesBottlesForCrates() {
        when(cellar.bottlesLeft("TRI")).thenReturn(1000);
        when(cellar.used("CAFE/2024-3")).thenReturn(0);

        desk.process("CAFE-0077", 1, LocalDate.of(2024, 3, 12), "2 CRATE TRI");

        verify(cellar).takeBottles("TRI", 48);
        verify(cellar).addUsed("CAFE/2024-3", 48);
    }

    @Test
    void nothingTakenWhenSoldOut() {
        when(cellar.bottlesLeft("NOX")).thenReturn(0);
        when(cellar.used(anyString())).thenReturn(0);

        desk.process("SH-220", 1, LocalDate.of(2024, 3, 12), "5 BOTTLE NOX");

        verify(cellar, never()).takeBottles(anyString(), anyInt());
    }
}
