package com.medbid.product.repository;

import com.medbid.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByDeletedFalse(Pageable pageable);

    Page<Product> findByCategoryAndDeletedFalse(String category, Pageable pageable);

    Page<Product> findByManufacturerAndDeletedFalse(String manufacturer, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchByNameOrBrand(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:manufacturer IS NULL OR p.manufacturer = :manufacturer)")
    Page<Product> searchProducts(@Param("category") String category,
                                 @Param("manufacturer") String manufacturer,
                                 Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:manufacturer IS NULL OR p.manufacturer = :manufacturer) AND " +
           "(LOWER(p.name) LIKE LOWER(:keyword) OR LOWER(p.brand) LIKE LOWER(:keyword))")
    Page<Product> searchProductsByKeyword(@Param("keyword") String keyword,
                                          @Param("category") String category,
                                          @Param("manufacturer") String manufacturer,
                                          Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND " +
           "p.registrationExpiryDate IS NOT NULL AND p.registrationExpiryDate <= :date")
    List<Product> findProductsWithExpiringRegistration(@Param("date") LocalDate date);

    List<Product> findByCategoryAndDeletedFalse(String category);
}
