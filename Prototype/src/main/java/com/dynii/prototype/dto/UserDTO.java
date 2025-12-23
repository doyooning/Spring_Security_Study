package com.dynii.prototype.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    private String role;
    private String name;
    private String username;

    // Email from the social provider.
    private String email;

    // Flag to indicate social user needs extra signup data.
    private boolean newUser;
}
