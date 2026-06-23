package com.medbid.enterprise.repository;

import com.medbid.enterprise.entity.LegalDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LegalDocumentRepository extends JpaRepository<LegalDocument, UUID> {

    List<LegalDocument> findByEnterpriseId(UUID enterpriseId);

    List<LegalDocument> findByEnterpriseIdAndDeletedFalse(UUID enterpriseId);

    List<LegalDocument> findByExpiryDateBeforeAndDeletedFalse(LocalDate date);

    List<LegalDocument> findByExpiryDateBetweenAndDeletedFalse(LocalDate start, LocalDate end);
}
