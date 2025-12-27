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
@Table(name = "seller_grade")
public class SellerGradeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grade_id")
    private Long id;

    // Seller grade value (A/B/C).
    @Column(name = "grade")
    private String grade;

    // Seller grade status (ACTIVE/TEMP/REVIEW).
    @Column(name = "grade_status")
    private String gradeStatus;

    // Grade creation timestamp.
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Grade update timestamp.
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Grade expiration timestamp.
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    // Company id linked to this grade.
    @Column(name = "company_id")
    private Long companyId;
}
