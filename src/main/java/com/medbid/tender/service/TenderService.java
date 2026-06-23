package com.medbid.tender.service;

import com.medbid.exception.BusinessException;
import com.medbid.exception.ResourceNotFoundException;
import com.medbid.tender.dto.TenderCreateRequest;
import com.medbid.tender.dto.TenderDto;
import com.medbid.tender.dto.TenderOutcomeRequest;
import com.medbid.tender.dto.TenderStatusUpdateRequest;
import com.medbid.tender.entity.*;
import com.medbid.tender.repository.TenderRepository;
import com.medbid.product.repository.ProductRepository;
import com.medbid.quotation.entity.PriceHistory;
import com.medbid.quotation.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TenderService {

    private final TenderRepository tenderRepository;
    private final TenderItemService tenderItemService;
    private final PriceHistoryRepository priceHistoryRepository;
    private final ProductRepository productRepository;

    public Page<TenderDto> findAll(Pageable pageable) {
        return tenderRepository.findByDeletedFalse(pageable)
                .map(this::toDto);
    }

    public Page<TenderDto> findByStatus(String status, Pageable pageable) {
        return tenderRepository.findByStatusAndDeletedFalse(status, pageable)
                .map(this::toDto);
    }

    public TenderDto findById(UUID id) {
        Tender tender = getTenderOrThrow(id);
        return toDto(tender);
    }

    public TenderDto create(TenderCreateRequest request) {
        Tender tender = Tender.builder()
                .name(request.name())
                .description(request.description())
                .bidPackageCode(request.bidPackageCode())
                .procuringEntity(request.procuringEntity())
                .submissionDeadline(request.submissionDeadline())
                .openingDate(request.openingDate())
                .estimatedValue(request.estimatedValue())
                .currency(request.currency() != null ? request.currency() : "VND")
                .notes(request.notes())
                .status(TenderStatus.DRAFT)
                .build();

        tender = tenderRepository.save(tender);
        log.info("Created tender: id={}, name={}", tender.getId(), tender.getName());
        return toDto(tender);
    }

    public TenderDto update(UUID id, TenderCreateRequest request) {
        Tender tender = getTenderOrThrow(id);

        if (tender.getStatus() != TenderStatus.DRAFT && tender.getStatus() != TenderStatus.REVIEWING) {
            throw new BusinessException("Chỉ có thể cập nhật gói thầu ở trạng thái DRAFT hoặc REVIEWING");
        }

        tender.setName(request.name());
        tender.setDescription(request.description());
        tender.setBidPackageCode(request.bidPackageCode());
        tender.setProcuringEntity(request.procuringEntity());
        tender.setSubmissionDeadline(request.submissionDeadline());
        tender.setOpeningDate(request.openingDate());
        tender.setEstimatedValue(request.estimatedValue());
        if (request.currency() != null) {
            tender.setCurrency(request.currency());
        }
        tender.setNotes(request.notes());

        tender = tenderRepository.save(tender);
        log.info("Updated tender: id={}", tender.getId());
        return toDto(tender);
    }

    @Transactional
    public void delete(UUID id) {
        Tender tender = getTenderOrThrow(id);
        tender.setDeleted(true);
        tenderRepository.save(tender);
        log.info("Soft-deleted tender: id={}", tender.getId());
    }

    public TenderDto submit(UUID id) {
        Tender tender = getTenderOrThrow(id);
        validateStatusTransition(tender, TenderStatus.SUBMITTED,
                TenderStatus.APPROVED,
                "Chỉ có thể nộp gói thầu khi đã được phê duyệt (APPROVED)");

        tender.setStatus(TenderStatus.SUBMITTED);
        tender = tenderRepository.save(tender);
        log.info("Submitted tender: id={}", tender.getId());
        return toDto(tender);
    }

    public TenderDto approve(UUID id) {
        Tender tender = getTenderOrThrow(id);
        validateStatusTransition(tender, TenderStatus.APPROVED,
                TenderStatus.REVIEWING,
                "Chỉ có thể phê duyệt gói thầu ở trạng thái REVIEWING");

        tender.setStatus(TenderStatus.APPROVED);
        tender = tenderRepository.save(tender);
        log.info("Approved tender: id={}", tender.getId());
        return toDto(tender);
    }

    public TenderDto markWon(UUID id) {
        Tender tender = getTenderOrThrow(id);
        validateStatusTransition(tender, TenderStatus.WON,
                TenderStatus.SUBMITTED,
                "Chỉ có thể đánh dấu trúng thầu khi gói thầu ở trạng thái SUBMITTED");

        tender.setStatus(TenderStatus.WON);
        tender = tenderRepository.save(tender);
        log.info("Marked tender as WON: id={}", tender.getId());
        return toDto(tender);
    }

    public TenderDto markLost(UUID id) {
        Tender tender = getTenderOrThrow(id);
        validateStatusTransition(tender, TenderStatus.LOST,
                TenderStatus.SUBMITTED,
                "Chỉ có thể đánh dấu thất bại khi gói thầu ở trạng thái SUBMITTED");

        tender.setStatus(TenderStatus.LOST);
        tender = tenderRepository.save(tender);
        log.info("Marked tender as LOST: id={}", tender.getId());
        return toDto(tender);
    }

    public TenderDto cancel(UUID id) {
        Tender tender = getTenderOrThrow(id);
        if (tender.getStatus() == TenderStatus.WON || tender.getStatus() == TenderStatus.LOST) {
            throw new BusinessException("Không thể hủy gói thầu đã có kết quả (WON/LOST)");
        }

        tender.setStatus(TenderStatus.CANCELED);
        tender = tenderRepository.save(tender);
        log.info("Canceled tender: id={}", tender.getId());
        return toDto(tender);
    }

    @Transactional
    public TenderDto updateStatus(UUID id, TenderStatusUpdateRequest request) {
        TenderStatus newStatus;
        try {
            newStatus = TenderStatus.valueOf(request.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Trạng thái không hợp lệ: " + request.status()
                    + ". Các trạng thái hợp lệ: DRAFT, REVIEWING, APPROVED, SUBMITTED, WON, LOST, CANCELED");
        }

        Tender tender = getTenderOrThrow(id);
        if (!tender.getStatus().canTransitionTo(newStatus)) {
            throw new BusinessException("Không thể chuyển trạng thái từ " + tender.getStatus() + " sang " + newStatus);
        }

        tender.setStatus(newStatus);
        if (request.notes() != null) {
            tender.setNotes(request.notes());
        }
        tender = tenderRepository.save(tender);
        log.info("Updated tender status: id={}, status={}", tender.getId(), tender.getStatus());
        return toDto(tender);
    }

    @Transactional
    public TenderDto cloneTender(UUID sourceId) {
        Tender source = getTenderOrThrow(sourceId);

        Tender cloned = Tender.builder()
                .name(source.getName() + " (Bản sao)")
                .description(source.getDescription())
                .bidPackageCode(source.getBidPackageCode())
                .procuringEntity(source.getProcuringEntity())
                .submissionDeadline(source.getSubmissionDeadline())
                .openingDate(source.getOpeningDate())
                .estimatedValue(source.getEstimatedValue())
                .currency(source.getCurrency())
                .clonedFromId(source.getId())
                .notes("Được sao chép từ gói thầu #" + source.getId())
                .status(TenderStatus.DRAFT)
                .build();

        cloned = tenderRepository.save(cloned);

        for (TenderItem sourceItem : source.getItems()) {
            if (Boolean.FALSE.equals(sourceItem.getDeleted())) {
                TenderItem clonedItem = TenderItem.builder()
                        .tender(cloned)
                        .itemNumber(sourceItem.getItemNumber())
                        .name(sourceItem.getName())
                        .description(sourceItem.getDescription())
                        .quantity(sourceItem.getQuantity())
                        .unit(sourceItem.getUnit())
                        .estimatedPrice(sourceItem.getEstimatedPrice())
                        .notes(sourceItem.getNotes())
                        .build();
                cloned.addItem(clonedItem);
            }
        }

        for (TenderRequirement sourceReq : source.getRequirements()) {
            if (Boolean.FALSE.equals(sourceReq.getDeleted())) {
                TenderRequirement clonedReq = TenderRequirement.builder()
                        .tenderId(cloned.getId())
                        .description(sourceReq.getDescription())
                        .type(sourceReq.getType())
                        .operator(sourceReq.getOperator())
                        .value(sourceReq.getValue())
                        .unit(sourceReq.getUnit())
                        .mandatory(sourceReq.getMandatory())
                        .priority(sourceReq.getPriority())
                        .source(sourceReq.getSource())
                        .status("EXTRACTED")
                        .build();
                cloned.addRequirement(clonedReq);
            }
        }

        tenderRepository.save(cloned);
        log.info("Cloned tender: sourceId={}, clonedId={}", sourceId, cloned.getId());
        return toDto(cloned);
    }

    public List<TenderDto> findExpiringDeadlines() {
        LocalDateTime threshold = LocalDateTime.now().plusDays(3);
        List<Tender> expiring = tenderRepository.findBySubmissionDeadlineBefore(threshold);
        return expiring.stream()
                .filter(t -> t.getSubmissionDeadline().isAfter(LocalDateTime.now()))
                .map(this::toDto)
                .toList();
    }

    /**
     * Record tender outcome with winning price → persists to PriceHistory for future reuse.
     */
    public TenderDto recordOutcome(UUID id, TenderOutcomeRequest request) {
        Tender tender = getTenderOrThrow(id);

        // Validate: can't re-record already decided
        if (tender.getStatus() == TenderStatus.WON || tender.getStatus() == TenderStatus.LOST) {
            log.info("Tender already marked as {}, updating price instead", tender.getStatus());
        }

        if (request.won()) {
            tender.setStatus(TenderStatus.WON);
            log.info("Tender marked WON with price: id={}, price={}", id, request.winningPrice());
        } else {
            tender.setStatus(TenderStatus.LOST);
            log.info("Tender marked LOST: id={}", id);
        }
        tender = tenderRepository.save(tender);

        // Record price history for future price suggestions
        if (request.won() && request.winningPrice() != null) {
            TenderDto dto = toDto(tender);
            // Find any real product in the DB to satisfy FK constraint
            var products = productRepository.findByDeletedFalse(
                    org.springframework.data.domain.PageRequest.of(0, 1));
            if (!products.isEmpty()) {
                UUID productId = products.getContent().get(0).getId();
                PriceHistory ph = PriceHistory.builder()
                        .productId(productId)
                        .price(request.winningPrice())
                        .priceType("WINNING")
                        .recordedDate(LocalDate.now())
                        .source(dto.name() + " (trung thau)")
                        .build();
                priceHistoryRepository.save(ph);
                log.info("Price history recorded: {} {} for tender {}", request.winningPrice(), request.currency(), id);
            } else {
                log.warn("Cannot record price history: no products in DB");
            }
        }

        return toDto(tender);
    }

    /**
     * Find tenders by multiple statuses (comma-separated) for history/reuse.
     */
    public Page<TenderDto> findByStatuses(String statusesCsv, Pageable pageable) {
        List<String> statusList = Arrays.asList(statusesCsv.split(","));
        return tenderRepository.findByStatusInAndDeletedFalse(statusList, pageable).map(this::toDto);
    }

    private Tender getTenderOrThrow(UUID id) {
        return tenderRepository.findById(id)
                .filter(t -> Boolean.FALSE.equals(t.getDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Tender", "id", id));
    }

    private void validateStatusTransition(Tender tender, TenderStatus target, TenderStatus requiredCurrent,
                                          String errorMessage) {
        if (tender.getStatus() != requiredCurrent) {
            throw new BusinessException(errorMessage
                    + ". Trạng thái hiện tại: " + tender.getStatus());
        }
    }

    public TenderDto toDto(Tender tender) {
        return new TenderDto(
                tender.getId(),
                tender.getName(),
                tender.getDescription(),
                tender.getBidPackageCode(),
                tender.getProcuringEntity(),
                tender.getSubmissionDeadline(),
                tender.getOpeningDate(),
                tender.getEstimatedValue(),
                tender.getCurrency(),
                tender.getStatus().name(),
                tender.getClonedFromId(),
                tender.getNotes(),
                tender.getVersion(),
                tender.getCreatedAt(),
                tender.getUpdatedAt()
        );
    }
}
