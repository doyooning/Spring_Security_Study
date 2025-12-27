package com.dynii.prototype.repository;

import com.dynii.prototype.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // Lookup user by social login username.
    UserEntity findByUsername(String username);

    // Lookup user by email to prevent duplicate invitations.
    UserEntity findByEmail(String email);
}
