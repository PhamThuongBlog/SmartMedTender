package com.medbid.product.repository;

import com.medbid.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    List<ProductImage> findByProductId(UUID productId);

    List<ProductImage> findByProductIdOrderBySortOrderAsc(UUID productId);

    Optional<ProductImage> findByProductIdAndIsPrimaryTrue(UUID productId);
}
