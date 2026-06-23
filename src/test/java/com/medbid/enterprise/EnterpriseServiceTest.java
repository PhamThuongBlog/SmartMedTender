package com.medbid.enterprise;

import com.medbid.enterprise.dto.EnterpriseCreateRequest;
import com.medbid.enterprise.dto.EnterpriseProfileDto;
import com.medbid.enterprise.entity.EnterpriseProfile;
import com.medbid.enterprise.mapper.EnterpriseMapper;
import com.medbid.enterprise.repository.EnterpriseProfileRepository;
import com.medbid.enterprise.repository.LegalDocumentRepository;
import com.medbid.enterprise.service.EnterpriseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnterpriseServiceTest {

    @Mock private EnterpriseProfileRepository enterpriseRepository;
    @Mock private LegalDocumentRepository legalDocumentRepository;
    @Mock private EnterpriseMapper enterpriseMapper;

    @InjectMocks private EnterpriseService enterpriseService;

    private EnterpriseProfileDto makeDto(UUID id, String companyName) {
        return new EnterpriseProfileDto(id, companyName, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    @Test
    void shouldCreateEnterprise() {
        EnterpriseCreateRequest request = new EnterpriseCreateRequest(
                "Cong ty TNHH ABC", "0123456789", "Ha Noi", "0241234567",
                "abc@test.com", "www.abc.vn", "Nguyen Van A",
                null, null, null, null);

        EnterpriseProfile entity = new EnterpriseProfile();
        entity.setId(UUID.randomUUID());
        entity.setCompanyName("Cong ty TNHH ABC");

        when(enterpriseMapper.toEntity(request)).thenReturn(entity);
        when(enterpriseRepository.save(any())).thenReturn(entity);
        when(enterpriseMapper.toDto(entity)).thenReturn(makeDto(entity.getId(), "Cong ty TNHH ABC"));

        EnterpriseProfileDto result = enterpriseService.create(request);

        assertNotNull(result);
        assertEquals("Cong ty TNHH ABC", result.companyName());
        verify(enterpriseRepository).save(any());
    }

    @Test
    void shouldGetAllEnterprises() {
        when(enterpriseRepository.findByDeletedFalse(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<EnterpriseProfileDto> result = enterpriseService.getAll(Pageable.unpaged());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldGetEnterpriseById() {
        UUID id = UUID.randomUUID();
        EnterpriseProfile entity = new EnterpriseProfile();
        entity.setId(id);
        entity.setCompanyName("Test");

        when(enterpriseRepository.findById(id)).thenReturn(Optional.of(entity));
        when(enterpriseMapper.toDto(entity)).thenReturn(makeDto(id, "Test"));

        EnterpriseProfileDto result = enterpriseService.getById(id);

        assertNotNull(result);
        assertEquals("Test", result.companyName());
    }
}
