package com.dynii.prototype.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String name;

    private String email;

    private String role;

    // Phone number provided during signup flow.
    private String phoneNumber;

    // Member type selected during signup (e.g., GENERAL, SELLER).
    private String memberType;

    // Optional MBTI for general members.
    private String mbti;

    // Optional job for general members.
    private String job;

    // Flag to indicate whether signup is fully completed.
    private boolean signupCompleted;
}
