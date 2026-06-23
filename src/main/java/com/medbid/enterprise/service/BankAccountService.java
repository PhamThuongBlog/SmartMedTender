package com.medbid.enterprise.service;

import com.medbid.enterprise.dto.BankAccountDto;
import com.medbid.enterprise.entity.BankAccount;
import com.medbid.enterprise.entity.EnterpriseProfile;
import com.medbid.enterprise.mapper.EnterpriseMapper;
import com.medbid.enterprise.repository.BankAccountRepository;
import com.medbid.enterprise.repository.EnterpriseProfileRepository;
import com.medbid.exception.BusinessException;
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
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final EnterpriseProfileRepository enterpriseProfileRepository;
    private final EnterpriseMapper enterpriseMapper;

    @Transactional(readOnly = true)
    public List<BankAccountDto> getByEnterpriseId(UUID enterpriseId) {
        return enterpriseMapper.toBankAccountDtoList(
                bankAccountRepository.findByEnterpriseIdAndDeletedFalse(enterpriseId));
    }

    @Transactional(readOnly = true)
    public BankAccountDto getById(UUID id) {
        BankAccount bankAccount = findBankAccountById(id);
        return enterpriseMapper.toBankAccountDto(bankAccount);
    }

    public BankAccountDto create(UUID enterpriseId, BankAccountDto request) {
        EnterpriseProfile enterprise = enterpriseProfileRepository.findById(enterpriseId)
                .filter(e -> !e.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("EnterpriseProfile", "id", enterpriseId));

        // If this account is set as primary, unset any existing primary
        if (Boolean.TRUE.equals(request.isPrimary())) {
            clearPrimaryAccounts(enterpriseId);
        }

        BankAccount bankAccount = enterpriseMapper.toBankAccountEntity(request);
        bankAccount.setEnterprise(enterprise);

        bankAccount = bankAccountRepository.save(bankAccount);
        log.info("Created bank account: {} - {} for enterprise: {}",
                bankAccount.getBankName(), bankAccount.getAccountNumber(), enterpriseId);
        return enterpriseMapper.toBankAccountDto(bankAccount);
    }

    public BankAccountDto update(UUID id, BankAccountDto request) {
        BankAccount bankAccount = findBankAccountById(id);

        // If setting as primary, unset any existing primary
        if (Boolean.TRUE.equals(request.isPrimary()) && !Boolean.TRUE.equals(bankAccount.getIsPrimary())) {
            clearPrimaryAccounts(bankAccount.getEnterprise().getId());
        }

        if (request.bankName() != null) {
            bankAccount.setBankName(request.bankName());
        }
        if (request.branch() != null) {
            bankAccount.setBranch(request.branch());
        }
        if (request.accountNumber() != null) {
            bankAccount.setAccountNumber(request.accountNumber());
        }
        if (request.accountHolder() != null) {
            bankAccount.setAccountHolder(request.accountHolder());
        }
        if (request.swiftCode() != null) {
            bankAccount.setSwiftCode(request.swiftCode());
        }
        if (request.currency() != null) {
            bankAccount.setCurrency(request.currency());
        }
        if (request.isPrimary() != null) {
            bankAccount.setIsPrimary(request.isPrimary());
        }

        bankAccount = bankAccountRepository.save(bankAccount);
        log.info("Updated bank account: {} (ID: {})", bankAccount.getAccountNumber(), bankAccount.getId());
        return enterpriseMapper.toBankAccountDto(bankAccount);
    }

    public void delete(UUID id) {
        BankAccount bankAccount = findBankAccountById(id);
        bankAccount.setDeleted(true);
        bankAccountRepository.save(bankAccount);
        log.info("Soft-deleted bank account: {} (ID: {})", bankAccount.getAccountNumber(), bankAccount.getId());
    }

    private void clearPrimaryAccounts(UUID enterpriseId) {
        List<BankAccount> primaryAccounts = bankAccountRepository
                .findByEnterpriseIdAndIsPrimaryTrue(enterpriseId);
        for (BankAccount account : primaryAccounts) {
            account.setIsPrimary(false);
            bankAccountRepository.save(account);
        }
    }

    private BankAccount findBankAccountById(UUID id) {
        return bankAccountRepository.findById(id)
                .filter(a -> !a.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("BankAccount", "id", id));
    }
}
