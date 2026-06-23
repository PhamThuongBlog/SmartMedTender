package com.medbid.product;

import com.medbid.product.dto.ProductCreateRequest;
import com.medbid.product.dto.ProductDto;
import com.medbid.product.entity.Product;
import com.medbid.product.mapper.ProductMapper;
import com.medbid.product.repository.ProductRepository;
import com.medbid.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductMapper productMapper;

    @InjectMocks private ProductService productService;

    private ProductDto makeDto(UUID id, String name, String manufacturer, String category) {
        return new ProductDto(id, name, manufacturer, null, null, null, category,
                null, Map.of(), null, null, null,
                false, false, false, false, "ACTIVE", null, null);
    }

    @Test
    void shouldCreateProduct() {
        ProductCreateRequest request = new ProductCreateRequest(
                "May CT Scanner", "Siemens", "SOMATOM", "Duc",
                "Chan doan hinh anh", "Medical Imaging",
                "May CT 128 lat cat", java.util.Collections.emptyMap(),
                null, null, null,
                false, false, true, false);

        Product entity = new Product();
        entity.setName("May CT Scanner");

        when(productMapper.toEntity(request)).thenReturn(entity);
        when(productRepository.save(any())).thenReturn(entity);
        when(productMapper.toDto(entity)).thenReturn(makeDto(entity.getId(), "May CT Scanner", "Siemens", "Chan doan hinh anh"));

        ProductDto result = productService.create(request);

        assertNotNull(result);
        assertEquals("May CT Scanner", result.name());
    }

    @Test
    void shouldSearchProducts() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("May CT");
        product.setCategory("Chan doan hinh anh");

        when(productRepository.searchProductsByKeyword(eq("%CT%"), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product)));
        when(productMapper.toDto(product)).thenReturn(makeDto(product.getId(), "May CT", "Siemens", "Chan doan hinh anh"));

        Page<ProductDto> results = productService.searchProducts("CT", null, null, Pageable.unpaged());

        assertNotNull(results);
        assertEquals(1, results.getTotalElements());
    }

    @Test
    void shouldGetProductById() {
        UUID id = UUID.randomUUID();
        Product entity = new Product();
        entity.setId(id);
        entity.setName("Test Product");

        when(productRepository.findById(id)).thenReturn(Optional.of(entity));
        when(productMapper.toDto(entity)).thenReturn(makeDto(id, "Test Product", null, null));

        ProductDto result = productService.getById(id);

        assertNotNull(result);
        assertEquals("Test Product", result.name());
    }
}
