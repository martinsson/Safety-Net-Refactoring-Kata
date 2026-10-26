package com.thornfieldsoft.vegshow;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CupRegisterTest {

    @Test
    void cupIsRecorded() {
        CupRegister.award("Village Hall Plate", "Ada Pennock", 2018);
        assertThat(CupRegister.holder("Village Hall Plate", 2018)).isEqualTo("ada pennock");
    }

    @Test
    void thirdYearInARow() {
        CupRegister.award("Parish Bowl", "Walter Bligh", 2017);
        CupRegister.award("Parish Bowl", "Walter Bligh", 2018);
        assertThat(CupRegister.award("Parish Bowl", "Walter Bligh", 2019)).contains("outright");
    }
}
