package com.dynii.oauthjwt.controller;

import com.dynii.oauthjwt.service.AuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MainController {

    private final AuthService authService;

    @GetMapping("/")
    public String mainAPI() {
        return "main route";
    }

    @PostMapping("/auth/reissue")
    public ResponseEntity<?> reissue(
            @CookieValue(value = "refresh_token", required = false)
            String refreshToken) {

        if (refreshToken == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("NO_REFRESH_TOKEN");
        }

        try {
            String newAccessToken =
                    authService.reissueAccessToken(refreshToken);

            return ResponseEntity.ok(Map.of(
                    "accessToken", newAccessToken
            ));

        } catch (ExpiredJwtException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("REFRESH_TOKEN_EXPIRED");
        }
    }
}
