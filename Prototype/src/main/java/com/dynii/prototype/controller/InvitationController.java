package com.dynii.prototype.controller;

import com.dynii.prototype.dto.CustomOAuth2User;
import com.dynii.prototype.entity.InvitationEntity;
import com.dynii.prototype.entity.SellerEntity;
import com.dynii.prototype.entity.UserEntity;
import com.dynii.prototype.repository.InvitationRepository;
import com.dynii.prototype.repository.SellerRepository;
import com.dynii.prototype.repository.UserRepository;
import com.dynii.prototype.service.InviteEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/invitations")
public class InvitationController {

    // Owner role allowed to send invitations.
    private static final String ROLE_SELLER_OWNER = "ROLE_SELLER_OWNER";

    // Invitation status for pending invites.
    private static final String INVITATION_STATUS_PENDING = "PENDING";

    // Invitation status for accepted invites.
    private static final String INVITATION_STATUS_ACCEPTED = "ACCEPTED";

    // Invitation status for expired invites.
    private static final String INVITATION_STATUS_EXPIRED = "EXPIRED";

    // Maximum number of invitations allowed per owner.
    private static final int INVITATION_LIMIT = 2;

    // Invitation expiration window in hours.
    private static final long INVITATION_EXPIRY_HOURS = 24L;

    // Repository for invitation persistence.
    private final InvitationRepository invitationRepository;

    // Repository for seller accounts.
    private final SellerRepository sellerRepository;

    // Repository for user accounts to prevent duplicate invitations.
    private final UserRepository userRepository;

    // Email sender for invitation links.
    private final InviteEmailService inviteEmailService;

    // Create a new seller invitation and email the link.
    @PostMapping
    @Transactional
    public ResponseEntity<?> inviteSeller(
            @AuthenticationPrincipal CustomOAuth2User user,
            @RequestBody Map<String, String> payload
    ) {
        // Reject if unauthenticated.
        if (user == null) {
            return new ResponseEntity<>("unauthorized", HttpStatus.UNAUTHORIZED);
        }

        // Extract current user role for authorization.
        String role = user.getAuthorities().iterator().next().getAuthority();
        if (!ROLE_SELLER_OWNER.equals(role)) {
            return new ResponseEntity<>("owner role required", HttpStatus.FORBIDDEN);
        }

        // Extract and validate invite email.
        String email = trimToNull(payload.get("email"));
        if (email == null) {
            return new ResponseEntity<>("email required", HttpStatus.BAD_REQUEST);
        }

        // Reject duplicate invitations for the same email.
        if (invitationRepository.existsByEmailAndStatusIn(
                email,
                List.of(INVITATION_STATUS_PENDING, INVITATION_STATUS_ACCEPTED))
        ) {
            return new ResponseEntity<>("duplicate invitation", HttpStatus.CONFLICT);
        }

        // Reject if the email already belongs to a registered user.
        UserEntity existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            return new ResponseEntity<>("email already registered", HttpStatus.CONFLICT);
        }

        // Lookup owner seller record for invitation linkage.
        SellerEntity ownerSeller = sellerRepository.findByLoginId(user.getUsername());
        if (ownerSeller == null) {
            return new ResponseEntity<>("owner seller not found", HttpStatus.NOT_FOUND);
        }

        // Enforce invitation limit per owner seller.
        long inviteCount = invitationRepository.countBySellerIdAndStatusIn(
                ownerSeller.getId(),
                List.of(INVITATION_STATUS_PENDING, INVITATION_STATUS_ACCEPTED)
        );
        if (inviteCount >= INVITATION_LIMIT) {
            return new ResponseEntity<>("invitation limit reached", HttpStatus.CONFLICT);
        }

        // Timestamp for invitation creation.
        LocalDateTime now = LocalDateTime.now();

        // Compute invitation expiry.
        LocalDateTime expiresAt = now.plusHours(INVITATION_EXPIRY_HOURS);

        // Token format includes UUID and expiry epoch seconds.
        String token = UUID.randomUUID() + "-" + expiresAt.toEpochSecond(ZoneOffset.UTC);

        // Invitation record for persistence.
        InvitationEntity invitation = new InvitationEntity();
        invitation.setEmail(email);
        invitation.setCreatedAt(now);
        invitation.setUpdatedAt(now);
        invitation.setExpiredAt(expiresAt);
        invitation.setStatus(INVITATION_STATUS_PENDING);
        invitation.setToken(token);
        invitation.setSellerId(ownerSeller.getId());

        invitationRepository.save(invitation);

        // Build invite URL for the signup page.
        String inviteUrl = "http://localhost:5173/signup?invite=" +
                URLEncoder.encode(token, StandardCharsets.UTF_8);

        try {
            inviteEmailService.sendInviteMail(email, inviteUrl);
        } catch (IOException ex) {
            throw new IllegalStateException("invite email send failed", ex);
        }

        return new ResponseEntity<>("invitation sent", HttpStatus.OK);
    }

    // Validate an invitation token before signup.
    @GetMapping("/validate")
    @Transactional
    public ResponseEntity<?> validateInvitation(@RequestParam("token") String token) {
        // Normalize and validate token input.
        String normalizedToken = trimToNull(token);
        if (normalizedToken == null) {
            return new ResponseEntity<>("token required", HttpStatus.BAD_REQUEST);
        }

        // Lookup invitation by token.
        InvitationEntity invitation = invitationRepository.findByToken(normalizedToken);
        if (invitation == null) {
            return new ResponseEntity<>("invitation not found", HttpStatus.NOT_FOUND);
        }

        // Return conflict for already used invitations.
        if (!INVITATION_STATUS_PENDING.equalsIgnoreCase(invitation.getStatus())) {
            return new ResponseEntity<>("invitation already used", HttpStatus.CONFLICT);
        }

        // Mark expired invitations and report error.
        LocalDateTime now = LocalDateTime.now();
        if (invitation.getExpiredAt() != null && invitation.getExpiredAt().isBefore(now)) {
            invitation.setStatus(INVITATION_STATUS_EXPIRED);
            invitation.setUpdatedAt(now);
            invitationRepository.save(invitation);
            return new ResponseEntity<>("invitation expired", HttpStatus.GONE);
        }

        // Response payload for valid invitation.
        Map<String, String> payload = Map.of(
                "email", invitation.getEmail(),
                "expiresAt", invitation.getExpiredAt().toString()
        );

        return new ResponseEntity<>(payload, HttpStatus.OK);
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
