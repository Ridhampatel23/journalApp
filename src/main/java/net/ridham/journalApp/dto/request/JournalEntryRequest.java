package net.ridham.journalApp.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class JournalEntryRequest {

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 150, message = "Title cannot exceed 150 characters")
    private String title;

    @Size(max = 10000, message = "Content cannot exceed 10000 characters")
    private String content;
}