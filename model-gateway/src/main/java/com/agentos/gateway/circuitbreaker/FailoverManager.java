package com.agentos.gateway.circuitbreaker;

import com.agentos.gateway.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FailoverManager {
    private static final Logger log = LoggerFactory.getLogger(FailoverManager.class);
    private final Map<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();
    private final Map<String, List<String>> failoverOrder = new ConcurrentHashMap<>();

    public void registerProvider(String providerName, int failureThreshold, long halfOpenTimeoutMs) {
        circuitBreakers.put(providerName, new CircuitBreaker(providerName, failureThreshold, halfOpenTimeoutMs));
    }

    public void setFailoverOrder(String modelId, List<String> providerOrder) {
        failoverOrder.put(modelId, providerOrder);
    }

    public ModelResponse executeWithFailover(ModelRequest request, List<ModelProvider> providers) {
        List<String> order = failoverOrder.getOrDefault(request.getModelId(),
            providers.stream().map(ModelProvider::getName).toList());

        List<Exception> errors = new ArrayList<>();
        for (String providerName : order) {
            CircuitBreaker cb = circuitBreakers.get(providerName);
            if (cb != null && !cb.isAllowed()) {
                log.warn("Circuit breaker open for provider {}, skipping", providerName);
                continue;
            }
            Optional<ModelProvider> provider = providers.stream()
                .filter(p -> p.getName().equals(providerName) && p.isAvailable())
                .findFirst();
            if (provider.isEmpty()) continue;

            try {
                ModelResponse response = provider.get().call(request);
                if (response.isSuccess()) {
                    if (cb != null) cb.onSuccess();
                    return response;
                }
                if (cb != null) cb.onFailure();
                errors.add(new RuntimeException(response.getErrorMessage()));
            } catch (Exception e) {
                if (cb != null) cb.onFailure();
                errors.add(e);
                log.warn("Failover from provider {}: {}", providerName, e.getMessage());
            }
        }
        return new ModelResponse().failure("All providers failed: " + errors);
    }
}