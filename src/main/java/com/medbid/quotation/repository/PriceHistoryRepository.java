package com.medbid.quotation.repository;

import com.medbid.quotation.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, UUID> {

    List<PriceHistory> findByProductIdOrderByRecordedDateDesc(UUID productId);

    List<PriceHistory> findByProductIdAndPriceTypeOrderByRecordedDateDesc(UUID productId, String priceType);

    @Query("SELECT ph FROM PriceHistory ph WHERE ph.productId = :productId " +
           "AND ph.recordedDate >= :since ORDER BY ph.recordedDate DESC")
    List<PriceHistory> findRecentByProductId(@Param("productId") UUID productId,
                                              @Param("since") LocalDate since);

    @Query("SELECT AVG(ph.price) FROM PriceHistory ph WHERE ph.productId = :productId " +
           "AND ph.priceType = :priceType AND ph.recordedDate >= :since")
    Double averagePriceByProductAndType(@Param("productId") UUID productId,
                                         @Param("priceType") String priceType,
                                         @Param("since") LocalDate since);
}
