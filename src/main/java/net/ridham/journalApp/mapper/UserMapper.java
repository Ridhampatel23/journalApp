package net.ridham.journalApp.mapper;

import net.ridham.journalApp.dto.request.UserRegistrationRequest;
import net.ridham.journalApp.dto.request.UserUpdateRequest;
import net.ridham.journalApp.dto.response.UserResponseDTO;
import net.ridham.journalApp.entity.UserEntity;
import org.bson.types.ObjectId;

public class UserMapper {

    public static UserResponseDTO toDTO(UserEntity entity) {
        if (entity == null) return null;

        UserResponseDTO dto = new UserResponseDTO();
        ObjectId id = entity.getId();
        dto.setId(id != null ? id.toHexString() : null);
        dto.setUserName(entity.getUserName());
        dto.setEmail(entity.getEmail());

        return dto;
    }

    public static UserEntity fromRegistration(UserRegistrationRequest req) {
        if (req == null) return null;
        UserEntity user = new UserEntity();
        user.setUserName(req.getUserName());
        user.setEmail(req.getEmail());
        user.setPassword(req.getPassword()); // raw, will be encoded in service
        return user;
    }

    public static void applyUpdate(UserUpdateRequest req, UserEntity user) {
        if (req == null || user == null) return;

        if (req.getUserName() != null && !req.getUserName().isEmpty()) {
            user.setUserName(req.getUserName());
        }
        if (req.getPassword() != null && !req.getPassword().isEmpty()) {
            user.setPassword(req.getPassword()); // raw, service encodes when saving
        }
    }
}
