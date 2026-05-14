package com.agentos.control.config;

import com.agentos.control.billing.BillingManager;
import com.agentos.control.iam.IdentityManager;
import com.agentos.control.rbac.AccessControl;
import com.agentos.control.tenant.TenantManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ControlPlaneConfig {
    @Bean public TenantManager tenantManager() { return new TenantManager(); }
    @Bean public IdentityManager identityManager() { return new IdentityManager(); }
    @Bean public AccessControl accessControl() { return new AccessControl(); }
    @Bean public BillingManager billingManager() { return new BillingManager(); }
}