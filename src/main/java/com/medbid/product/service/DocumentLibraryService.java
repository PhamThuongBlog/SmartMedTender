package com.medbid.product.service;

import com.medbid.exception.ResourceNotFoundException;
import com.medbid.product.dto.ProductDocumentDto;
import com.medbid.product.dto.ProductDocumentUploadRequest;
import com.medbid.product.entity.Product;
import com.medbid.product.entity.ProductDocument;
import com.medbid.product.repository.ProductDocumentRepository;
import com.medbid.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DocumentLibraryService {

    private final ProductDocumentRepository productDocumentRepository;
    private final ProductRepository productRepository;

    @Value("${app.file.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * Search all product documents across all products with optional filters.
     */
    @Transactional(readOnly = true)
    public Page<ProductDocumentDto> searchDocuments(String search, UUID productId, String documentType,
                                                     String status, Pageable pageable) {
        // Load all non-deleted documents
        List<ProductDocument> allDocs = new ArrayList<>();
        if (productId != null) {
            allDocs = productDocumentRepository.findByProductIdAndDeletedFalse(productId);
        } else {
            // For global search, we need to iterate. In production, add a custom query.
            List<Product> allProducts = productRepository.findAll();
            for (Product p : allProducts) {
                if (!p.getDeleted()) {
                    allDocs.addAll(productDocumentRepository.findByProductIdAndDeletedFalse(p.getId()));
                }
            }
        }

        // Filter
        List<ProductDocument> filtered = allDocs.stream()
                .filter(d -> {
                    if (documentType != null && !documentType.equals(d.getDocumentType())) return false;
                    if (search != null && !search.isBlank()) {
                        String q = search.toLowerCase();
                        boolean matchesName = d.getDocumentName() != null && d.getDocumentName().toLowerCase().contains(q);
                        boolean matchesProduct = d.getProduct() != null && d.getProduct().getName() != null
                                && d.getProduct().getName().toLowerCase().contains(q);
                        if (!matchesName && !matchesProduct) return false;
                    }
                    if (status != null) {
                        if ("EXPIRED".equals(status)) {
                            if (d.getExpiryDate() == null || !d.getExpiryDate().isBefore(LocalDate.now())) return false;
                        } else if ("ACTIVE".equals(status)) {
                            if (d.getExpiryDate() != null && d.getExpiryDate().isBefore(LocalDate.now())) return false;
                        }
                    }
                    return true;
                })
                .sorted(Comparator.comparing(ProductDocument::getCreatedAt).reversed())
                .collect(Collectors.toList());

        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        if (start >= filtered.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, filtered.size());
        }
        List<ProductDocumentDto> dtos = filtered.subList(start, end).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, filtered.size());
    }

    @Transactional(readOnly = true)
    public ProductDocumentDto getDocument(UUID id) {
        ProductDocument doc = findDocumentById(id);
        return toDto(doc);
    }

    /**
     * Upload a new product document with file storage.
     */
    public ProductDocumentDto uploadDocument(MultipartFile file, ProductDocumentUploadRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .filter(p -> !p.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        // Save file to disk
        String originalFileName = file.getOriginalFilename();
        String extension = getFileExtension(originalFileName);
        String storedFileName = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

        Path docDir = Paths.get(uploadDir, "documents", product.getId().toString());
        try {
            Files.createDirectories(docDir);
        } catch (IOException e) {
            log.error("Failed to create document directory: {}", docDir, e);
            throw new RuntimeException("Không thể tạo thư mục lưu trữ tài liệu");
        }

        Path targetPath = docDir.resolve(storedFileName);
        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Document file saved: {}", targetPath);
        } catch (IOException e) {
            log.error("Failed to save document file: {}", targetPath, e);
            throw new RuntimeException("Không thể lưu file tài liệu: " + e.getMessage());
        }

        ProductDocument doc = ProductDocument.builder()
                .product(product)
                .documentType(request.getDocumentType())
                .documentName(request.getDocumentName())
                .filePath(targetPath.toString())
                .fileName(originalFileName)
                .fileSize(file.getSize())
                .issueDate(request.getIssueDate())
                .expiryDate(request.getExpiryDate())
                .notes(request.getDocumentName())
                .build();

        doc = productDocumentRepository.save(doc);
        log.info("Product document created: id={}, type={}, name={}", doc.getId(), doc.getDocumentType(), doc.getDocumentName());
        return toDto(doc);
    }

    /**
     * Update document metadata.
     */
    public ProductDocumentDto updateDocument(UUID id, ProductDocumentUploadRequest request) {
        ProductDocument doc = findDocumentById(id);

        if (request.getDocumentType() != null) doc.setDocumentType(request.getDocumentType());
        if (request.getDocumentName() != null) doc.setDocumentName(request.getDocumentName());
        if (request.getIssuingAuthority() != null) doc.setNotes(request.getDocumentName());
        if (request.getIssueDate() != null) doc.setIssueDate(request.getIssueDate());
        if (request.getExpiryDate() != null) doc.setExpiryDate(request.getExpiryDate());
        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                    .filter(p -> !p.getDeleted())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));
            doc.setProduct(product);
        }

        doc = productDocumentRepository.save(doc);
        log.info("Product document updated: id={}", doc.getId());
        return toDto(doc);
    }

    /**
     * Soft delete a document.
     */
    public void deleteDocument(UUID id) {
        ProductDocument doc = findDocumentById(id);
        doc.setDeleted(true);
        productDocumentRepository.save(doc);
        log.info("Product document soft-deleted: id={}", id);
    }

    /**
     * Get file path for download.
     */
    public Path getDocumentFilePath(UUID id) {
        ProductDocument doc = findDocumentById(id);
        if (doc.getFilePath() == null) {
            throw new ResourceNotFoundException("File", "documentId", id);
        }
        return Paths.get(doc.getFilePath());
    }

    /**
     * Get all documents expiring within a date range (for expiry scanner).
     */
    @Transactional(readOnly = true)
    public List<ProductDocument> findExpiringDocuments(LocalDate from, LocalDate to) {
        List<ProductDocument> allDocs = new ArrayList<>();
        List<Product> allProducts = productRepository.findAll();
        for (Product p : allProducts) {
            if (!p.getDeleted()) {
                List<ProductDocument> docs = productDocumentRepository.findByProductIdAndDeletedFalse(p.getId());
                for (ProductDocument doc : docs) {
                    if (doc.getExpiryDate() != null
                            && !doc.getExpiryDate().isBefore(from)
                            && !doc.getExpiryDate().isAfter(to)) {
                        allDocs.add(doc);
                    }
                }
            }
        }
        return allDocs;
    }

    private ProductDocument findDocumentById(UUID id) {
        return productDocumentRepository.findById(id)
                .filter(d -> !d.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("ProductDocument", "id", id));
    }

    private ProductDocumentDto toDto(ProductDocument doc) {
        return ProductDocumentDto.builder()
                .id(doc.getId())
                .productId(doc.getProduct() != null ? doc.getProduct().getId() : null)
                .productName(doc.getProduct() != null ? doc.getProduct().getName() : null)
                .documentType(doc.getDocumentType())
                .documentName(doc.getDocumentName())
                .filePath(doc.getFilePath())
                .fileName(doc.getFileName())
                .fileSize(doc.getFileSize())
                .issueDate(doc.getIssueDate())
                .expiryDate(doc.getExpiryDate())
                .issuingAuthority(doc.getNotes())
                .notes(doc.getNotes())
                .status(determineStatus(doc.getExpiryDate()))
                .build();
    }

    private String determineStatus(LocalDate expiryDate) {
        if (expiryDate == null) return "ACTIVE";
        if (expiryDate.isBefore(LocalDate.now())) return "EXPIRED";
        return "ACTIVE";
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "";
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}
