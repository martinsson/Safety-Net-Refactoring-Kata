package com.thornfieldsoft.vegshow;

import java.time.LocalDate;

public class ScoreUtil {

    private static int certificateCounter = 0;

    // average of the judges' marks
    public static double average(int[] marks) {
        if (marks == null || marks.length == 0) return 0;
        double total = 0;
        int hi = 0;
        int lo = 10;
        for (int i = 0; i < marks.length; i++) {
            int m = marks[i] > 10 ? 10 : marks[i];
            total = total + m;
            if (m > hi) hi = m;
            if (m < lo) lo = m;
        }
        if (marks.length >= 4 && hi - lo >= 5) {
            return (total - hi - lo) / (marks.length - 2);
        }
        return total / marks.length;
    }

    // round to one decimal
    public static double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }

    public static synchronized String nextCertificate() {
        certificateCounter++;
        return "UW-" + LocalDate.now().getYear() + "-" + certificateCounter;
    }
}
