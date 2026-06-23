package com.medbid.hsdt.service;

import com.medbid.enterprise.entity.EnterpriseProfile;
import com.medbid.enterprise.entity.LegalDocument;
import com.medbid.enterprise.repository.EnterpriseProfileRepository;
import com.medbid.enterprise.repository.LegalDocumentRepository;
import com.medbid.exception.BusinessException;
import com.medbid.exception.ResourceNotFoundException;
import com.medbid.hsdt.dto.HSDTPreviewResponse;
import com.medbid.hsdt.dto.HSDTPreviewResponse.*;
import com.medbid.matching.MatchingService;
import com.medbid.matching.MatchResponse;
import com.medbid.matching.dto.GapAnalysisResponse;
import com.medbid.product.entity.Product;
import com.medbid.product.entity.ProductDocument;
import com.medbid.product.repository.ProductDocumentRepository;
import com.medbid.product.repository.ProductRepository;
import com.medbid.quotation.service.QuotationService;
import com.medbid.tender.entity.Tender;
import com.medbid.tender.entity.TenderRequirement;
import com.medbid.tender.repository.TenderRepository;
import com.medbid.tender.repository.TenderRequirementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HSDTBuilderService {

    private final TenderRepository tenderRepository;
    private final TenderRequirementRepository requirementRepository;
    private final ProductRepository productRepository;
    private final ProductDocumentRepository productDocumentRepository;
    private final EnterpriseProfileRepository enterpriseProfileRepository;
    private final LegalDocumentRepository legalDocumentRepository;
    private final MatchingService matchingService;
    private final QuotationService quotationService;

    public HSDTPreviewResponse buildPreview(UUID tenderId, List<UUID> productIds) {
        Tender tender = getTender(tenderId);
        if (productIds == null || productIds.isEmpty()) {
            throw new BusinessException("Vui lòng chọn ít nhất một sản phẩm để tạo HSDT");
        }

        // Enterprise info
        EnterpriseProfile enterprise = getPrimaryEnterprise();

        // Build product entries
        List<ProductEntry> entries = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        int complete = 0, incomplete = 0;

        for (int i = 0; i < productIds.size(); i++) {
            UUID pid = productIds.get(i);
            ProductEntry entry = buildProductEntry(tenderId, pid, i + 1);
            entries.add(entry);

            if (entry.getSuggestedPrice() != null) {
                totalPrice = totalPrice.add(entry.getSuggestedPrice());
            }
            if (entry.isAllMandatoryPassed()) complete++;
            else incomplete++;
        }

        // Smart checklist
        List<ChecklistItem> checklist = generateSmartChecklist(tender, entries, enterprise);

        return HSDTPreviewResponse.builder()
                .tenderId(tenderId)
                .tenderName(tender.getName())
                .bidPackageCode(tender.getBidPackageCode())
                .procuringEntity(tender.getProcuringEntity())
                .submissionDeadline(tender.getSubmissionDeadline())
                .estimatedValue(tender.getEstimatedValue())
                .currency(tender.getCurrency())
                .companyName(enterprise != null ? enterprise.getCompanyName() : "Chưa thiết lập")
                .taxCode(enterprise != null ? enterprise.getTaxCode() : null)
                .companyAddress(enterprise != null ? enterprise.getAddress() : null)
                .legalRepresentative(enterprise != null ? enterprise.getLegalRepresentative() : null)
                .products(entries)
                .totalProducts(productIds.size())
                .totalPrice(totalPrice)
                .completeProducts(complete)
                .incompleteProducts(incomplete)
                .checklist(checklist)
                .generatedAt(java.time.LocalDateTime.now())
                .build();
    }

    private ProductEntry buildProductEntry(UUID tenderId, UUID productId, int itemNumber) {
        Product product = getProduct(productId);
        ProductEntry entry = ProductEntry.builder()
                .productId(productId).productName(product.getName())
                .manufacturer(product.getManufacturer()).brand(product.getBrand())
                .model(product.getModel()).originCountry(product.getOriginCountry())
                .itemNumber(itemNumber).build();

        // Run matching
        try {
            MatchResponse match = matchingService.matchProduct(tenderId, productId);
            entry.setMatchScore(match.overallScore());
            entry.setPassedRequirements(match.passed());
            entry.setTotalRequirements(match.totalRequirements());

            boolean allMandatory = requirementRepository.findByTenderIdAndMandatoryTrueAndDeletedFalse(tenderId)
                    .stream().allMatch(mr -> match.details().stream()
                            .anyMatch(d -> d.requirementId().equals(mr.getId()) && "PASS".equals(d.status())));
            entry.setAllMandatoryPassed(allMandatory);

            if (!allMandatory) {
                entry.setFailedMandatoryReqs(
                        requirementRepository.findByTenderIdAndMandatoryTrueAndDeletedFalse(tenderId).stream()
                                .filter(mr -> match.details().stream()
                                        .noneMatch(d -> d.requirementId().equals(mr.getId()) && "PASS".equals(d.status())))
                                .map(TenderRequirement::getDescription).toList()
                );
            }
        } catch (Exception e) {
            log.warn("Matching failed for product {}: {}", productId, e.getMessage());
            entry.setMatchScore(0);
            entry.setAllMandatoryPassed(false);
        }

        // Document status
        List<ProductDocument> docs = productDocumentRepository.findByProductIdAndDeletedFalse(productId);
        List<DocStatus> docStatuses = new ArrayList<>();
        String[] certTypes = {"CO", "CQ", "ISO_13485", "ISO_9001", "CE", "FDA", "CATALOGUE"};
        for (String ct : certTypes) {
            var found = docs.stream().filter(d -> ct.equals(d.getDocumentType())).findFirst();
            docStatuses.add(DocStatus.builder()
                    .docType(ct).docName(found.map(ProductDocument::getDocumentName).orElse(null))
                    .available(found.isPresent())
                    .expired(found.map(d -> d.getExpiryDate() != null && d.getExpiryDate().isBefore(LocalDate.now())).orElse(false))
                    .expiryDate(found.map(d -> d.getExpiryDate() != null ? d.getExpiryDate().toString() : null).orElse(null))
                    .build());
        }
        entry.setDocuments(docStatuses);

        // Price suggestion
        try {
            QuotationService.SuggestedPrice sp = quotationService.suggestPrice(productId, tenderId);
            entry.setSuggestedPrice(sp.suggestedPrice());
            entry.setImportPrice(sp.suggestedPrice());  // use as proxy
            entry.setSellingPrice(sp.suggestedPrice());
            entry.setLastWinningPrice(sp.maxPrice());
            entry.setPriceConfidence(sp.confidence());
            entry.setPriceDataPoints(sp.dataPoints());
        } catch (Exception e) {
            log.warn("Price suggestion failed for product {}: {}", productId, e.getMessage());
        }

        return entry;
    }

    private List<ChecklistItem> generateSmartChecklist(Tender tender, List<ProductEntry> entries, EnterpriseProfile enterprise) {
        List<ChecklistItem> items = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // I. HANH CHINH
        items.add(new ChecklistItem("HANH_CHINH", "Đơn dự thầu (theo mẫu)", "OK", "Tạo từ hệ thống", true));
        items.add(new ChecklistItem("HANH_CHINH", "Giấy ủy quyền (nếu có)", "WARNING", "Kiểm tra và đính kèm nếu cần", false));

        // Check enterprise legal docs
        if (enterprise != null) {
            List<LegalDocument> legalDocs = legalDocumentRepository.findByEnterpriseIdAndDeletedFalse(enterprise.getId());
            boolean hasBizLicense = legalDocs.stream().anyMatch(d -> "BUSINESS_LICENSE".equals(d.getDocumentType()));
            items.add(new ChecklistItem("HANH_CHINH", "Giấy chứng nhận đăng ký kinh doanh",
                    hasBizLicense ? "OK" : "MISSING", hasBizLicense ? "Đã có" : "Cần tải lên hồ sơ doanh nghiệp", true));

            // Check business license expiry
            if (enterprise.getBusinessLicenseExpiryDate() != null) {
                boolean expired = enterprise.getBusinessLicenseExpiryDate().isBefore(today);
                items.add(new ChecklistItem("HANH_CHINH", "Giấy phép kinh doanh còn hiệu lực",
                        expired ? "EXPIRED" : "OK", expired ? "Đã hết hạn" : "Còn hiệu lực đến " + enterprise.getBusinessLicenseExpiryDate(), true));
            }

            items.add(new ChecklistItem("HANH_CHINH", "Báo cáo tài chính năm gần nhất",
                    "WARNING", "Cần đính kèm báo cáo tài chính đã kiểm toán", true));
        } else {
            items.add(new ChecklistItem("HANH_CHINH", "Hồ sơ pháp lý doanh nghiệp",
                    "MISSING", "Chưa thiết lập hồ sơ doanh nghiệp trong hệ thống", true));
        }

        // II. KY THUAT - per product
        items.add(new ChecklistItem("KY_THUAT", "Bảng so sánh thông số kỹ thuật",
                "OK", "Được tạo tự động từ hệ thống đối chiếu", true));

        for (ProductEntry entry : entries) {
            items.add(new ChecklistItem("KY_THUAT", "Catalog sản phẩm: " + entry.getProductName(),
                    entry.getDocuments().stream().anyMatch(d -> "CATALOGUE".equals(d.getDocType()) && d.isAvailable()) ? "OK" : "MISSING",
                    "Cần đính kèm catalogue gốc", true));

            items.add(new ChecklistItem("KY_THUAT", "Hướng dẫn sử dụng: " + entry.getProductName(),
                    "WARNING", "Đính kèm IFU/User Manual", false));

            if (!entry.isAllMandatoryPassed()) {
                String failedReqs = entry.getFailedMandatoryReqs() != null
                        ? String.join("; ", entry.getFailedMandatoryReqs()) : "Xem bảng đối chiếu";
                items.add(new ChecklistItem("KY_THUAT", "Tiêu chí bắt buộc chưa đạt: " + entry.getProductName(),
                        "WARNING", failedReqs, true));
            }
        }

        // III. CHUNG CHI
        for (ProductEntry entry : entries) {
            for (DocStatus ds : entry.getDocuments()) {
                if (!ds.isAvailable()) {
                    items.add(new ChecklistItem("CHUNG_CHI",
                            "Thiếu chứng chỉ " + ds.getDocType() + " cho " + entry.getProductName(),
                            "MISSING", "Cần bổ sung chứng chỉ " + ds.getDocType(), true));
                } else if (ds.isExpired()) {
                    items.add(new ChecklistItem("CHUNG_CHI",
                            "Chứng chỉ " + ds.getDocType() + " hết hạn: " + entry.getProductName(),
                            "EXPIRED", "Hết hạn ngày " + ds.getExpiryDate(), true));
                } else {
                    items.add(new ChecklistItem("CHUNG_CHI",
                            "Chứng chỉ " + ds.getDocType() + ": " + entry.getProductName(),
                            "OK", ds.getDocName(), false));
                }
            }
        }

        // IV. TAI CHINH
        items.add(new ChecklistItem("TAI_CHINH", "Bảng giá chào thầu", "OK", "Tạo tự động từ hệ thống", true));
        items.add(new ChecklistItem("TAI_CHINH", "Bảo lãnh dự thầu", "WARNING", "Cần ngân hàng phát hành bảo lãnh", true));
        items.add(new ChecklistItem("TAI_CHINH", "Phân tích chi tiết giá", "OK",
                "Dựa trên " + entries.stream().mapToInt(ProductEntry::getPriceDataPoints).sum() + " điểm dữ liệu lịch sử", false));

        // V. KHAC
        items.add(new ChecklistItem("KHAC", "Biên bản khảo sát hiện trường (nếu có)", "WARNING", "", false));
        items.add(new ChecklistItem("KHAC", "Hợp đồng tương tự đã thực hiện", "WARNING", "Đính kèm 2-3 hợp đồng tương tự", false));
        items.add(new ChecklistItem("KHAC", "Tài liệu khác theo yêu cầu HSMT", "WARNING", "Kiểm tra HSMT để bổ sung", false));

        return items;
    }

    private Tender getTender(UUID id) {
        return tenderRepository.findById(id)
                .filter(t -> Boolean.FALSE.equals(t.getDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Tender", "id", id));
    }

    private Product getProduct(UUID id) {
        return productRepository.findById(id)
                .filter(p -> Boolean.FALSE.equals(p.getDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    private EnterpriseProfile getPrimaryEnterprise() {
        var page = enterpriseProfileRepository.findByDeletedFalse(PageRequest.of(0, 1));
        return page.isEmpty() ? null : page.getContent().get(0);
    }
}
