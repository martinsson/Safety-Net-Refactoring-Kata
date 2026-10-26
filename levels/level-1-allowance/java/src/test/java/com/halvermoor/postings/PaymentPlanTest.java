package com.halvermoor.postings;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class PaymentPlanTest {

    private TransferFile file(String agent, String category) {
        TransferFile f = new TransferFile();
        f.agentId = agent;
        f.category = category;
        f.requestDate = LocalDate.of(2022, 3, 14);
        return f;
    }

    @Test
    void smallAmountInOnePayment() {
        PaymentPlan plan = new PaymentPlan(file("P-1", "B"), 900, false, 1);
        assertThat(plan.count).isEqualTo(1);
        assertThat(plan.instalment).isEqualTo(900);
    }

    @Test
    void largeAmountInInstalments() {
        PaymentPlan plan = new PaymentPlan(file("P-2", "A"), 2600, false, 2);
        assertThat(plan.count).isGreaterThan(1);
        assertThat(plan.advance).isZero();
    }

    @Test
    void advanceToRemoteOutpost() {
        PaymentPlan plan = new PaymentPlan(file("P-3", "A"), 2000, true, 1);
        assertThat(plan.advance).isEqualTo(600);
    }
}
