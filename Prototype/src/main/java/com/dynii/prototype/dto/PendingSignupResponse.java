package com.dynii.prototype.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PendingSignupResponse {

    // Username derived from provider and providerId.
    private String username;

    // Display name from provider profile.
    private String name;

    // Email from provider profile.
    private String email;
}
