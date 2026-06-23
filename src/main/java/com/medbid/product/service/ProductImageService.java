package com.medbid.product.service;

import com.medbid.product.entity.Product;
import com.medbid.product.entity.ProductImage;
import com.medbid.product.mapper.ProductMapper;
import com.medbid.product.repository.ProductImageRepository;
import com.medbid.product.repository.ProductRepository;
import com.medbid.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public List<ProductMapper.ProductImageDto> getByProductId(UUID productId) {
        return productMapper.toImageDtoList(
                productImageRepository.findByProductIdOrderBySortOrderAsc(productId));
    }

    @Transactional(readOnly = true)
    public ProductMapper.ProductImageDto getById(UUID id) {
        ProductImage image = findImageById(id);
        return productMapper.toImageDto(image);
    }

    public ProductMapper.ProductImageDto uploadImage(UUID productId, String filePath,
                                                     String fileName, boolean isPrimary) {
        Product product = productRepository.findById(productId)
                .filter(p -> !p.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // If setting as primary, unset any existing primary images
        if (isPrimary) {
            clearPrimaryImages(productId);
        }

        // Determine next sort order
        List<ProductImage> existingImages = productImageRepository.findByProductId(productId);
        int nextSortOrder = existingImages.stream()
                .mapToInt(ProductImage::getSortOrder)
                .max()
                .orElse(0) + 1;

        ProductImage image = ProductImage.builder()
                .product(product)
                .filePath(filePath)
                .fileName(fileName)
                .isPrimary(isPrimary)
                .sortOrder(nextSortOrder)
                .build();

        image = productImageRepository.save(image);
        log.info("Uploaded product image: {} for product: {}", fileName, productId);
        return productMapper.toImageDto(image);
    }

    public ProductMapper.ProductImageDto setPrimary(UUID imageId) {
        ProductImage image = findImageById(imageId);
        clearPrimaryImages(image.getProduct().getId());
        image.setIsPrimary(true);
        image = productImageRepository.save(image);
        log.info("Set primary image: {} for product: {}", image.getId(), image.getProduct().getId());
        return productMapper.toImageDto(image);
    }

    public void updateSortOrder(UUID imageId, int sortOrder) {
        ProductImage image = findImageById(imageId);
        image.setSortOrder(sortOrder);
        productImageRepository.save(image);
        log.info("Updated sort order for image: {} to {}", imageId, sortOrder);
    }

    public void delete(UUID id) {
        ProductImage image = findImageById(id);
        productImageRepository.delete(image);
        log.info("Deleted product image: {}", id);
    }

    private void clearPrimaryImages(UUID productId) {
        productImageRepository.findByProductIdAndIsPrimaryTrue(productId)
                .ifPresent(primary -> {
                    primary.setIsPrimary(false);
                    productImageRepository.save(primary);
                });
    }

    private ProductImage findImageById(UUID id) {
        return productImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", "id", id));
    }
}
