package com.thornfieldsoft.vegshow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShowJudgeTest {

    private final ShowJudge judge = new ShowJudge();

    @BeforeEach
    void newSeason() {
        GrowerHistory.setInstance(null);
    }

    private Entry pumpkin(String id, String grower, double weight) {
        return new Entry(id, grower, "PUMPKIN", weight, 0, 300, new int[]{7, 8, 6}, 1, 0.8,
                LocalDate.of(2019, 8, 1), 45, false);
    }

    @Test
    void judgesAPumpkinClass() {
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(pumpkin("P1", "Ada Pennock", 210.5));
        entries.add(pumpkin("P2", "Walter Bligh", 180));
        entries.add(pumpkin("P3", "Joan Ferrar", 95));

        List<Placing> board = judge.judge(entries, 2019);

        assertThat(board).hasSize(4);
        assertThat(board.get(0).getEntryId()).isEqualTo("P1");
        assertThat(board.get(0).getRank()).isEqualTo(1);
        assertThat(board.get(0).getPrize()).isGreaterThan(0);
    }

    @Test
    void scoresAMarrow() {
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(new Entry("M1", "Walter Bligh", "MARROW", 30, 80, 70, new int[]{8, 8, 8}, 2, 0.5,
                LocalDate.of(2019, 7, 12), 60, false));

        List<Placing> board = judge.judge(entries, 2019);

        assertThat(board.get(0).getScore()).isGreaterThan(100.0);
    }

    @Test
    void onionClass() {
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(new Entry("O1", "Joan Ferrar", "ONION", 3.2, 0, 60, new int[]{7, 7, 8}, 0, 0.9,
                LocalDate.of(2019, 8, 20), 50, false));
        entries.add(new Entry("O2", "Harold Tusk", "ONION", 2.9, 0, 58, new int[]{6, 7, 7}, 1, 0.9,
                LocalDate.of(2019, 8, 20), 50, false));

        List<Placing> board = judge.judge(entries, 2019);

        assertThat(board).hasSize(2);
        assertThat(board.get(0).getScore()).isGreaterThan(0);
        assertThat(board.get(1).getScore()).isGreaterThan(0);
    }

    @Test
    void lateEntryIsStillJudged() {
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(new Entry("M5", "Harold Tusk", "MARROW", 18, 70, 50, new int[]{6, 7, 7}, 0, 0.9,
                LocalDate.of(2019, 9, 3), 70, false));

        List<Placing> board = judge.judge(entries, 2019);

        assertThat(board).hasSize(1);
        assertThat(board.get(0).getScore()).isNotNull();
    }

    @Test
    void entryForThisYearsShow() {
        int year = LocalDate.now().getYear();
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(new Entry("P9", "Ada Pennock", "PUMPKIN", 150, 0, 280, new int[]{7, 7, 7}, 0, 0.8,
                LocalDate.of(year, 1, 1), 45, false));

        List<Placing> board = judge.judge(entries, year);

        assertThat(board.get(0).getNote()).contains("Cup");
        assertThat(board.get(0).getCertificate()).startsWith("UW-" + year);
    }

    @Test
    void recordsEveryGrowerInTheHistory() {
        GrowerHistory history = mock(GrowerHistory.class);
        when(history.isNew(anyString(), anyInt())).thenReturn(true);
        GrowerHistory.setInstance(history);
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(new Entry("M2", "Nell Kitto", "MARROW", 20, 60, 50, new int[]{5, 6, 7}, 0, 0.5,
                LocalDate.of(2020, 8, 30), 12, true));

        judge.judge(entries, 2020);

        verify(history).isNew("Nell Kitto", 2020);
        verify(history).add("Nell Kitto", 2020);
        GrowerHistory.setInstance(null);
    }

    @Test
    void firstTimeGrowerBonus() {
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(new Entry("M3", "Nell Kitto", "MARROW", 20, 60, 50, new int[]{6, 6, 6}, 0, 0.5,
                LocalDate.of(2020, 8, 30), 12, true));

        List<Placing> board = judge.judge(entries, 2020);

        assertThat(board.get(0).getScore()).isGreaterThan(80.0);
    }

    @Test
    void returningGrowerHasNoBonus() {
        GrowerHistory.getInstance().add("Nell Kitto", 2020);
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(new Entry("M4", "Nell Kitto", "MARROW", 20, 60, 50, new int[]{6, 6, 6}, 0, 0.5,
                LocalDate.of(2020, 8, 30), 12, true));

        List<Placing> board = judge.judge(entries, 2020);

        assertThat(board.get(0).getScore()).isLessThan(85.0);
    }

    @Test
    void tieBreakOnJudgesMarks() {
        Entry a = pumpkin("A", "Ada Pennock", 100);
        Entry b = pumpkin("B", "Walter Bligh", 100);
        b.marks = new int[]{9, 9, 9};

        assertThat(ShowJudge.before(b, 50, a, 50)).isTrue();
    }

    @Test
    void carrotClass() {
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(new Entry("C1", "Joan Ferrar", "CARROT", 1.2, 48, 20, new int[]{8, 7, 8, 7}, 1, 0.6,
                LocalDate.of(2021, 8, 2), 52, false));
        entries.add(new Entry("C2", "Harold Tusk", "CARROT", 0.9, 52, 18, new int[]{6, 7, 7, 6}, 0, 0.6,
                LocalDate.of(2021, 8, 2), 70, false));
        entries.add(new Entry("C3", "Ada Pennock", "CARROT", 1.1, 44, 19, new int[]{7, 7, 7, 7}, 2, 0.6,
                LocalDate.of(2021, 8, 2), 45, false));

        List<Placing> board = judge.judge(entries, 2021);

        assertThat(board).hasSize(4);
        assertThat(board.get(0).getNote()).isNotEmpty();
        assertThat(board.get(3).getNote()).isEqualTo("longest exhibit");
    }

    @Test
    void doesNotFailOnEmptyList() {
        List<Placing> board = judge.judge(new ArrayList<Entry>(), 2019);
        assertThat(board).isNotNull();
    }
}
