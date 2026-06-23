package com.medbid.hsmt.repository;

import com.medbid.tender.entity.TenderDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TenderDocumentRepository extends JpaRepository<TenderDocument, UUID> {

    List<TenderDocument> findByTenderId(UUID tenderId);

    List<TenderDocument> findByTenderIdAndOcrStatus(UUID tenderId, String ocrStatus);

    List<TenderDocument> findByOcrStatus(String ocrStatus);
}
