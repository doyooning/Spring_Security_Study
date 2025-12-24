package com.dynii.prototype.repository;

import com.dynii.prototype.entity.SellerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerRepository extends JpaRepository<SellerEntity, Long> {

    // Lookup seller by social login id.
    SellerEntity findByLoginId(String loginId);
}
