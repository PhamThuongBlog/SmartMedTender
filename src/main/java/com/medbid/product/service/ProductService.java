package com.medbid.product.service;

import com.medbid.product.dto.ProductCreateRequest;
import com.medbid.product.dto.ProductDto;
import com.medbid.product.entity.Product;
import com.medbid.product.mapper.ProductMapper;
import com.medbid.product.repository.ProductRepository;
import com.medbid.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public ProductDto getById(UUID id) {
        Product product = findProductById(id);
        return productMapper.toDto(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getAll(Pageable pageable) {
        return productRepository.findByDeletedFalse(pageable)
                .map(productMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> searchProducts(String keyword, String category,
                                           String manufacturer, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.trim() + "%";
            return productRepository.searchProductsByKeyword(pattern, category, manufacturer, pageable)
                    .map(productMapper::toDto);
        }
        return productRepository.searchProducts(category, manufacturer, pageable)
                .map(productMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getByCategory(String category, Pageable pageable) {
        return productRepository.findByCategoryAndDeletedFalse(category, pageable)
                .map(productMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getByManufacturer(String manufacturer, Pageable pageable) {
        return productRepository.findByManufacturerAndDeletedFalse(manufacturer, pageable)
                .map(productMapper::toDto);
    }

    public ProductDto create(ProductCreateRequest request) {
        Product product = productMapper.toEntity(request);
        product.setStatus("ACTIVE");
        product = productRepository.save(product);
        log.info("Created product: {} (ID: {})", product.getName(), product.getId());
        return productMapper.toDto(product);
    }

    public ProductDto update(UUID id, ProductCreateRequest request) {
        Product product = findProductById(id);
        productMapper.updateEntity(request, product);
        product = productRepository.save(product);
        log.info("Updated product: {} (ID: {})", product.getName(), product.getId());
        return productMapper.toDto(product);
    }

    public void delete(UUID id) {
        Product product = findProductById(id);
        product.setDeleted(true);
        product.setStatus("INACTIVE");
        productRepository.save(product);
        log.info("Soft-deleted product: {} (ID: {})", product.getName(), product.getId());
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getExpiringProducts() {
        LocalDate thirtyDaysLater = LocalDate.now().plusDays(30);
        List<Product> expiringProducts = productRepository
                .findProductsWithExpiringRegistration(thirtyDaysLater);
        log.info("Found {} products with expiring registrations within 30 days", expiringProducts.size());
        return productMapper.toDtoList(expiringProducts);
    }

    private Product findProductById(UUID id) {
        return productRepository.findById(id)
                .filter(p -> !p.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }
}
