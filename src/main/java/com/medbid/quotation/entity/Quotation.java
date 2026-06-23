package com.medbid.quotation.entity;

import com.medbid.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "quotations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Quotation extends BaseEntity {

    @Column(name = "tender_id", nullable = false)
    private java.util.UUID tenderId;

    @Column(name = "product_id", nullable = false)
    private java.util.UUID productId;

    @Column(name = "import_price", precision = 18, scale = 2)
    private BigDecimal importPrice;

    @Column(name = "selling_price", precision = 18, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "winning_price", precision = 18, scale = 2)
    private BigDecimal winningPrice;

    @Column(name = "bid_date")
    private LocalDate bidDate;

    @Column(name = "is_winning", nullable = false)
    @Builder.Default
    private Boolean isWinning = false;

    @Column(name = "source", length = 255)
    private String source;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
