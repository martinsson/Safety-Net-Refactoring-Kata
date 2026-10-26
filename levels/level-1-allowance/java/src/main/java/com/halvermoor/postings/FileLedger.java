package com.halvermoor.postings;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Keeps track of the files settled since the service started.
 */
public class FileLedger {

    private static FileLedger instance = new FileLedger();
    private static int sequence = 0;
    private HashMap lastSettled = new HashMap();
    private HashMap lastFrom = new HashMap();

    public static FileLedger getInstance() { return instance; }

    public String nextReference() {
        sequence++;
        String s = "00000" + sequence;
        return "RRA-" + Calendar.getInstance().get(Calendar.YEAR) + "-" + s.substring(s.length() - 5);
    }

    public LocalDate lastSettlement(String agentId) { return (LocalDate) lastSettled.get(agentId); }

    public String lastOrigin(String agentId) { return (String) lastFrom.get(agentId); }

    public void record(String agentId, LocalDate date, String fromCode) {
        lastSettled.put(agentId, date);
        lastFrom.put(agentId, fromCode);
    }

    void clear() {
        lastSettled.clear();
        lastFrom.clear();
        sequence = 0;
    }
}
