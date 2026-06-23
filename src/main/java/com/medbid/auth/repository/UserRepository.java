package com.medbid.auth.repository;

import com.medbid.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("""
            SELECT u FROM User u
            WHERE u.deleted = false
              AND (:search IS NULL OR
                   u.username ILIKE CONCAT('%', :search, '%') OR
                   u.fullName ILIKE CONCAT('%', :search, '%') OR
                   u.email ILIKE CONCAT('%', :search, '%'))
            """)
    Page<User> findAllActive(@Param("search") String search, Pageable pageable);

    Page<User> findByDeletedFalse(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.id = :id")
    Optional<User> findActiveById(@Param("id") UUID id);
}
