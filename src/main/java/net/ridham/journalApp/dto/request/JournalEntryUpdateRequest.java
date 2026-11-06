package net.ridham.journalApp.dto.request;

import lombok.Data;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


@Data
public class JournalEntryUpdateRequest {

    @Pattern(
            regexp = "(?s).*\\S.*",
            message = "Title cannot be blank"
    )
    @Size(max = 150)
    private String title;

    @Size(max = 10000)
    private String content;
}