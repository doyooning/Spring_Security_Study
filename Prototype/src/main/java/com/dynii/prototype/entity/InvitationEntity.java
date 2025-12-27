package com.dynii.prototype.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "invitation")
public class InvitationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitation_id")
    private Long id;

    // Email address of the invited seller.
    @Column(name = "email")
    private String email;

    // Timestamp when the invitation was created.
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Timestamp when the invitation expires.
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    // Timestamp when the invitation was last updated.
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Invitation status (PENDING/ACCEPTED/EXPIRED).
    @Column(name = "status")
    private String status;

    // Token used in the invitation link.
    @Column(name = "token")
    private String token;

    // Owner seller id who issued the invitation.
    @Column(name = "seller_id")
    private Long sellerId;
}
