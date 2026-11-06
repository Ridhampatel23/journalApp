package net.ridham.journalApp.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.ridham.journalApp.enums.Sentiment;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/// These classes are called POJO classes : Plain Old Java Objects
@Document(collection = "journal_entries")
@CompoundIndex(
        name = "user_date_idx",
        def = "{'userId': 1, 'date': -1}"
)
@Data
@NoArgsConstructor
public class JournalEntry {

    @Id
    private ObjectId id;

    @NonNull
    private String title;

    private ObjectId userId;

    private String content;
    private LocalDateTime date;
    private Sentiment sentiment;
}