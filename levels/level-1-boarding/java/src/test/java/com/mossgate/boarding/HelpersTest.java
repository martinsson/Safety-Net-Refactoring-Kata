package com.mossgate.boarding;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Calendar;

import org.junit.jupiter.api.Test;

class HelpersTest {

    @Test
    void days_between() {
        Calendar a = StayBilling.toCal(LocalDate.of(2024, 3, 1));
        Calendar b = StayBilling.toCal(LocalDate.of(2024, 3, 11));
        assertThat(StayBilling.daysBetween(a, b)).isEqualTo(10);
    }

    @Test
    void to_cal_keeps_the_day() {
        Calendar c = StayBilling.toCal(LocalDate.of(2024, 2, 29));
        assertThat(c.get(Calendar.MONTH)).isEqualTo(Calendar.FEBRUARY);
        assertThat(c.get(Calendar.DAY_OF_MONTH)).isEqualTo(29);
    }

    @Test
    void dog_rates() {
        RateBook book = new RateBook();
        assertThat(book.nightlyRate("DOG", 2)).isEqualTo(27.0);
        assertThat(book.nightlyRate("CAT", 1)).isEqualTo(18.0);
    }

    @Test
    void season_factor_can_be_changed() {
        SeasonTable.CURRENT.put(Calendar.FEBRUARY, 1.5);
        assertThat(SeasonTable.factor(Calendar.FEBRUARY, 10)).isEqualTo(1.5);
    }

    @Test
    void summer_factor() {
        assertThat(SeasonTable.factor(Calendar.JULY, 14)).isGreaterThan(1.0);
    }

    @Test
    void loyalty_points_accumulate() {
        LoyaltyRegistry reg = LoyaltyRegistry.getInstance();
        int before = reg.points("T-1");
        reg.add("T-1", 10);
        reg.add("T-1", 5);
        assertThat(reg.points("T-1")).isEqualTo(before + 15);
    }

    @Test
    void trade_accounts() {
        assertThat(TradeAccounts.isTrade("hollins kennels", LocalDate.of(2024, 1, 1))).isTrue();
        assertThat(TradeAccounts.isTrade(null, LocalDate.of(2024, 1, 1))).isFalse();
    }
}
