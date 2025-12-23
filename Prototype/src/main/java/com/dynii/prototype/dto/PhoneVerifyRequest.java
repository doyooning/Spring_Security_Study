package com.dynii.prototype.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhoneVerifyRequest {

    // Phone number tied to the verification code.
    private String phoneNumber;

    // Verification code entered by the user.
    private String code;
}
