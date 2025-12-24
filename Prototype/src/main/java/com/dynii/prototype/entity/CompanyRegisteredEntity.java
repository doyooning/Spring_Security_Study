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
@Table(name = "company_registered")
public class CompanyRegisteredEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long id;

    // Registered company name for seller signup verification.
    @Column(name = "company_name")
    private String companyName;

    // Business number used to validate duplicate registrations.
    @Column(name = "business_number")
    private String businessNumber;

    // Seller user id that owns the company registration.
    @Column(name = "seller_id")
    private Long sellerId;

    // Timestamp when the company was registered.
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Company status (ACTIVE/DELETED).
    @Column(name = "company_status")
    private String companyStatus;
}
