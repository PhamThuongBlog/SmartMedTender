package com.medbid.security.config;

import com.medbid.audit.service.AuditService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditConfig {

    /**
     * Exposes the existing AuditService (from com.medbid.audit.service) as a named
     * bean for the security/audit context. The primary AuditService is already
     * component-scanned and managed by Spring; this config provides an additional
     * alias bean in the security context if needed for @Qualifier usage.
     */
    @Bean
    public AuditService securityAuditService(AuditService auditService) {
        return auditService;
    }
}
