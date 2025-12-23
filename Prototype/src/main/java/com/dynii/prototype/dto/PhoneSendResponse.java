package com.dynii.prototype.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhoneSendResponse {

    // Response message for the phone send request.
    private String message;

    // Development-only code echo.
    private String code;
}
