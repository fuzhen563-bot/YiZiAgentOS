package com.agentos.gateway.billing;

import com.agentos.gateway.core.ModelResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class BillingService {
    private static final Logger log = LoggerFactory.getLogger(BillingService.class);
    private final TokenCounter tokenCounter;
    private final CostCalculator costCalculator;
    private final Map<String, AtomicLong> userTokens = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> workspaceTokens = new ConcurrentHashMap<>();

    public BillingService(TokenCounter tokenCounter, CostCalculator costCalculator) {
        this.tokenCounter = tokenCounter;
        this.costCalculator = costCalculator;
    }

    public void recordUsage(ModelResponse response, String userId, String workspaceId) {
        tokenCounter.record(response);
        if (userId != null) {
            userTokens.computeIfAbsent(userId, k -> new AtomicLong())
                .addAndGet(response.getTotalTokens());
        }
        if (workspaceId != null) {
            workspaceTokens.computeIfAbsent(workspaceId, k -> new AtomicLong())
                .addAndGet(response.getTotalTokens());
        }
    }

    public Map<String, Object> getUsageReport(String workspaceId) {
        return Map.of(
            "workspace_id", workspaceId,
            "total_tokens", workspaceTokens.getOrDefault(workspaceId, new AtomicLong()).get(),
            "provider_summary", tokenCounter.getSummary()
        );
    }
}