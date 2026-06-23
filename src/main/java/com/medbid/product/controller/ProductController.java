package com.medbid.product.controller;

import com.medbid.product.dto.ProductCreateRequest;
import com.medbid.product.dto.ProductDto;
import com.medbid.product.dto.ProductSearchRequest;
import com.medbid.product.mapper.ProductMapper;
import com.medbid.product.service.ProductDocumentService;
import com.medbid.product.service.ProductImageService;
import com.medbid.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductDocumentService productDocumentService;
    private final ProductImageService productImageService;

    // ============================================
    // Product CRUD Endpoints
    // ============================================

    @GetMapping
    public ResponseEntity<Page<ProductDto>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String manufacturer,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(
                productService.searchProducts(keyword, category, manufacturer, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProductCreateRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================
    // Product Document Endpoints
    // ============================================

    @GetMapping("/{productId}/documents")
    public ResponseEntity<List<ProductMapper.ProductDocumentDto>> getDocuments(
            @PathVariable UUID productId) {
        return ResponseEntity.ok(productDocumentService.getByProductId(productId));
    }

    @GetMapping("/{productId}/documents/{documentId}")
    public ResponseEntity<ProductMapper.ProductDocumentDto> getDocument(
            @PathVariable UUID productId,
            @PathVariable UUID documentId) {
        return ResponseEntity.ok(productDocumentService.getById(documentId));
    }

    @PostMapping("/{productId}/documents")
    public ResponseEntity<ProductMapper.ProductDocumentDto> uploadDocument(
            @PathVariable UUID productId,
            @RequestParam String documentType,
            @RequestParam String documentName,
            @RequestParam(required = false) String filePath,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) Long fileSize) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productDocumentService.uploadDocument(
                        productId, documentType, documentName, filePath, fileName, fileSize));
    }

    @PutMapping("/{productId}/documents/{documentId}")
    public ResponseEntity<ProductMapper.ProductDocumentDto> updateDocument(
            @PathVariable UUID productId,
            @PathVariable UUID documentId,
            @RequestParam(required = false) String documentType,
            @RequestParam(required = false) String documentName) {
        return ResponseEntity.ok(
                productDocumentService.updateDocument(documentId, documentType, documentName));
    }

    @DeleteMapping("/{productId}/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable UUID productId,
            @PathVariable UUID documentId) {
        productDocumentService.delete(documentId);
        return ResponseEntity.noContent().build();
    }

    // ============================================
    // Product Image Endpoints
    // ============================================

    @GetMapping("/{productId}/images")
    public ResponseEntity<List<ProductMapper.ProductImageDto>> getImages(
            @PathVariable UUID productId) {
        return ResponseEntity.ok(productImageService.getByProductId(productId));
    }

    @PostMapping("/{productId}/images/upload")
    public ResponseEntity<ProductMapper.ProductImageDto> uploadImage(
            @PathVariable UUID productId,
            @RequestParam String filePath,
            @RequestParam(required = false) String fileName,
            @RequestParam(defaultValue = "false") boolean isPrimary) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productImageService.uploadImage(productId, filePath, fileName, isPrimary));
    }

    @PutMapping("/{productId}/images/{imageId}/primary")
    public ResponseEntity<ProductMapper.ProductImageDto> setPrimaryImage(
            @PathVariable UUID productId,
            @PathVariable UUID imageId) {
        return ResponseEntity.ok(productImageService.setPrimary(imageId));
    }

    @PutMapping("/{productId}/images/{imageId}/sort-order")
    public ResponseEntity<Void> updateSortOrder(
            @PathVariable UUID productId,
            @PathVariable UUID imageId,
            @RequestParam int sortOrder) {
        productImageService.updateSortOrder(imageId, sortOrder);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable UUID productId,
            @PathVariable UUID imageId) {
        productImageService.delete(imageId);
        return ResponseEntity.noContent().build();
    }

    // ============================================
    // Expiring Products Warning
    // ============================================

    @GetMapping("/expiring-registrations")
    public ResponseEntity<List<ProductDto>> getExpiringProducts() {
        return ResponseEntity.ok(productService.getExpiringProducts());
    }
}
