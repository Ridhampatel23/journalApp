package net.ridham.journalApp.dto.request;

import lombok.Data;

@Data
public class UserRegistrationRequest {
    private String userName;
    private String password;
    private String email;
}
