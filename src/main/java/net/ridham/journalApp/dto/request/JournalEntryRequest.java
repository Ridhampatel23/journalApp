package net.ridham.journalApp.dto.request;

import lombok.Data;

@Data
public class JournalEntryRequest {
    private String title;
    private String content;
}
