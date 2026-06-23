package com.medbid.tender.service;

import com.medbid.exception.BusinessException;
import com.medbid.exception.ResourceNotFoundException;
import com.medbid.tender.dto.TenderItemDto;
import com.medbid.tender.dto.TenderItemRequest;
import com.medbid.tender.entity.Tender;
import com.medbid.tender.entity.TenderItem;
import com.medbid.tender.entity.TenderStatus;
import com.medbid.tender.repository.TenderItemRepository;
import com.medbid.tender.repository.TenderRepository;
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
public class TenderItemService {

    private final TenderItemRepository itemRepository;
    private final TenderRepository tenderRepository;

    public List<TenderItemDto> findByTenderId(UUID tenderId) {
        return itemRepository.findByTenderIdAndDeletedFalse(tenderId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public TenderItemDto findById(UUID tenderId, UUID itemId) {
        TenderItem item = getItemOrThrow(itemId);
        if (!item.getTender().getId().equals(tenderId)) {
            throw new ResourceNotFoundException("TenderItem", "id", itemId);
        }
        return toDto(item);
    }

    public TenderItemDto addItem(UUID tenderId, TenderItemRequest request) {
        Tender tender = getTenderOrThrow(tenderId);

        if (tender.getStatus() != TenderStatus.DRAFT && tender.getStatus() != TenderStatus.REVIEWING) {
            throw new BusinessException("Chỉ có thể thêm danh mục vào gói thầu ở trạng thái DRAFT hoặc REVIEWING");
        }

        TenderItem item = TenderItem.builder()
                .tender(tender)
                .itemNumber(request.itemNumber())
                .name(request.name())
                .description(request.description())
                .quantity(request.quantity())
                .unit(request.unit())
                .estimatedPrice(request.estimatedPrice())
                .notes(request.notes())
                .build();

        item = itemRepository.save(item);
        log.info("Added item to tender: tenderId={}, itemId={}, name={}", tenderId, item.getId(), item.getName());
        return toDto(item);
    }

    public TenderItemDto updateItem(UUID tenderId, UUID itemId, TenderItemRequest request) {
        TenderItem item = getItemOrThrow(itemId);
        if (!item.getTender().getId().equals(tenderId)) {
            throw new ResourceNotFoundException("TenderItem", "id", itemId);
        }

        item.setItemNumber(request.itemNumber());
        item.setName(request.name());
        item.setDescription(request.description());
        item.setQuantity(request.quantity());
        item.setUnit(request.unit());
        item.setEstimatedPrice(request.estimatedPrice());
        item.setNotes(request.notes());

        item = itemRepository.save(item);
        log.info("Updated tender item: id={}", item.getId());
        return toDto(item);
    }

    @Transactional
    public void deleteItem(UUID tenderId, UUID itemId) {
        TenderItem item = getItemOrThrow(itemId);
        if (!item.getTender().getId().equals(tenderId)) {
            throw new ResourceNotFoundException("TenderItem", "id", itemId);
        }
        item.setDeleted(true);
        itemRepository.save(item);
        log.info("Soft-deleted tender item: id={}", item.getId());
    }

    private TenderItem getItemOrThrow(UUID id) {
        return itemRepository.findById(id)
                .filter(i -> Boolean.FALSE.equals(i.getDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("TenderItem", "id", id));
    }

    private Tender getTenderOrThrow(UUID id) {
        return tenderRepository.findById(id)
                .filter(t -> Boolean.FALSE.equals(t.getDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Tender", "id", id));
    }

    private TenderItemDto toDto(TenderItem item) {
        return new TenderItemDto(
                item.getId(),
                item.getTender() != null ? item.getTender().getId() : null,
                item.getItemNumber(),
                item.getName(),
                item.getDescription(),
                item.getQuantity(),
                item.getUnit(),
                item.getEstimatedPrice(),
                item.getNotes()
        );
    }
}
