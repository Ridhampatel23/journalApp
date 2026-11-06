package net.ridham.journalApp.dto.request;

import lombok.Data;


@Data
public class JournalEntryUpdateRequest {
    private String title;
    private String content;
}
