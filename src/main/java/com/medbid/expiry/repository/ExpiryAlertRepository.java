package com.medbid.expiry.repository;

import com.medbid.expiry.entity.ExpiryAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ExpiryAlertRepository extends JpaRepository<ExpiryAlert, UUID> {

    Page<ExpiryAlert> findByIsReadFalseAndIsDismissedFalseOrderByCreatedAtDesc(Pageable pageable);

    Page<ExpiryAlert> findBySeverityAndIsReadFalseAndIsDismissedFalseOrderByCreatedAtDesc(
            String severity, Pageable pageable);

    long countByIsReadFalseAndIsDismissedFalse();

    long countBySeverityAndIsReadFalseAndIsDismissedFalse(String severity);

    boolean existsByReferenceTypeAndReferenceIdAndIsDismissedFalse(String referenceType, UUID referenceId);

    @Modifying
    @Transactional
    @Query("UPDATE ExpiryAlert a SET a.isDismissed = true, a.dismissedAt = :dismissedAt, a.dismissedBy = :userId WHERE a.isDismissed = false")
    int dismissAll(@Param("dismissedAt") LocalDateTime dismissedAt, @Param("userId") UUID userId);
}
