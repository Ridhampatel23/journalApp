package net.ridham.journalApp.mapper;

import net.ridham.journalApp.dto.request.JournalEntryRequest;
import net.ridham.journalApp.dto.request.JournalEntryUpdateRequest;
import net.ridham.journalApp.dto.response.JournalEntryResponseDTO;
import net.ridham.journalApp.entity.JournalEntry;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.Objects;

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

        Objects.requireNonNull(request, "Journal request cannot be null");

        JournalEntry entry = new JournalEntry();
        entry.setTitle(request.getTitle());
        entry.setContent(request.getContent());
        entry.setDate(LocalDateTime.now());

        return entry;
    }

    public static void applyUpdate(JournalEntryUpdateRequest request, JournalEntry existing) {
        if (request == null || existing == null) return;

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            existing.setTitle(request.getTitle());
        }
        if (request.getContent() != null && !request.getContent().isEmpty()) {
            existing.setContent(request.getContent());
        }
    }
}
