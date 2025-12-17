package com.dynii.oauthjwt.service;

import com.dynii.oauthjwt.jwt.JWTUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JWTUtil jwtUtil;

    public String reissueAccessToken(String refreshToken) {

        Claims claims = jwtUtil.parseClaims(refreshToken);
        String username = claims.getSubject();

        return jwtUtil.createAccessToken(username, "ROLE_USER");
    }
}
