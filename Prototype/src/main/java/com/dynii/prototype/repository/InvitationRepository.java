package com.dynii.prototype.repository;

import com.dynii.prototype.entity.InvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface InvitationRepository extends JpaRepository<InvitationEntity, Long> {

    // Find invitation by token from the email link.
    InvitationEntity findByToken(String token);

    // Check for duplicate invitations for the same email.
    boolean existsByEmailAndStatusIn(String email, Collection<String> statuses);

    // Count invitations for a seller to enforce the invite limit.
    long countBySellerIdAndStatusIn(Long sellerId, Collection<String> statuses);
}
