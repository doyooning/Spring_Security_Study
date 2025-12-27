package com.dynii.prototype.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "seller")
public class SellerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_id")
    private Long id;

    // Seller status (PENDING/ACTIVE).
    @Column(name = "seller_status")
    private String sellerStatus;

    // Seller creation timestamp.
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Seller update timestamp.
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Seller display name from social provider.
    @Column(name = "name")
    private String name;

    // Social login identifier for the seller.
    @Column(name = "login_id")
    private String loginId;

    // Verified phone number for the seller.
    @Column(name = "phone")
    private String phone;

    // Seller role (OWNER/MANAGER or pending role).
    @Column(name = "role")
    private String role;
}
