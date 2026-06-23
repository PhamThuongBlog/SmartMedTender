package com.medbid.product.mapper;

import com.medbid.product.dto.ProductCreateRequest;
import com.medbid.product.dto.ProductDto;
import com.medbid.product.entity.Product;
import com.medbid.product.entity.ProductDocument;
import com.medbid.product.entity.ProductImage;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    ProductDto toDto(Product entity);

    List<ProductDto> toDtoList(List<Product> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "productDocuments", ignore = true)
    @Mapping(target = "productImages", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Product toEntity(ProductCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "productDocuments", ignore = true)
    @Mapping(target = "productImages", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(ProductCreateRequest request, @MappingTarget Product entity);

    // Product document mapping
    @Mapping(target = "productId", source = "product.id")
    ProductDocumentDto toDocumentDto(ProductDocument entity);

    List<ProductDocumentDto> toDocumentDtoList(List<ProductDocument> entities);

    // Product image mapping
    @Mapping(target = "productId", source = "product.id")
    ProductImageDto toImageDto(ProductImage entity);

    List<ProductImageDto> toImageDtoList(List<ProductImage> entities);

    // Inner DTO records for documents and images
    record ProductDocumentDto(
            java.util.UUID id,
            java.util.UUID productId,
            String documentType,
            String documentName,
            String filePath,
            String fileName,
            Long fileSize,
            java.time.LocalDate issueDate,
            java.time.LocalDate expiryDate,
            String notes,
            java.time.LocalDateTime createdAt
    ) {}

    record ProductImageDto(
            java.util.UUID id,
            java.util.UUID productId,
            String filePath,
            String fileName,
            Boolean isPrimary,
            Integer sortOrder,
            java.time.LocalDateTime createdAt
    ) {}
}
