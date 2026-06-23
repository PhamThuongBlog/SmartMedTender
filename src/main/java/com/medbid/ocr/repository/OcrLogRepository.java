package com.medbid.ocr.repository;

import com.medbid.ocr.entity.OcrLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OcrLogRepository extends JpaRepository<OcrLog, UUID> {
}
