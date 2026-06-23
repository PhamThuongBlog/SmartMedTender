package com.medbid.ai.repository;

import com.medbid.ai.entity.AiLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AiLogRepository extends JpaRepository<AiLog, UUID> {
}
