package com.halvermoor.postings;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class OutpostsTest {

    @Test
    void findsOutpostWhateverTheCase() {
        assertThat(Outposts.find("cvr")).isEqualTo(4);
    }

    @Test
    void unknownOutpost() {
        assertThat(Outposts.find("ZZZ")).isEqualTo(-1);
    }

    @Test
    void distanceAlongTheTrunkRoad() {
        assertThat(Outposts.distance(0, 4)).isEqualTo(286);
    }

    @Test
    void islandIsFurther() {
        int island = Outposts.distance(0, Outposts.find("IQL"));
        assertThat(island).isGreaterThan(286);
    }

    @Test
    void harrowGapOpenedIn2015() {
        Outposts.load(LocalDate.of(2015, 1, 1));
        assertThat(Outposts.find("HGP")).isEqualTo(-1);
    }
}
