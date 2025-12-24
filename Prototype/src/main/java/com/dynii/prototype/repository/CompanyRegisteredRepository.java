package com.dynii.prototype.repository;

import com.dynii.prototype.entity.CompanyRegisteredEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRegisteredRepository extends JpaRepository<CompanyRegisteredEntity, Long> {

    // Lookup for validating duplicate business numbers.
    CompanyRegisteredEntity findByBusinessNumber(String businessNumber);
}
