package com.medbid.hsmt.repository;

import com.medbid.tender.entity.TenderRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HsmtRequirementRepository extends JpaRepository<TenderRequirement, UUID> {

    List<TenderRequirement> findByTenderId(UUID tenderId);

    List<TenderRequirement> findByTenderIdAndStatus(UUID tenderId, String status);

    List<TenderRequirement> findByTenderIdAndMandatory(UUID tenderId, Boolean mandatory);

    List<TenderRequirement> findBySourceDocumentId(UUID sourceDocumentId);

    long countByTenderIdAndType(UUID tenderId, String type);

    @Modifying
    @Query("UPDATE TenderRequirement r SET r.deleted = true WHERE r.tenderId = :tenderId AND r.source = :source")
    void softDeleteByTenderIdAndSource(@Param("tenderId") UUID tenderId, @Param("source") String source);
}
