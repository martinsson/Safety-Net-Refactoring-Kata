package com.halvermoor.postings;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AmountUtilTest {

    @Test
    void roundsDownToFive() {
        assertThat(AmountUtil.roundDown5(1234.0)).isEqualTo(1230.0);
    }

    @Test
    void roundsToCents() {
        assertThat(AmountUtil.round2(10.0 / 3)).isEqualTo(3.33);
    }

    @Test
    void formatsWithThousandsSeparator() {
        assertThat(AmountUtil.format(1234567.5)).isEqualTo("1 234 567.50");
    }

    @Test
    void formatsSmallAmount() {
        assertThat(AmountUtil.format(400)).isNotEmpty();
    }
}
