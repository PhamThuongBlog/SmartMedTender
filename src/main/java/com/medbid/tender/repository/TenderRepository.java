package com.medbid.tender.repository;

import com.medbid.tender.entity.Tender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TenderRepository extends JpaRepository<Tender, UUID> {

    Page<Tender> findByDeletedFalse(Pageable pageable);

    Page<Tender> findByStatus(String status, Pageable pageable);

    List<Tender> findBySubmissionDeadlineBefore(LocalDateTime dateTime);

    List<Tender> findByStatusAndDeletedFalse(String status);

    Page<Tender> findByStatusAndDeletedFalse(String status, Pageable pageable);

    Page<Tender> findByStatusInAndDeletedFalse(List<String> statuses, Pageable pageable);

    boolean existsByNameAndDeletedFalse(String name);
}
