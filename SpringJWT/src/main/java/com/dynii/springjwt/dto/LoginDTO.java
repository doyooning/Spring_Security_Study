package com.dynii.springjwt.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {
    // JSON 형태의 로그인 요청을 받을 LoginDTO
    private String username;
    private String password;
}
