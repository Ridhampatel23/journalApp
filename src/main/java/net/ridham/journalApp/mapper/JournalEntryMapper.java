package net.ridham.journalApp.mapper;

import net.ridham.journalApp.dto.request.JournalEntryRequest;
import net.ridham.journalApp.dto.response.JournalEntryResponseDTO;
import net.ridham.journalApp.entity.JournalEntry;
import net.ridham.journalApp.enums.Sentiment;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public class JournalEntryMapper {

    public static JournalEntryResponseDTO toDTO(JournalEntry entity) {
        if (entity == null) return null;
        JournalEntryResponseDTO dto = new JournalEntryResponseDTO();

        ObjectId id = entity.getId();
        dto.setId(id != null ? id.toHexString() : null);

        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setDate(entity.getDate());
        dto.setSentiment(entity.getSentiment() != null ? entity.getSentiment().name() : null);

        return dto;
    }

    public static JournalEntry fromRequest(JournalEntryRequest request) {
        if (request == null) return null;

        JournalEntry entry = new JournalEntry();
        // id left null so Mongo generates it
        entry.setTitle(request.getTitle());
        entry.setContent(request.getContent());
        entry.setDate(LocalDateTime.now());
        // you can change default here if you want
        entry.setSentiment(Sentiment.HAPPY);

        return entry;
    }

    public static void applyUpdate(JournalEntryRequest request, JournalEntry existing) {
        if (request == null || existing == null) return;

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            existing.setTitle(request.getTitle());
        }
        if (request.getContent() != null && !request.getContent().isEmpty()) {
            existing.setContent(request.getContent());
        }
    }
}
