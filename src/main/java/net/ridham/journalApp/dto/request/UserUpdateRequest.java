package net.ridham.journalApp.dto.request;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String userName;
    private String password;
}
