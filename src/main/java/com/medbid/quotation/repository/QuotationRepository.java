package com.medbid.quotation.repository;

import com.medbid.quotation.entity.Quotation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, UUID> {

    List<Quotation> findByTenderId(UUID tenderId);

    List<Quotation> findByProductId(UUID productId);

    List<Quotation> findByProductIdOrderByBidDateDesc(UUID productId);

    List<Quotation> findByTenderIdAndProductId(UUID tenderId, UUID productId);

    Page<Quotation> findByTenderId(UUID tenderId, Pageable pageable);

    Page<Quotation> findByDeletedFalse(Pageable pageable);

    Optional<Quotation> findFirstByProductIdAndIsWinningTrueOrderByBidDateDesc(UUID productId);
}
