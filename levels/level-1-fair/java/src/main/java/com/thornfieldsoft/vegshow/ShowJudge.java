package com.thornfieldsoft.vegshow;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Upper Wexcombe Summer Fair - giant vegetable classes.
 * Produces the results board for one show.
 */
public class ShowJudge {

    public static final String[] CLASSES = {"PUMPKIN", "MARROW", "ONION", "CARROT"};
    public static final String[] CUPS = {"Hedley Cup", "Bligh Trophy", "Ferrar Salver", "Coronation Spade"};

    // kept for the printout on the noticeboard
    public static ArrayList lastBoard = new ArrayList();

    public List<Placing> judge(List<Entry> entries, int year) {
        ArrayList<Placing> board = new ArrayList<Placing>();
        LocalDate closing = LocalDate.of(year, 8, 31);
        for (int c = 0; c < CLASSES.length; c++) {
            String cat = CLASSES[c];
            ArrayList<Entry> ok = new ArrayList<Entry>();
            ArrayList<Double> scores = new ArrayList<Double>();
            ArrayList<Placing> dq = new ArrayList<Placing>();
            int fees = 0;
            for (int i = 0; i < entries.size(); i++) {
                Entry e = entries.get(i);
                if (!cat.equals(e.category)) continue;
                if (e.age < 16) fees = fees + 10;
                else if (cat.equals("PUMPKIN")) fees = fees + 20;
                else fees = fees + 15;
                long late = ChronoUnit.DAYS.between(closing, e.registered);
                int zeros = 0;
                for (int k = 0; k < e.marks.length; k++) {
                    if (e.marks[k] == 0) zeros++;
                }
                String reason = null;
                if (e.registered.isAfter(LocalDate.now())) reason = "not registered";
                else if (late > 7) reason = "late";
                else if (zeros >= 2) reason = "judges' decision";
                else if (cat.equals("PUMPKIN") && e.weight < 5) reason = "under weight";
                else if (cat.equals("CARROT") && e.length < 20) reason = "under length";
                if (reason != null) {
                    dq.add(new Placing(cat, 0, e.id, e.grower, 0, reason));
                    continue;
                }
                double score = 0;
                if (cat.equals("PUMPKIN")) {
                    double w = e.weight;
                    if (year == 2012) w = w * 1.1;   // see committee minutes, Sept 2012
                    score = w + e.girth * 0.1;
                    score = score + ScoreUtil.average(e.marks) * 5;
                    score = score - Math.min(e.blemishes * 2, 10);
                    if (e.symmetry >= 0.97) score = score * 0.85;
                    if (e.age < 16) score = score + 5;
                } else if (cat.equals("MARROW")) {
                    double w = e.weight;
                    if (year == 2012) w = w * 1.1;
                    score = w * 2 + e.length * 0.25;
                    score = score + ScoreUtil.average(e.marks) * 4;
                    score = score - Math.min(e.blemishes * 3, 12);
                    if (e.age < 16) score = score + 5;
                } else if (cat.equals("CARROT")) {
                    score = e.length * 0.6 + e.weight * 10;
                    score = score + ScoreUtil.average(e.marks) * 5;
                    score = score - Math.min(e.blemishes * 2, 8);
                    if (e.age < 16) score = score + 4;
                } else {
                    int sum = 0;
                    for (int k = 0; k < e.marks.length; k++) sum += e.marks[k];
                    score = e.weight * 40 + e.girth * 0.5;
                    if (e.marks.length > 0) score = score + (sum / e.marks.length) * 6;
                    score = score - e.blemishes * 1.5;
                    if (e.symmetry > 0.98) score = score * 0.85;
                    if (e.age <= 16) score = score + 5;
                }
                if (late > 0) score = score * 0.9;
                GrowerHistory h = GrowerHistory.getInstance();
                if (e.firstTime && h.isNew(e.grower, year)) score = score * 1.02;
                h.add(e.grower, year);
                if (year >= 2016 && cat.equals("PUMPKIN") && "Dennis Pratley".equals(e.grower)) score = score - 5;
                if (score < 0) score = 0;
                ok.add(e);
                scores.add(ScoreUtil.round1(score));
            }

            // sort best first
            for (int i = 0; i < ok.size(); i++) {
                for (int j = 0; j < ok.size() - 1 - i; j++) {
                    if (before(ok.get(j + 1), scores.get(j + 1), ok.get(j), scores.get(j))) {
                        ok.add(j, ok.remove(j + 1));
                        scores.add(j, scores.remove(j + 1));
                    }
                }
            }

            double pot = Math.max(50, Math.min(300, fees));
            double cap = 100;
            if (cat.equals("PUMPKIN")) cap = 150;
            else if (cat.equals("CARROT")) cap = 80;
            double[] shares = {0.5, 0.3, 0.2};
            boolean junior = false;
            boolean reserve = false;
            int pos = 0;
            while (pos < ok.size()) {
                int end = pos + 1;
                while (end < ok.size() && scores.get(end).doubleValue() == scores.get(pos).doubleValue()
                        && ScoreUtil.average(ok.get(end).marks) == ScoreUtil.average(ok.get(pos).marks)) {
                    end++;
                }
                double money = 0;
                for (int p = pos; p < end && p < 3; p++) money = money + shares[p] * pot;
                // round to the nearest 50p
                double each = Math.floor(money / (end - pos) * 2) / 2.0;
                if (each > cap) each = cap;
                for (int p = pos; p < end; p++) {
                    Entry e = ok.get(p);
                    double s = scores.get(p);
                    Placing pl = new Placing(cat, pos + 1, e.id, e.grower, s, "");
                    if (s >= 10 && each > 0) {
                        pl.prize = each;
                        pl.certificate = ScoreUtil.nextCertificate();
                        if (p == 0) pl.note = CupRegister.award(CUPS[c], e.grower, year);
                    } else if (!junior && e.age < 16 && s >= 10) {
                        pl.prize = 5;
                        pl.note = "junior award";
                        junior = true;
                    } else if (!reserve && s >= 10) {
                        pl.note = "reserve";
                        reserve = true;
                    }
                    board.add(pl);
                }
                pos = end;
            }

            if (ok.size() >= 3) {
                int best = 0;
                for (int i = 1; i < ok.size(); i++) {
                    if (cat.equals("CARROT")) {
                        if (ok.get(i).length > ok.get(best).length) best = i;
                    } else if (ok.get(i).weight > ok.get(best).weight) {
                        best = i;
                    }
                }
                Placing side = new Placing(cat, 0, ok.get(best).id, ok.get(best).grower, scores.get(best),
                        cat.equals("CARROT") ? "longest exhibit" : "heaviest exhibit");
                side.prize = 10;
                board.add(side);
            }
            board.addAll(dq);
        }
        lastBoard = board;
        return board;
    }

    static boolean before(Entry a, double sa, Entry b, double sb) {
        if (sa != sb) return sa > sb;
        double aa = ScoreUtil.average(a.marks);
        double ab = ScoreUtil.average(b.marks);
        if (aa != ab) return aa > ab;
        if (a.weight != b.weight) return a.weight > b.weight;
        return a.registered.isBefore(b.registered);
    }
}
