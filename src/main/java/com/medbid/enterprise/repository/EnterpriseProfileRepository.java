package com.medbid.enterprise.repository;

import com.medbid.enterprise.entity.EnterpriseProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnterpriseProfileRepository extends JpaRepository<EnterpriseProfile, UUID> {

    Page<EnterpriseProfile> findByDeletedFalse(Pageable pageable);

    Optional<EnterpriseProfile> findByTaxCode(String taxCode);

    boolean existsByTaxCode(String taxCode);
}
