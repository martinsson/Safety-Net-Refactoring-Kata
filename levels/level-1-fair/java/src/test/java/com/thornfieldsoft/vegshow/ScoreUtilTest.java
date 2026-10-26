package com.thornfieldsoft.vegshow;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreUtilTest {

    @Test
    void averageOfMarks() {
        assertThat(ScoreUtil.average(new int[]{6, 7, 8})).isEqualTo(7.0);
    }

    @Test
    void averageOfNoMarks() {
        assertThat(ScoreUtil.average(new int[0])).isEqualTo(0.0);
    }

    @Test
    void roundsToOneDecimal() {
        assertThat(ScoreUtil.round1(12.34)).isEqualTo(12.3);
    }

    @Test
    void certificateNumber() {
        String cert = ScoreUtil.nextCertificate();
        assertThat(cert).startsWith("UW-");
    }
}
