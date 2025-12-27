package com.dynii.prototype.controller;

import com.dynii.prototype.dto.*;
import com.dynii.prototype.entity.CompanyRegisteredEntity;
import com.dynii.prototype.entity.InvitationEntity;
import com.dynii.prototype.entity.SellerGradeEntity;
import com.dynii.prototype.entity.SellerEntity;
import com.dynii.prototype.entity.SellerRegisterEntity;
import com.dynii.prototype.entity.UserEntity;
import com.dynii.prototype.enums.CompanyStatus;
import com.dynii.prototype.enums.InvitationStatus;
import com.dynii.prototype.enums.MemberType;
import com.dynii.prototype.enums.SellerGrade;
import com.dynii.prototype.enums.SellerGradeStatus;
import com.dynii.prototype.enums.SellerRole;
import com.dynii.prototype.enums.SellerStatus;
import com.dynii.prototype.jwt.JWTUtil;
import com.dynii.prototype.repository.CompanyRegisteredRepository;
import com.dynii.prototype.repository.InvitationRepository;
import com.dynii.prototype.repository.RefreshRepository;
import com.dynii.prototype.repository.SellerGradeRepository;
import com.dynii.prototype.repository.SellerRepository;
import com.dynii.prototype.repository.SellerRegisterRepository;
import com.dynii.prototype.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Base64;
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

    // Repository for seller accounts.
    private final SellerRepository sellerRepository;

    // Repository for registered companies.
    private final CompanyRegisteredRepository companyRegisteredRepository;

    // Repository for seller registration submissions.
    private final SellerRegisterRepository sellerRegisterRepository;

    // Repository for seller grade assignments.
    private final SellerGradeRepository sellerGradeRepository;

    // Repository for seller invitations.
    private final InvitationRepository invitationRepository;

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
        PendingSignupResponse response = PendingSignupResponse.builder()
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .build();

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
        PhoneSendResponse response = PhoneSendResponse.builder()
                .message("verification code generated")
                .code(code)
                .build();

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
    @Transactional
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

        // Ensure seller does not already exist in DB.
        SellerEntity existSeller = sellerRepository.findByLoginId(user.getUsername());
        if (existSeller != null) {
            return new ResponseEntity<>("already signed up", HttpStatus.CONFLICT);
        }

        // Validate terms agreement.
        if (!request.isAgreeToTerms()) {
            return new ResponseEntity<>("terms agreement required", HttpStatus.BAD_REQUEST);
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

        // Member type selected for signup branching.
        // Raw member type input from request payload.
        String memberTypeRaw = trimToNull(request.getMemberType());
        if (memberTypeRaw == null) {
            return new ResponseEntity<>("member type required", HttpStatus.BAD_REQUEST);
        }

        // Parsed member type for signup branching.
        MemberType memberType = parseMemberType(memberTypeRaw);
        if (memberType == null) {
            return new ResponseEntity<>("unsupported member type", HttpStatus.BAD_REQUEST);
        }

        if (MemberType.GENERAL.equals(memberType)) {
            return completeGeneralSignup(user, request, response, session, storedPhone);
        }

        if (MemberType.SELLER.equals(memberType)) {
            return completeSellerSignup(user, request, response, session, storedPhone);
        }

        return new ResponseEntity<>("unsupported member type", HttpStatus.BAD_REQUEST);
    }

    // Handle general member signup completion.
    private ResponseEntity<?> completeGeneralSignup(
            CustomOAuth2User user,
            SocialSignupRequest request,
            HttpServletResponse response,
            HttpSession session,
            String storedPhone
    ) {
        // Build and persist the new user entity.
        UserEntity userEntity = UserEntity.builder()
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .role("ROLE_USER")
                .memberType(MemberType.GENERAL.name())
                .phoneNumber(storedPhone)
                .mbti(trimToNull(request.getMbti()))
                .job(trimToNull(request.getJob()))
                .signupCompleted(true)
                .build();

        userRepository.save(userEntity);

        // Issue tokens after successful signup.
        issueTokens(userEntity.getUsername(), userEntity.getRole(), response);

        // Clear phone verification state after completion.
        clearPhoneSession(session);

        return new ResponseEntity<>("signup completed", HttpStatus.OK);
    }

    // Handle seller signup completion with review data.
    private ResponseEntity<?> completeSellerSignup(
            CustomOAuth2User user,
            SocialSignupRequest request,
            HttpServletResponse response,
            HttpSession session,
            String storedPhone
    ) {
        // Invitation token for invited seller signup.
        String inviteToken = trimToNull(request.getInviteToken());
        if (inviteToken != null) {
            return completeInvitedSellerSignup(user, response, session, storedPhone, inviteToken);
        }

        // Business number required for seller registration.
        String businessNumber = trimToNull(request.getBusinessNumber());
        if (businessNumber == null) {
            return new ResponseEntity<>("business number required", HttpStatus.BAD_REQUEST);
        }

        // Company name required for seller registration.
        String companyName = trimToNull(request.getCompanyName());
        if (companyName == null) {
            return new ResponseEntity<>("company name required", HttpStatus.BAD_REQUEST);
        }

        // Plan file payload required for seller registration.
        String planFileBase64 = trimToNull(request.getPlanFileBase64());
        if (planFileBase64 == null) {
            return new ResponseEntity<>("plan file required", HttpStatus.BAD_REQUEST);
        }

        // Reject when the business number is already registered.
        CompanyRegisteredEntity existingCompany = companyRegisteredRepository.findByBusinessNumber(businessNumber);
        if (existingCompany != null
                && CompanyStatus.ACTIVE.name().equalsIgnoreCase(existingCompany.getCompanyStatus())) {
            return new ResponseEntity<>("business number already registered", HttpStatus.CONFLICT);
        }

        // Optional seller description for review.
        String description = trimToNull(request.getDescription());

        // Decode plan file from base64 payload.
        byte[] planFile;
        try {
            planFile = decodePlanFile(planFileBase64);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>("invalid plan file payload", HttpStatus.BAD_REQUEST);
        }

        // Timestamp used for seller-related records.
        LocalDateTime now = LocalDateTime.now();

        // Build and persist the new seller user entity.
        SellerEntity sellerEntity = SellerEntity.builder()
                .loginId(user.getUsername())
                .name(user.getName())
                .phone(storedPhone)
                .role(SellerRole.ROLE_SELLER_OWNER.name())
                .sellerStatus(SellerStatus.PENDING.name())
                .createdAt(now)
                .updatedAt(now)
                .build();

        sellerRepository.save(sellerEntity);

        // Store the registered company for duplicate checks.
        CompanyRegisteredEntity companyRegistered = CompanyRegisteredEntity.builder()
                .companyName(companyName)
                .businessNumber(businessNumber)
                .sellerId(sellerEntity.getId())
                .createdAt(now)
                .companyStatus(CompanyStatus.ACTIVE.name())
                .build();

        companyRegisteredRepository.save(companyRegistered);

        // Store seller review submission details.
        SellerRegisterEntity sellerRegister = SellerRegisterEntity.builder()
                .planFile(planFile)
                .sellerId(sellerEntity.getId())
                .description(description)
                .companyName(companyName)
                .build();

        sellerRegisterRepository.save(sellerRegister);

        // Assign initial seller grade in review status.
        SellerGradeEntity sellerGrade = SellerGradeEntity.builder()
                .grade(SellerGrade.C.name())
                .gradeStatus(SellerGradeStatus.REVIEW.name())
                .createdAt(now)
                .updatedAt(now)
                .expiredAt(now.plusYears(1))
                .companyId(companyRegistered.getId())
                .build();

        sellerGradeRepository.save(sellerGrade);

        // Issue tokens after successful signup request.
        issueTokens(sellerEntity.getLoginId(), sellerEntity.getRole(), response);

        // Clear phone verification state after completion.
        clearPhoneSession(session);

        return new ResponseEntity<>(
                "판매자 회원 가입 신청이 완료되었습니다. 관리자 승인 후에 서비스 이용이 가능합니다.",
                HttpStatus.OK
        );
    }

    // Handle invited seller signup completion with manager role.
    private ResponseEntity<?> completeInvitedSellerSignup(
            CustomOAuth2User user,
            HttpServletResponse response,
            HttpSession session,
            String storedPhone,
            String inviteToken
    ) {
        // Invitation lookup by token.
        InvitationEntity invitation = invitationRepository.findByToken(inviteToken);
        if (invitation == null) {
            return new ResponseEntity<>("invitation not found", HttpStatus.NOT_FOUND);
        }

        // Validate invitation status.
        if (!InvitationStatus.PENDING.name().equalsIgnoreCase(invitation.getStatus())) {
            return new ResponseEntity<>("invitation already used", HttpStatus.CONFLICT);
        }

        // Validate invitation expiration.
        LocalDateTime now = LocalDateTime.now();
        if (invitation.getExpiredAt() != null && invitation.getExpiredAt().isBefore(now)) {
            invitation.setStatus(InvitationStatus.EXPIRED.name());
            invitation.setUpdatedAt(now);
            invitationRepository.save(invitation);
            return new ResponseEntity<>("invitation expired", HttpStatus.GONE);
        }

        // Ensure the invitation email matches the signup email.
        String inviteEmail = trimToNull(invitation.getEmail());
        String signupEmail = trimToNull(user.getEmail());
        if (inviteEmail == null || signupEmail == null || !inviteEmail.equalsIgnoreCase(signupEmail)) {
            return new ResponseEntity<>("invitation email mismatch", HttpStatus.BAD_REQUEST);
        }

        // Ensure the invitation owner seller exists.
        SellerEntity ownerSeller = sellerRepository.findById(invitation.getSellerId()).orElse(null);
        if (ownerSeller == null) {
            return new ResponseEntity<>("invitation owner not found", HttpStatus.NOT_FOUND);
        }

        // Build and persist the new manager seller entity.
        SellerEntity sellerEntity = SellerEntity.builder()
                .loginId(user.getUsername())
                .name(user.getName())
                .phone(storedPhone)
                .role(SellerRole.ROLE_SELLER_MANAGER.name())
                .sellerStatus(SellerStatus.ACTIVE.name())
                .createdAt(now)
                .updatedAt(now)
                .build();

        sellerRepository.save(sellerEntity);

        // Mark invitation as accepted after successful signup.
        invitation.setStatus(InvitationStatus.ACCEPTED.name());
        invitation.setUpdatedAt(now);
        invitationRepository.save(invitation);

        // Issue tokens after successful invited signup.
        issueTokens(sellerEntity.getLoginId(), sellerEntity.getRole(), response);

        // Clear phone verification state after completion.
        clearPhoneSession(session);

        return new ResponseEntity<>("invited seller signup completed", HttpStatus.OK);
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

        refreshRepository.save(username, refresh, refreshExpiryMs);

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
    // Clear phone verification attributes after signup completion.
    private void clearPhoneSession(HttpSession session) {
        // Cleanup pending phone state stored in session.
        session.removeAttribute(SESSION_PHONE_NUMBER);
        session.removeAttribute(SESSION_PHONE_CODE);
        session.removeAttribute(SESSION_PHONE_VERIFIED);
    }

    // Decode base64 plan file payload, tolerating data URL prefixes.
    private byte[] decodePlanFile(String encodedPlanFile) {
        // Remove data URL prefix if present.
        int commaIndex = encodedPlanFile.indexOf(',');
        String payload = commaIndex >= 0 ? encodedPlanFile.substring(commaIndex + 1) : encodedPlanFile;
        return Base64.getDecoder().decode(payload);
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

    // Parse member type input into enum value.
    private MemberType parseMemberType(String memberType) {
        // Upper-cased member type value for enum mapping.
        String normalized = memberType == null ? null : memberType.trim().toUpperCase();
        if (normalized == null || normalized.isEmpty()) {
            return null;
        }
        try {
            return MemberType.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
