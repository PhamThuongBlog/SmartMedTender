package com.medbid.enterprise.repository;

import com.medbid.enterprise.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {

    List<BankAccount> findByEnterpriseId(UUID enterpriseId);

    List<BankAccount> findByEnterpriseIdAndDeletedFalse(UUID enterpriseId);

    List<BankAccount> findByEnterpriseIdAndIsPrimaryTrue(UUID enterpriseId);
}
