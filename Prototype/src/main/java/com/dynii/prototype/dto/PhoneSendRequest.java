package com.dynii.prototype.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhoneSendRequest {

    // Phone number to receive verification code.
    private String phoneNumber;
}
