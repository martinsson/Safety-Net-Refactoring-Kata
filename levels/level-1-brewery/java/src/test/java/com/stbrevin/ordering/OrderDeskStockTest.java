package com.stbrevin.ordering;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderDeskStockTest {

    @Mock
    StockRegistry registry;

    OrderDesk desk;

    @BeforeEach
    void setUp() {
        desk = new OrderDesk();
        desk.registry = registry;
    }

    @Test
    void takesBottlesForCrates() {
        when(registry.bottlesLeft("TRI")).thenReturn(1000);
        when(registry.quotaUsed("CAFE/2024-3")).thenReturn(0);

        desk.process("CAFE-0012", "CAFE", 1, LocalDate.of(2024, 3, 12), "2 CRATE TRI");

        verify(registry).takeBottles("TRI", 48);
        verify(registry).addQuota("CAFE/2024-3", 48);
        verify(registry).nextNumber();
    }

    @Test
    void nothingTakenWhenSoldOut() {
        when(registry.bottlesLeft("NOX")).thenReturn(0);
        when(registry.quotaUsed(anyString())).thenReturn(0);

        desk.process("SH-220", "SHOP", 1, LocalDate.of(2024, 3, 12), "5 BOTTLE NOX");

        verify(registry, never()).takeBottles(anyString(), org.mockito.ArgumentMatchers.anyInt());
    }
}
