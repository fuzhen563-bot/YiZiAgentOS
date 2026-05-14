package com.agentos.secure.config;

import com.agentos.secure.browser.BrowserAutomation;
import com.agentos.secure.policy.PermissionBroker;
import com.agentos.secure.policy.PolicyGuard;
import com.agentos.secure.rollback.RollbackManager;
import com.agentos.secure.sandbox.SandboxEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecureExecutionConfig {

    @Bean
    public BrowserAutomation browserAutomation() { return new BrowserAutomation(); }

    @Bean
    public SandboxEnvironment sandboxEnvironment() { return new SandboxEnvironment(); }

    @Bean
    public PermissionBroker permissionBroker() { return new PermissionBroker(); }

    @Bean
    public PolicyGuard policyGuard() { return new PolicyGuard(); }

    @Bean
    public RollbackManager rollbackManager() { return new RollbackManager(); }
}