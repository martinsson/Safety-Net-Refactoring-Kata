package com.halvermoor.postings;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Year;

import org.junit.jupiter.api.Test;

class FileLedgerTest {

    @Test
    void referenceContainsTheYear() {
        assertThat(FileLedger.getInstance().nextReference()).startsWith("RRA-" + Year.now() + "-");
    }

    @Test
    void remembersLastSettlement() {
        FileLedger ledger = FileLedger.getInstance();
        ledger.record("T-100", LocalDate.of(2021, 4, 2), "OLM");
        assertThat(ledger.lastSettlement("T-100")).isEqualTo(LocalDate.of(2021, 4, 2));
    }
}
