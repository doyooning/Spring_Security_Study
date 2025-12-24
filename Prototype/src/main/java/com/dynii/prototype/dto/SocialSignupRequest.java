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

    // Seller business registration number.
    private String businessNumber;

    // Seller company name for review.
    private String companyName;

    // Optional seller description for review.
    private String description;

    // Base64-encoded plan file payload for seller review.
    private String planFileBase64;

    // Terms agreement flag.
    private boolean agreeToTerms;
}
