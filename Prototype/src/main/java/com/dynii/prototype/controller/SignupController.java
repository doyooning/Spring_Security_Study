package com.dynii.prototype.controller;

import com.dynii.prototype.dto.*;
import com.dynii.prototype.entity.RefreshEntity;
import com.dynii.prototype.entity.UserEntity;
import com.dynii.prototype.jwt.JWTUtil;
import com.dynii.prototype.repository.RefreshRepository;
import com.dynii.prototype.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/signup/social")
public class SignupController {

    // Session key for phone number awaiting verification.
    private static final String SESSION_PHONE_NUMBER = "pendingPhoneNumber";

    // Session key for verification code.
    private static final String SESSION_PHONE_CODE = "pendingPhoneCode";

    // Session key for verification completion flag.
    private static final String SESSION_PHONE_VERIFIED = "pendingPhoneVerified";

    // Repository for persisting user records.
    private final UserRepository userRepository;

    // Utility for creating JWT tokens.
    private final JWTUtil jwtUtil;

    // Repository for refresh token persistence.
    private final RefreshRepository refreshRepository;

    // Provide pending signup info to the frontend after social login.
    @GetMapping("/pending")
    public ResponseEntity<?> pending(
            @AuthenticationPrincipal CustomOAuth2User user
    ) {
        // Reject if unauthenticated.
        if (user == null) {
            return new ResponseEntity<>("unauthorized", HttpStatus.UNAUTHORIZED);
        }

        // Reject if signup is already completed.
        if (!user.isNewUser()) {
            return new ResponseEntity<>("already signed up", HttpStatus.CONFLICT);
        }

        // Response payload to prefill signup form.
        PendingSignupResponse response = new PendingSignupResponse();
        response.setUsername(user.getUsername());
        response.setName(user.getName());
        response.setEmail(user.getEmail());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Send a verification code for phone validation.
    @PostMapping("/phone/send")
    public ResponseEntity<?> sendPhoneCode(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestBody PhoneSendRequest request,
            HttpSession session
    ) {
        // Reject if unauthenticated.
        if (user == null) {
            return new ResponseEntity<>("unauthorized", HttpStatus.UNAUTHORIZED);
        }

        // Reject if signup is already completed.
        if (!user.isNewUser()) {
            return new ResponseEntity<>("already signed up", HttpStatus.CONFLICT);
        }

        // Phone number from the request payload.
        String phoneNumber = request.getPhoneNumber();
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return new ResponseEntity<>("phone number required", HttpStatus.BAD_REQUEST);
        }

        // Generate a 6-digit verification code for development.
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(100000, 1000000));

        // Persist phone verification state in session.
        session.setAttribute(SESSION_PHONE_NUMBER, phoneNumber);
        session.setAttribute(SESSION_PHONE_CODE, code);
        session.setAttribute(SESSION_PHONE_VERIFIED, false);

        // Response payload containing dev-only code.
        PhoneSendResponse response = new PhoneSendResponse();
        response.setMessage("verification code generated");
        response.setCode(code);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Verify phone code submitted by the user.
    @PostMapping("/phone/verify")
    public ResponseEntity<?> verifyPhoneCode(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestBody PhoneVerifyRequest request,
            HttpSession session
    ) {
        // Reject if unauthenticated.
        if (user == null) {
            return new ResponseEntity<>("unauthorized", HttpStatus.UNAUTHORIZED);
        }

        // Reject if signup is already completed.
        if (!user.isNewUser()) {
            return new ResponseEntity<>("already signed up", HttpStatus.CONFLICT);
        }

        // Phone number and code from the request payload.
        String phoneNumber = request.getPhoneNumber();
        String code = request.getCode();

        // Session-stored verification state.
        String storedPhone = (String) session.getAttribute(SESSION_PHONE_NUMBER);
        String storedCode = (String) session.getAttribute(SESSION_PHONE_CODE);

        if (!Objects.equals(phoneNumber, storedPhone) || !Objects.equals(code, storedCode)) {
            return new ResponseEntity<>("verification failed", HttpStatus.BAD_REQUEST);
        }

        session.setAttribute(SESSION_PHONE_VERIFIED, true);

        return new ResponseEntity<>("verified", HttpStatus.OK);
    }

