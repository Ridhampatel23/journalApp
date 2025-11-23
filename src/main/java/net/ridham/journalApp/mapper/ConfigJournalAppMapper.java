package net.ridham.journalApp.mapper;

import net.ridham.journalApp.dto.response.ConfigJournalAppResponseDTO;
import net.ridham.journalApp.entity.ConfigJournalAppEntity;

public class ConfigJournalAppMapper {

    public static ConfigJournalAppResponseDTO toDTO(ConfigJournalAppEntity entity) {
        if (entity == null) return null;
        ConfigJournalAppResponseDTO dto = new ConfigJournalAppResponseDTO();
        dto.setKey(entity.getKey());
        dto.setValue(entity.getValue());
        return dto;
    }
}
