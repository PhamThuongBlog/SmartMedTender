package com.medbid.enterprise.entity;

import com.medbid.common.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "enterprise_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EnterpriseProfile extends BaseEntity {

    @NotBlank
    @Column(name = "company_name", length = 500, nullable = false)
    private String companyName;

    @Column(name = "tax_code", length = 20)
    private String taxCode;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "website", length = 500)
    private String website;

    @Column(name = "company_name_en", length = 500)
    private String companyNameEn;

    @Column(name = "legal_representative", length = 255)
    private String legalRepresentative;

    @Column(name = "legal_rep_position", length = 255)
    private String legalRepPosition;

    @Column(name = "established_date")
    private LocalDate establishedDate;

    @Column(name = "issuing_authority", length = 255)
    private String issuingAuthority;

    @Column(name = "business_license_number", length = 100)
    private String businessLicenseNumber;

    @Column(name = "business_license_issue_date")
    private LocalDate businessLicenseIssueDate;

    @Column(name = "business_license_expiry_date")
    private LocalDate businessLicenseExpiryDate;

    @OneToMany(mappedBy = "enterprise", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LegalDocument> legalDocuments = new ArrayList<>();

    @OneToMany(mappedBy = "enterprise", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BankAccount> bankAccounts = new ArrayList<>();

    public void addLegalDocument(LegalDocument document) {
        legalDocuments.add(document);
        document.setEnterprise(this);
    }

    public void removeLegalDocument(LegalDocument document) {
        legalDocuments.remove(document);
        document.setEnterprise(null);
    }

    public void addBankAccount(BankAccount bankAccount) {
        bankAccounts.add(bankAccount);
        bankAccount.setEnterprise(this);
    }

    public void removeBankAccount(BankAccount bankAccount) {
        bankAccounts.remove(bankAccount);
        bankAccount.setEnterprise(null);
    }
}
