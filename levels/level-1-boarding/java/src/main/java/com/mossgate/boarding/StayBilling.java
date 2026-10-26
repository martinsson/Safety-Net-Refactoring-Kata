package com.mossgate.boarding;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class StayBilling {

    private static int seq = 0;
    private static final double VAT = 0.2;

    private RateBook rates;

    public StayBilling() {
        this(new RateBook());
    }

    StayBilling(RateBook rates) {
        this.rates = rates;
    }

    public Invoice invoice(String owner, String card, LocalDate checkIn, LocalDate checkOut, List<Pet> pets) {
        Invoice inv = new Invoice();
        seq++;
        boolean trade = TradeAccounts.isTrade(owner, checkIn);
        inv.reference = (trade ? "MGT" : "MG") + Calendar.getInstance().get(Calendar.YEAR) + "-" + seq;
        if (checkOut.isBefore(checkIn) || pets == null || pets.size() == 0) {
            return inv;
        }
        Calendar start = toCal(checkIn);
        int nights = daysBetween(start, toCal(checkOut));
        boolean dayCare = false;
        if (nights == 0) {
            dayCare = true;
            nights = 1;
        }
        inv.nights = nights;
        boolean vip = owner != null && owner.trim().equalsIgnoreCase("brindlecombe");
        // winter = December to February
        int month = start.get(Calendar.MONTH);
        boolean winter = month >= Calendar.NOVEMBER || month <= Calendar.FEBRUARY;
        HashMap seen = new HashMap();
        double total = 0;
        for (int i = 0; i < pets.size(); i++) {
            Pet p = pets.get(i);
            String sp = p.getSpecies().toUpperCase();
            double sub = 0;
            boolean hib = false;
            if (sp.equals("TORTOISE") && winter && nights >= 28) {
                hib = true;
                sub = 3.0 * nights;
            } else {
                double rate = rates.nightlyRate(sp, p.getSize());
                if (vip && sp.equals("PARROT")) {
                    rate = 11.0;
                }
                Calendar c = (Calendar) start.clone();
                for (int n = 0; n < nights; n++) {
                    double r = rate;
                    int dow = c.get(Calendar.DAY_OF_WEEK);
                    if ((dow == 6 || dow == 7) && !sp.equals("TORTOISE") && !vip) {
                        r = r + (sp.equals("FERRET") ? 8 : 4);
                    }
                    if (!vip) {
                        r = r * SeasonTable.factor(c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                    }
                    sub += r;
                    c.add(Calendar.DATE, 1);
                }
                if (dayCare) {
                    sub = sub * 0.6;
                }
                if (sp.equals("TORTOISE") && sub > 60) {
                    sub = 60;
                }
            }
            Integer cnt = (Integer) seen.get(sp);
            if (cnt != null) {
                sub = sub * 0.8;
            }
            seen.put(sp, cnt == null ? 1 : cnt + 1);
            if (!hib) {
                if (nights >= 14) {
                    sub = sub * 0.9;
                } else if (nights >= 7) {
                    sub = sub * 0.95;
                }
            }
            if (trade) {
                sub = sub * 0.9;
            }
            // medication is charged per pet per day
            if (p.isMedication()) {
                sub += 3.5 * nights > 40 ? 40 : 3.5 * nights;
            }
            sub = Math.round(sub * 20) / 20.0;
            String line = p.getName() + " " + sp + " " + nights + "n " + sub;
            if (hib) {
                line = line + " hibernation";
            }
            inv.lines.add(line);
            total += sub;
        }
        if (trade) {
            // trade accounts are billed at the end of the month, no card, no minimum
            inv.total = Math.round(total * 100) / 100.0;
            return inv;
        }
        if (card != null && card.length() > 0) {
            LoyaltyRegistry reg = LoyaltyRegistry.getInstance();
            if (reg.points(card) >= 100) {
                total = total * 0.95;
                reg.add(card, -100);
                inv.lines.add("Loyalty -5%");
            }
            int earned = (int) (total / 10);
            if (card.startsWith("G")) {
                earned = earned * 2;
            }
            reg.add(card, earned);
            inv.pointsEarned = earned;
        }
        if (total < 25) {
            total = 25;
        }
        inv.total = Math.round(total * 100) / 100.0;
        return inv;
    }

    // same as invoice, without registering anything
    public double estimate(String owner, String card, LocalDate checkIn, LocalDate checkOut, List<Pet> pets) {
        if (checkIn.getYear() < Calendar.getInstance().get(Calendar.YEAR) - 100)
            return 0;
        if (checkOut.isBefore(checkIn) || pets == null || pets.size() == 0)
            return -1;
        int nights = (int) (checkOut.toEpochDay() - checkIn.toEpochDay());
        if (nights == 0)
            return 15 * pets.size();
        boolean vip = owner != null && owner.trim().equalsIgnoreCase("brindlecombe");
        boolean trade = TradeAccounts.isTrade(owner, checkIn);
        int m = checkIn.getMonthValue();
        double season = (m == 7 || m == 8) ? 1.25 : 1.0;
        boolean winter = m >= 11 || m <= 2;
        double total = 0;
        ArrayList<String> species = new ArrayList<String>();
        for (int i = 0; i < pets.size(); i++) {
            Pet p = pets.get(i);
            String sp = p.getSpecies().toUpperCase();
            double sub = 0;
            boolean hib = false;
            if (sp.equals("TORTOISE") && winter && nights >= 28) {
                hib = true;
                sub = 3.0 * nights;
            } else {
                double rate = rates.nightlyRate(sp, p.getSize());
                if (vip && sp.equals("PARROT")) {
                    rate = 11.0;
                }
                for (int n = 0; n < nights; n++) {
                    double r = rate;
                    int dow = checkIn.plusDays(n).getDayOfWeek().getValue();
                    if ((dow == 5 || dow == 6) && !sp.equals("TORTOISE") && !vip) {
                        r = r + (sp.equals("FERRET") ? 8 : 4);
                    }
                    sub += r;
                }
                if (!vip) {
                    sub = sub * season;
                }
                if (sp.equals("TORTOISE") && sub > 60) {
                    sub = 60;
                }
            }
            if (species.contains(sp)) {
                sub = sub * 0.8;
            } else {
                species.add(sp);
            }
            if (!hib) {
                if (nights >= 14) {
                    sub = sub * 0.9;
                } else if (nights >= 7) {
                    sub = sub * 0.95;
                }
            }
            if (trade) {
                sub = sub * 0.9;
            }
            if (p.isMedication()) {
                sub += Math.min(3.5 * nights, 40);
            }
            total += sub;
        }
        if (trade)
            return Math.ceil(total);
        if (card != null && !card.equals("") && LoyaltyRegistry.getInstance().points(card) > 100)
            total = total * 0.95;
        if (total < 25)
            total = 25;
        return Math.ceil(total);
    }

    static Calendar toCal(LocalDate d) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.clear();
        c.set(d.getYear(), d.getMonthValue() - 1, d.getDayOfMonth());
        return c;
    }

    static int daysBetween(Calendar from, Calendar to) {
        return (int) ((to.getTimeInMillis() - from.getTimeInMillis()) / (1000 * 60 * 60 * 24));
    }
}