    // Complete signup for general members after phone verification.
    @PostMapping("/complete")
    public ResponseEntity<?> completeSignup(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestBody SocialSignupRequest request,
            HttpServletResponse response,
            HttpSession session
    ) {
        // Reject if unauthenticated.
        if (user == null) {
            return new ResponseEntity<>("unauthorized", HttpStatus.UNAUTHORIZED);
        }

        // Reject if signup is already completed.
        if (!user.isNewUser()) {
            return new ResponseEntity<>("already signed up", HttpStatus.CONFLICT);
        }

        // Ensure user does not already exist in DB.
        UserEntity existData = userRepository.findByUsername(user.getUsername());
        if (existData != null) {
            return new ResponseEntity<>("already signed up", HttpStatus.CONFLICT);
        }

        // Validate terms agreement.
        if (!request.isAgreeToTerms()) {
            return new ResponseEntity<>("terms agreement required", HttpStatus.BAD_REQUEST);
        }

        // Validate member type (general only for now).
        if (!"GENERAL".equalsIgnoreCase(request.getMemberType())) {
            return new ResponseEntity<>("only GENERAL signup is supported", HttpStatus.BAD_REQUEST);
        }

        // Ensure phone verification completed.
        Boolean verified = (Boolean) session.getAttribute(SESSION_PHONE_VERIFIED);
        if (verified == null || !verified) {
            return new ResponseEntity<>("phone verification required", HttpStatus.BAD_REQUEST);
        }

        // Validate verified phone number matches request.
        String storedPhone = (String) session.getAttribute(SESSION_PHONE_NUMBER);
        if (!Objects.equals(storedPhone, request.getPhoneNumber())) {
            return new ResponseEntity<>("phone number mismatch", HttpStatus.BAD_REQUEST);
        }

        // Build and persist the new user entity.
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(user.getUsername());
        userEntity.setName(user.getName());
        userEntity.setEmail(user.getEmail());
        userEntity.setRole("ROLE_USER");
        userEntity.setMemberType("GENERAL");
        userEntity.setPhoneNumber(storedPhone);
        userEntity.setMbti(trimToNull(request.getMbti()));
        userEntity.setJob(trimToNull(request.getJob()));
        userEntity.setSignupCompleted(true);

        userRepository.save(userEntity);

        // Issue tokens after successful signup.
        issueTokens(userEntity.getUsername(), userEntity.getRole(), response);

        // Clear session attributes after completion.
        session.removeAttribute(SESSION_PHONE_NUMBER);
        session.removeAttribute(SESSION_PHONE_CODE);
        session.removeAttribute(SESSION_PHONE_VERIFIED);

        return new ResponseEntity<>("signup completed", HttpStatus.OK);
    }

    // Issue access/refresh tokens and persist refresh token.
    private void issueTokens(String username, String role, HttpServletResponse response) {
        // Access token expiry in milliseconds.
        long accessExpiryMs = 600000L;

        // Refresh token expiry in milliseconds.
        long refreshExpiryMs = 86400000L;

        // Create tokens for the signed-up user.
        String access = jwtUtil.createJwt("access", username, role, accessExpiryMs); // Access token for header.
        String refresh = jwtUtil.createJwt("refresh", username, role, refreshExpiryMs); // Refresh token for cookie.

        addRefreshEntity(username, refresh, refreshExpiryMs);

        response.setHeader("access", access);
        response.addCookie(createCookie("refresh", refresh, Math.toIntExact(refreshExpiryMs / 1000)));
    }

    // Create a refresh cookie with HttpOnly flag.
    private Cookie createCookie(String key, String value, int maxAge) {
        // Cookie for refresh token storage.
        Cookie cookie = new Cookie(key, value);

        cookie.setMaxAge(maxAge);
        // cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }

    // Persist refresh token record for reissue workflow.
    private void addRefreshEntity(String username, String refresh, Long expiredMs) {
        // Expiration date derived from refresh expiry.
        Date date = new Date(System.currentTimeMillis() + expiredMs);

        // Refresh token entity to store in DB.
        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUsername(username);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(date.toString());

        refreshRepository.save(refreshEntity);
    }

    // Normalize optional text input to null when blank.
    private String trimToNull(String value) {
        // Trimmed value from optional input.
        String trimmed = value == null ? null : value.trim();
        if (trimmed == null || trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }
}
