package net.ridham.journalApp.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JournalEntryResponseDTO {
    private String id;
    private String title;
    private String content;
    private LocalDateTime date;
    private String sentiment;
}
