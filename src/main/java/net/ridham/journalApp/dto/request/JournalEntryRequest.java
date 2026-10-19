package net.ridham.journalApp.dto.request;

import lombok.Data;
import net.ridham.journalApp.enums.Sentiment;

import javax.validation.constraints.NotNull;

@Data
public class JournalEntryRequest {
    private String title;
    private String content;

    @NotNull(message = "Sentiment is required")
    private Sentiment sentiment;
}
