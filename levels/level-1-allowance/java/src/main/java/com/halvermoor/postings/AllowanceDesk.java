package com.halvermoor.postings;

import java.time.LocalDate;

/**
 * Removal and resettlement allowance (RRA).
 * Computes the amount due for a transfer file and issues the notice sent to the agent.
 */
public class AllowanceDesk {

    static final double KM_RATE = 0.95;
    static final int MIN_DISTANCE = 50;

    public AllowanceNotice process(TransferFile file) {
        FileLedger ledger = FileLedger.getInstance();
        AllowanceNotice notice = new AllowanceNotice();
        notice.reference = ledger.nextReference();
        StringBuffer txt = new StringBuffer();
        txt.append("Agent " + file.agentId + ", category " + file.category + ".\n");

        if (file.requestDate == null || file.requestDate.isAfter(LocalDate.now()))
            return reject(notice, txt, "Request rejected: the request date is later than the processing date.");
        if (file.ownRequest && "D".equals(file.category))
            return reject(notice, txt, "Request rejected: transfers at the agent's own request are not covered for contract staff.");
        Outposts.load(file.requestDate);
        int from = Outposts.find(file.fromCode);
        int to = Outposts.find(file.toCode);
        if (from < 0 || to < 0)
            return reject(notice, txt, "Request rejected: unknown outpost.");
        txt.append("Transfer from " + Outposts.NAMES[from] + " to " + Outposts.NAMES[to] + ".\n");
        int km = Outposts.distance(from, to);
        if (km < 20)
            return reject(notice, txt, "No allowance: transfer within the same locality.");
        boolean old = file.requestDate.isBefore(LocalDate.of(2011, 7, 1));
        txt.append("Distance retained: " + km + " km" + (old ? ".\n" : " (band " + (km < 200 ? "I" : km < 500 ? "II" : "III") + ").\n"));

        String cat = file.category;
        double base = "A".equals(cat) ? 1150 : "B".equals(cat) ? 940 : "D".equals(cat) ? 640 : 760;

        double hh = file.partner ? 1.2 : 1.0;
        for (int i = 1; i <= file.children; i++) {
            if (i <= 2 || old) hh = hh + 0.1;
            else hh = hh + 0.15;
        }
        if (hh > 1.8) hh = 1.8;
        int persons = 1 + (file.partner ? 1 : 0) + file.children;
        if (persons > 1) txt.append("Household of " + persons + " persons taken into account.\n");

        double amount;
        boolean remote = false;
        if (!file.ownRequest) {
            amount = base * hh;
            double dist;
            if (old) dist = km * 0.95;
            else if (km < 200) dist = km * 1.10;
            else if (km < 500) dist = km * 0.85;
            else if (file.requestDate.isBefore(LocalDate.of(2018, 1, 1))) dist = 120 + km * 0.60;
            else dist = 150 + km * 0.55;
            LocalDate prev = ledger.lastSettlement(file.agentId);
            if (prev != null && prev.isAfter(file.requestDate.minusMonths(24)) && Outposts.CODES[to].equals(ledger.lastOrigin(file.agentId))) {
                dist = 0;
                txt.append("Return to the previous outpost: no distance supplement.\n");
            }
            amount = amount + dist;
            int month = file.requestDate.getMonthValue();
            if (to == 10 && (month >= 11 || month <= 3)) {
                amount = amount + 150;
                txt.append("Winter installation supplement.\n");
            }
            if (Outposts.SHORTAGE[to] > 0) {
                amount = amount + Outposts.SHORTAGE[to];
                txt.append("Housing shortage supplement for " + Outposts.NAMES[to] + ".\n");
            }
            if (Outposts.COEFS[to] > 1.0) {
                amount = amount * Outposts.COEFS[to];
                remote = true;
            }
            if ("D".equals(cat)) {
                // contract staff
            } else if (file.yearsOfService >= 20) amount = amount * 1.08;
            else if (file.yearsOfService >= 10) amount = amount * 1.05;
        } else {
            // own request
            amount = base * hh * 0.6;
            double dist = 0;
            if (km >= 50) {
                if (old) dist = km * 0.95;
                else if (km < 200) dist = km * 1.10;
                else if (km <= 500) dist = km * 0.85;
                else if (file.requestDate.isBefore(LocalDate.of(2019, 4, 1))) dist = 120 + km * 0.60;
                else dist = 150 + km * 0.55;
                LocalDate prev = ledger.lastSettlement(file.agentId);
                if (prev != null && prev.isAfter(file.requestDate.minusMonths(24)) && Outposts.CODES[to].equals(ledger.lastOrigin(file.agentId))) {
                    dist = 0;
                    txt.append("Return to the previous outpost: no distance supplement.\n");
                }
            }
            amount = amount + dist * 0.6;
            // #2231, #2398
            if (to == 8 || to == 10) {
                amount = amount * Outposts.COEFS[to];
                remote = true;
            }
            if (Outposts.SHORTAGE[to] > 0) {
                amount = amount + Outposts.SHORTAGE[to] / 2;
                txt.append("Housing shortage supplement for " + Outposts.NAMES[to] + ".\n");
            }
            if (file.yearsOfService >= 20) amount = amount + 50;
            txt.append("Transfer at the agent's own request: reduced rate.\n");
        }
        if (remote) txt.append("Remote outpost coefficient applied (" + Outposts.COEFS[to] + ").\n");
        if (Outposts.COEFS[from] > 1.0 && Outposts.COEFS[to] <= 1.0 && file.yearsAtPreviousPost >= 3) {
            amount = amount + (old ? 250 : 300);
            txt.append("Return bonus after " + file.yearsAtPreviousPost + " years in a remote outpost.\n");
        }

        LocalDate last = ledger.lastSettlement(file.agentId);
        if (last != null && last.isAfter(file.requestDate.minusMonths(12))) {
            amount = amount / 2;
            txt.append("Second transfer within twelve months: amount halved.\n");
        }

        amount = AmountUtil.roundDown5(amount);
        // ceiling is 5000 for all categories
        double ceiling = "A".equals(cat) ? 3000 : "D".equals(cat) ? 1800 : 2400;
        if (amount > ceiling && !(old && "A".equals(cat))) {
            amount = ceiling;
            txt.append("Amount limited to the ceiling of " + AmountUtil.format(ceiling) + ".\n");
        }
        double min = "C".equals(cat) ? 400 : "D".equals(cat) ? 300 : 350;
        if (!file.ownRequest && amount < min) {
            amount = min;
            txt.append("Minimum allowance granted.\n");
        }

        PaymentPlan plan = new PaymentPlan(file, amount, remote, persons);
        notice.amount = amount;
        notice.status = 1;
        notice.advance = plan.advance;
        notice.instalments = plan.count;
        notice.instalmentAmount = plan.instalment;
        notice.lastInstalment = plan.last;
        txt.append("Allowance granted: " + AmountUtil.format(amount) + ".\n");
        if (plan.advance > 0)
            txt.append("Advance paid on notification: " + AmountUtil.format(plan.advance) + ".\n");
        if (plan.count == 1) {
            txt.append(plan.advance > 0 ? "Balance paid in a single payment." : "Paid in a single payment.");
        } else {
            double inst = notice.instalmentAmount, lastInst = notice.lastInstalment;
            txt.append((plan.advance > 0 ? "Balance payable in " : "Payable in ") + plan.count + " monthly instalments of "
                    + AmountUtil.format(inst) + ", the last one being " + AmountUtil.format(lastInst) + ".");
        }
        notice.text = txt.toString();
        ledger.record(file.agentId, file.requestDate, Outposts.CODES[from]);
        return notice;
    }

    private AllowanceNotice reject(AllowanceNotice notice, StringBuffer txt, String reason) {
        notice.status = 9;
        txt.append(reason);
        notice.text = txt.toString();
        return notice;
    }
}
