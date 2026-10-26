package com.halvermoor.postings;

import java.time.LocalDate;

/**
 * Payment schedule of a granted allowance.
 */
public class PaymentPlan {

    int count = 1;
    double advance, instalment, last;

    public PaymentPlan(TransferFile file, double amount, boolean remote, int persons) {
        double rest = amount;
        if (!file.ownRequest && (remote || persons >= 4) && file.requestDate.isBefore(LocalDate.now().minusDays(30))) {
            LocalDate previous = FileLedger.getInstance().lastSettlement(file.agentId);
            if (previous == null || previous.isBefore(file.requestDate.minusMonths(24))) {
                advance = Math.floor(amount * 0.3 / 10) * 10;
                rest = amount - advance;
            }
        }
        if (rest >= 2400) count = 4;
        else if (rest > 1200) count = 3;
        else if (rest > 800 && persons >= 3) count = 2;
        if ("C".equals(file.category) && count == 4) count = 3;
        instalment = count == 1 ? rest : AmountUtil.round2(rest / count);
        last = rest - instalment * (count - 1);
    }
}
