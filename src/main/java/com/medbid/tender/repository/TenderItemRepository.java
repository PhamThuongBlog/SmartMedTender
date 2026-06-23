package com.medbid.tender.repository;

import com.medbid.tender.entity.TenderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TenderItemRepository extends JpaRepository<TenderItem, UUID> {

    List<TenderItem> findByTenderId(UUID tenderId);

    List<TenderItem> findByTenderIdAndDeletedFalse(UUID tenderId);

    void deleteByTenderId(UUID tenderId);
}
