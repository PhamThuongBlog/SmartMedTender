package com.medbid.quotation.service;

import com.medbid.exception.BusinessException;
import com.medbid.exception.ResourceNotFoundException;
import com.medbid.product.entity.Product;
import com.medbid.product.repository.ProductRepository;
import com.medbid.quotation.dto.QuotationCreateRequest;
import com.medbid.quotation.dto.QuotationDto;
import com.medbid.quotation.entity.PriceHistory;
import com.medbid.quotation.entity.Quotation;
import com.medbid.quotation.repository.PriceHistoryRepository;
import com.medbid.quotation.repository.QuotationRepository;
import com.medbid.tender.repository.TenderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QuotationService {

    private final QuotationRepository quotationRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final TenderRepository tenderRepository;
    private final ProductRepository productRepository;

    public QuotationDto create(QuotationCreateRequest request) {
        validateTenderExists(request.tenderId());
        validateProductExists(request.productId());

        Quotation quotation = Quotation.builder()
                .tenderId(request.tenderId())
                .productId(request.productId())
                .importPrice(request.importPrice())
                .sellingPrice(request.sellingPrice())
                .winningPrice(request.winningPrice())
                .bidDate(request.bidDate() != null ? request.bidDate() : LocalDate.now())
                .isWinning(request.isWinning() != null ? request.isWinning() : false)
                .source(request.source())
                .notes(request.notes())
                .build();

        quotation = quotationRepository.save(quotation);
        log.info("Created quotation: id={}, tenderId={}, productId={}",
                quotation.getId(), request.tenderId(), request.productId());

        if (quotation.getIsWinning() && quotation.getWinningPrice() != null) {
            PriceHistory history = PriceHistory.builder()
                    .productId(request.productId())
                    .price(quotation.getWinningPrice())
                    .priceType("WINNING")
                    .recordedDate(quotation.getBidDate())
                    .source("TENDER_" + request.tenderId())
                    .build();
            priceHistoryRepository.save(history);
            log.info("Recorded winning price history for product: {}", request.productId());
        }

        return toDto(quotation);
    }

    public QuotationDto update(UUID id, QuotationCreateRequest request) {
        Quotation quotation = getQuotationOrThrow(id);

        quotation.setTenderId(request.tenderId());
        quotation.setProductId(request.productId());
        quotation.setImportPrice(request.importPrice());
        quotation.setSellingPrice(request.sellingPrice());
        quotation.setWinningPrice(request.winningPrice());
        quotation.setBidDate(request.bidDate());
        quotation.setIsWinning(request.isWinning() != null ? request.isWinning() : false);
        quotation.setSource(request.source());
        quotation.setNotes(request.notes());

        quotation = quotationRepository.save(quotation);
        log.info("Updated quotation: id={}", id);
        return toDto(quotation);
    }

    @Transactional
    public void delete(UUID id) {
        Quotation quotation = getQuotationOrThrow(id);
        quotation.setDeleted(true);
        quotationRepository.save(quotation);
        log.info("Soft-deleted quotation: id={}", id);
    }

    @Transactional(readOnly = true)
    public QuotationDto findById(UUID id) {
        return toDto(getQuotationOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<QuotationDto> findAll(Pageable pageable) {
        return quotationRepository.findByDeletedFalse(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<QuotationDto> findByTenderId(UUID tenderId) {
        return quotationRepository.findByTenderId(tenderId).stream()
                .map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<QuotationDto> findByProductId(UUID productId) {
        return quotationRepository.findByProductId(productId).stream()
                .map(this::toDto).toList();
    }

    public record SuggestedPrice(
            BigDecimal suggestedPrice,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            BigDecimal averagePrice,
            String confidence,
            String recommendation,
            int dataPoints
    ) {}

    @Transactional(readOnly = true)
    public SuggestedPrice suggestPrice(UUID productId, UUID tenderId) {
        validateProductExists(productId);

        List<PriceHistory> history = priceHistoryRepository
                .findByProductIdOrderByRecordedDateDesc(productId);

        if (history.isEmpty()) {
            return new SuggestedPrice(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    "THẤP",
                    "Chưa có dữ liệu lịch sử giá cho sản phẩm này. Cần thu thập thêm dữ liệu.",
                    0
            );
        }

        BigDecimalStatistics stats = calculateStatistics(history);

        Optional<Quotation> lastWinning = quotationRepository
                .findFirstByProductIdAndIsWinningTrueOrderByBidDateDesc(productId);

        BigDecimal suggestedPrice;
        String recommendation;
        String confidence;

        if (lastWinning.isPresent() && lastWinning.get().getWinningPrice() != null) {
            BigDecimal lastWinPrice = lastWinning.get().getWinningPrice();

            if (history.size() >= 5) {
                suggestedPrice = stats.avg().multiply(BigDecimal.valueOf(0.95))
                        .setScale(0, RoundingMode.HALF_UP);

                if (suggestedPrice.compareTo(lastWinPrice) > 0) {
                    suggestedPrice = lastWinPrice.multiply(BigDecimal.valueOf(0.98))
                            .setScale(0, RoundingMode.HALF_UP);
                }
            } else {
                suggestedPrice = lastWinPrice.multiply(BigDecimal.valueOf(0.97))
                        .setScale(0, RoundingMode.HALF_UP);
            }
            recommendation = "Giá đề xuất dựa trên giá trúng thầu gần nhất và xu hướng thị trường.";
        } else {
            suggestedPrice = stats.avg().multiply(BigDecimal.valueOf(0.90))
                    .setScale(0, RoundingMode.HALF_UP);
            recommendation = "Chưa có dữ liệu trúng thầu, đề xuất dựa trên giá trung bình thị trường.";
        }

        if (history.size() >= 20) {
            confidence = "CAO";
        } else if (history.size() >= 10) {
            confidence = "TRUNG BÌNH";
        } else if (history.size() >= 3) {
            confidence = "THẤP";
        } else {
            confidence = "RẤT THẤP";
        }

        log.info("Price suggestion for productId={}: suggestedPrice={}, confidence={}, dataPoints={}",
                productId, suggestedPrice, confidence, history.size());

        return new SuggestedPrice(
                suggestedPrice,
                stats.min(),
                stats.max(),
                stats.avg(),
                confidence,
                recommendation,
                history.size()
        );
    }

    public record PriceChartPoint(
            LocalDate date,
            BigDecimal price,
            String priceType,
            String source
    ) {}

    @Transactional(readOnly = true)
    public List<PriceChartPoint> getPriceChart(UUID productId) {
        validateProductExists(productId);

        List<PriceHistory> history = priceHistoryRepository
                .findByProductIdOrderByRecordedDateDesc(productId);

        if (history.isEmpty()) {
            return List.of();
        }

        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        List<PriceHistory> recentHistory = history.stream()
                .filter(h -> h.getRecordedDate().isAfter(oneYearAgo))
                .sorted(Comparator.comparing(PriceHistory::getRecordedDate))
                .toList();

        if (recentHistory.isEmpty()) {
            recentHistory = history.stream()
                    .sorted(Comparator.comparing(PriceHistory::getRecordedDate))
                    .toList();
        }

        return recentHistory.stream()
                .map(h -> new PriceChartPoint(
                        h.getRecordedDate(),
                        h.getPrice(),
                        h.getPriceType(),
                        h.getSource()
                ))
                .collect(Collectors.toList());
    }

    public record PriceStatistics(
            BigDecimal averageWinningPrice,
            BigDecimal medianWinningPrice,
            BigDecimal minWinningPrice,
            BigDecimal maxWinningPrice,
            BigDecimal stdDeviation,
            long totalDataPoints
    ) {}

    @Transactional(readOnly = true)
    public PriceStatistics getPriceStatistics(UUID productId) {
        List<PriceHistory> winningPrices = priceHistoryRepository
                .findByProductIdAndPriceTypeOrderByRecordedDateDesc(productId, "WINNING");

        if (winningPrices.isEmpty()) {
            return new PriceStatistics(
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, 0
            );
        }

        List<BigDecimal> prices = winningPrices.stream()
                .map(PriceHistory::getPrice)
                .sorted()
                .toList();

        BigDecimal avg = prices.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP);

        BigDecimal median;
        int mid = prices.size() / 2;
        if (prices.size() % 2 == 0) {
            median = prices.get(mid - 1).add(prices.get(mid))
                    .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        } else {
            median = prices.get(mid);
        }

        BigDecimal variance = prices.stream()
                .map(p -> p.subtract(avg).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(prices.size()), 4, RoundingMode.HALF_UP);

        double stdDev = Math.sqrt(variance.doubleValue());

        return new PriceStatistics(
                avg,
                median,
                prices.get(0),
                prices.get(prices.size() - 1),
                BigDecimal.valueOf(stdDev).setScale(2, RoundingMode.HALF_UP),
                prices.size()
        );
    }

    private record BigDecimalStatistics(BigDecimal min, BigDecimal max, BigDecimal avg) {}

    private BigDecimalStatistics calculateStatistics(List<PriceHistory> history) {
        if (history.isEmpty()) {
            return new BigDecimalStatistics(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal min = null;
        BigDecimal max = null;
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;

        for (PriceHistory h : history) {
            BigDecimal p = h.getPrice();
            if (p == null) continue;
            if (min == null || p.compareTo(min) < 0) min = p;
            if (max == null || p.compareTo(max) > 0) max = p;
            sum = sum.add(p);
            count++;
        }

        BigDecimal avg = count > 0
                ? sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new BigDecimalStatistics(
                min != null ? min : BigDecimal.ZERO,
                max != null ? max : BigDecimal.ZERO,
                avg
        );
    }

    private Quotation getQuotationOrThrow(UUID id) {
        return quotationRepository.findById(id)
                .filter(q -> Boolean.FALSE.equals(q.getDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Quotation", "id", id));
    }

    private void validateTenderExists(UUID id) {
        if (!tenderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tender", "id", id);
        }
    }

    private void validateProductExists(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
    }

    private QuotationDto toDto(Quotation q) {
        return new QuotationDto(
                q.getId(),
                q.getTenderId(),
                q.getProductId(),
                q.getImportPrice(),
                q.getSellingPrice(),
                q.getWinningPrice(),
                q.getBidDate(),
                q.getIsWinning(),
                q.getSource(),
                q.getNotes(),
                q.getCreatedAt(),
                q.getUpdatedAt()
        );
    }
}
