package com.halvermoor.postings;

import java.time.LocalDate;

/**
 * Transfer file as keyed in by the posting office.
 */
public class TransferFile {
    public String agentId;
    public String category;
    public int yearsOfService;
    public String fromCode;
    public String toCode;
    public boolean partner;
    public int children;
    public boolean ownRequest;
    public int yearsAtPreviousPost;
    public LocalDate requestDate;
}
