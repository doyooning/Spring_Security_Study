package com.dynii.prototype.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDTO {

    private String role;
    private String name;
    private String username;

    // Email from the social provider.
    private String email;

    // Flag to indicate social user needs extra signup data.
    private boolean newUser;
}
