package com.medbid.product.service;

import com.medbid.product.entity.Product;
import com.medbid.product.entity.ProductDocument;
import com.medbid.product.mapper.ProductMapper;
import com.medbid.product.repository.ProductDocumentRepository;
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
public class ProductDocumentService {

    private final ProductDocumentRepository productDocumentRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public List<ProductMapper.ProductDocumentDto> getByProductId(UUID productId) {
        return productMapper.toDocumentDtoList(
                productDocumentRepository.findByProductIdAndDeletedFalse(productId));
    }

    @Transactional(readOnly = true)
    public ProductMapper.ProductDocumentDto getById(UUID id) {
        ProductDocument document = findDocumentById(id);
        return productMapper.toDocumentDto(document);
    }

    public ProductMapper.ProductDocumentDto uploadDocument(UUID productId, String documentType,
                                                           String documentName, String filePath,
                                                           String fileName, Long fileSize) {
        Product product = productRepository.findById(productId)
                .filter(p -> !p.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        ProductDocument document = ProductDocument.builder()
                .product(product)
                .documentType(documentType)
                .documentName(documentName)
                .filePath(filePath)
                .fileName(fileName)
                .fileSize(fileSize)
                .build();

        document = productDocumentRepository.save(document);
        log.info("Uploaded product document: {} for product: {}", document.getDocumentName(), productId);
        return productMapper.toDocumentDto(document);
    }

    public ProductMapper.ProductDocumentDto updateDocument(UUID id, String documentType,
                                                           String documentName) {
        ProductDocument document = findDocumentById(id);
        if (documentType != null) {
            document.setDocumentType(documentType);
        }
        if (documentName != null) {
            document.setDocumentName(documentName);
        }
        document = productDocumentRepository.save(document);
        log.info("Updated product document: {} (ID: {})", document.getDocumentName(), document.getId());
        return productMapper.toDocumentDto(document);
    }

    public void delete(UUID id) {
        ProductDocument document = findDocumentById(id);
        document.setDeleted(true);
        productDocumentRepository.save(document);
        log.info("Soft-deleted product document: {} (ID: {})", document.getDocumentName(), document.getId());
    }

    private ProductDocument findDocumentById(UUID id) {
        return productDocumentRepository.findById(id)
                .filter(d -> !d.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("ProductDocument", "id", id));
    }
}
