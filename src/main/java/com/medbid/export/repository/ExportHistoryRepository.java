package com.medbid.export.repository;

import com.medbid.export.entity.ExportHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExportHistoryRepository extends JpaRepository<ExportHistory, UUID> {

    List<ExportHistory> findByTenderIdOrderByCreatedAtDesc(UUID tenderId);

    Page<ExportHistory> findByTenderId(UUID tenderId, Pageable pageable);

    Page<ExportHistory> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
