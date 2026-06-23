package com.medbid.enterprise.mapper;

import com.medbid.enterprise.dto.*;
import com.medbid.enterprise.entity.BankAccount;
import com.medbid.enterprise.entity.EnterpriseProfile;
import com.medbid.enterprise.entity.LegalDocument;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EnterpriseMapper {

    EnterpriseProfileDto toDto(EnterpriseProfile entity);

    List<EnterpriseProfileDto> toDtoList(List<EnterpriseProfile> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "legalDocuments", ignore = true)
    @Mapping(target = "bankAccounts", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    EnterpriseProfile toEntity(EnterpriseCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "legalDocuments", ignore = true)
    @Mapping(target = "bankAccounts", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(EnterpriseUpdateRequest request, @MappingTarget EnterpriseProfile entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "legalDocuments", ignore = true)
    @Mapping(target = "bankAccounts", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    EnterpriseProfile fromDto(EnterpriseProfileDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "legalDocuments", ignore = true)
    @Mapping(target = "bankAccounts", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(EnterpriseProfileDto dto, @MappingTarget EnterpriseProfile entity);

    LegalDocumentDto toLegalDocumentDto(LegalDocument entity);

    List<LegalDocumentDto> toLegalDocumentDtoList(List<LegalDocument> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "enterprise", ignore = true)
    @Mapping(target = "filePath", ignore = true)
    @Mapping(target = "fileName", ignore = true)
    @Mapping(target = "fileSize", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    LegalDocument toLegalDocumentEntity(LegalDocumentUploadRequest request);

    BankAccountDto toBankAccountDto(BankAccount entity);

    List<BankAccountDto> toBankAccountDtoList(List<BankAccount> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enterprise", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    BankAccount toBankAccountEntity(BankAccountDto dto);
}
