package com.halvermoor.postings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;

class AllowanceDeskTest {

    private AllowanceDesk desk = new AllowanceDesk();

    @BeforeEach
    void setUp() {
        FileLedger.getInstance().clear();
    }

    private TransferFile file(String agent, String category, String from, String to, LocalDate date) {
        TransferFile f = new TransferFile();
        f.agentId = agent;
        f.category = category;
        f.fromCode = from;
        f.toCode = to;
        f.requestDate = date;
        return f;
    }

    @Test
    void serviceTransfer() {
        AllowanceNotice notice = desk.process(file("A-1001", "A", "BRF", "CVR", LocalDate.of(2020, 3, 1)));

        assertThat(notice).isNotNull();
        assertThat(notice.getStatus()).isEqualTo(1);
        assertThat(notice.getAmount()).isEqualTo(1570.0);
        assertThat(notice.getText()).contains("Castel Varro");
    }

    @Test
    void familyMovingToRemoteOutpost() {
        TransferFile f = file("B-2001", "B", "OLM", "DMR", LocalDate.of(2019, 5, 1));
        f.partner = true;
        f.children = 3;
        f.yearsOfService = 12;

        AllowanceNotice notice = desk.process(f);

        assertThat(notice.getAmount()).isGreaterThan(1500);
        assertThat(notice.getInstalments()).isEqualTo(3);
        assertThat(notice.getText()).contains("Household").contains("Remote");
    }

    @Test
    void ownRequest() {
        TransferFile own = file("C-3002", "C", "CVR", "VSL", LocalDate.of(2018, 1, 10));
        own.ownRequest = true;
        own.yearsOfService = 25;

        AllowanceNotice notice = desk.process(own);

        assertThat(notice.getStatus()).isEqualTo(1);
        assertThat(notice.getText()).contains("own request");
    }

    @Test
    void sameLocality() {
        AllowanceNotice notice = desk.process(file("C-3003", "C", "OLM", "OLX", LocalDate.of(2019, 5, 1)));

        assertThat(notice.getStatus()).isEqualTo(9);
        assertThat(notice.getAmount()).isZero();
    }

    @Test
    void unknownOutpostIsRejected() {
        AllowanceNotice notice = desk.process(file("C-3004", "C", "OLM", "XYZ", LocalDate.of(2019, 5, 1)));

        assertThat(notice.getStatus()).isNotEqualTo(1);
    }

    @Test
    void requestOfLastWeek() {
        AllowanceNotice notice = desk.process(file("B-2002", "B", "PAL", "FNR", LocalDate.now().minusDays(7)));

        assertThat(notice.getAmount()).isGreaterThan(0);
        assertThat(notice.getAdvance()).isZero();
        assertThat(notice.getReference()).isNotNull();
    }

    @Test
    void requestInTheFuture() {
        AllowanceNotice notice = desk.process(file("B-2003", "B", "PAL", "FNR", LocalDate.now().plusDays(30)));

        assertThat(notice.getStatus()).isEqualTo(9);
    }

    @Test
    void secondTransfer() {
        AllowanceNotice first = desk.process(file("A-1002", "A", "BRF", "SWH", LocalDate.of(2022, 2, 1)));
        AllowanceNotice second = desk.process(file("A-1002", "A", "SWH", "BRF", LocalDate.of(2022, 8, 1)));

        assertThat(first.getAmount()).isGreaterThan(0);
        assertThat(second.getText()).contains("A-1002");
    }

    @Test
    void largeAmountIsSplit() {
        TransferFile f = file("A-1003", "A", "BRF", "DMR", LocalDate.of(2021, 6, 1));
        f.partner = true;
        f.children = 2;
        f.yearsOfService = 22;

        AllowanceNotice notice = desk.process(f);

        assertThat(notice.getInstalments()).isGreaterThan(1);
        assertThat(notice.getInstalmentAmount()).isGreaterThan(0);
        assertThat(notice.getLastInstalment()).isGreaterThan(0);
    }

    @Test
    void contractStaff() {
        TransferFile f = file("D-4002", "D", "VSL", "HGP", LocalDate.of(2023, 4, 3));
        f.yearsOfService = 15;

        AllowanceNotice notice = desk.process(f);

        assertThat(notice.getStatus()).isEqualTo(1);
        assertThat(notice.getAmount()).isLessThanOrEqualTo(1800);
    }

    @Test
    void advanceForLargeFamily() {
        TransferFile f = file("A-1006", "A", "OLM", "FNR", LocalDate.of(2017, 9, 12));
        f.partner = true;
        f.children = 3;

        AllowanceNotice notice = desk.process(f);

        assertThat(notice.getAdvance()).isGreaterThan(0);
        assertThat(notice.getText()).contains("Advance");
    }

    @Test
    void settledFileIsRecorded() {
        FileLedger ledger = mock(FileLedger.class);
        try (MockedStatic<FileLedger> mocked = mockStatic(FileLedger.class)) {
            mocked.when(FileLedger::getInstance).thenReturn(ledger);
            when(ledger.nextReference()).thenReturn("RRA-TEST");

            desk.process(file("A-1004", "A", "BRF", "PAL", LocalDate.of(2020, 1, 1)));

            verify(ledger, times(2)).lastSettlement("A-1004");
            verify(ledger).record("A-1004", LocalDate.of(2020, 1, 1), "BRF");
        }
    }

    @Test
    void rejectedFileIsNotRecorded() {
        FileLedger ledger = mock(FileLedger.class);
        try (MockedStatic<FileLedger> mocked = mockStatic(FileLedger.class)) {
            mocked.when(FileLedger::getInstance).thenReturn(ledger);

            desk.process(file("A-1005", "A", "OLM", "OLX", LocalDate.of(2020, 1, 1)));

            verify(ledger, never()).record(ArgumentMatchers.anyString(), ArgumentMatchers.any(), ArgumentMatchers.any());
        }
    }
}
