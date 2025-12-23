package com.dynii.prototype.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialSignupRequest {

    // Member type selected during signup.
    private String memberType;

    // Verified phone number.
    private String phoneNumber;

    // Optional MBTI for general members.
    private String mbti;

    // Optional job for general members.
    private String job;

    // Terms agreement flag.
    private boolean agreeToTerms;
}
