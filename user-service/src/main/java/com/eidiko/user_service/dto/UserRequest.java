package com.eidiko.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String role; // Optional, default to "USER" if not provided

}
