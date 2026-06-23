package com.medbid.matching;

import com.medbid.matching.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MatchResultRepository extends JpaRepository<MatchResult, UUID> {

    List<MatchResult> findByTenderId(UUID tenderId);

    List<MatchResult> findByTenderIdAndProductId(UUID tenderId, UUID productId);

    List<MatchResult> findByProductId(UUID productId);

    void deleteByTenderIdAndProductId(UUID tenderId, UUID productId);

    long countByTenderIdAndProductIdAndPassed(UUID tenderId, UUID productId, Boolean passed);
}
