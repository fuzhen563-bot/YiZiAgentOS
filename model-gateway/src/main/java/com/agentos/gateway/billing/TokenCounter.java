package com.agentos.gateway.billing;

import com.agentos.gateway.core.ModelResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TokenCounter {
    private static final Logger log = LoggerFactory.getLogger(TokenCounter.class);
    private final Map<String, AtomicLong> promptTokens = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> completionTokens = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> totalCost = new ConcurrentHashMap<>();

    public void record(ModelResponse response) {
        String provider = response.getProvider();
        promptTokens.computeIfAbsent(provider, k -> new AtomicLong())
            .addAndGet(response.getPromptTokens());
        completionTokens.computeIfAbsent(provider, k -> new AtomicLong())
            .addAndGet(response.getCompletionTokens());
        totalCost.computeIfAbsent(provider, k -> new AtomicLong())
            .addAndGet((long) (response.getCost() * 1000000));
        log.debug("Token usage recorded for {}: prompt={}, completion={}, cost={}",
            provider, response.getPromptTokens(), response.getCompletionTokens(), response.getCost());
    }

    public long getPromptTokens(String provider) {
        return promptTokens.getOrDefault(provider, new AtomicLong()).get();
    }

    public long getCompletionTokens(String provider) {
        return completionTokens.getOrDefault(provider, new AtomicLong()).get();
    }

    public double getTotalCost(String provider) {
        return totalCost.getOrDefault(provider, new AtomicLong()).get() / 1000000.0;
    }

    public Map<String, Object> getSummary() {
        return Map.of(
            "prompt_tokens", promptTokens.values().stream().mapToLong(AtomicLong::get).sum(),
            "completion_tokens", completionTokens.values().stream().mapToLong(AtomicLong::get).sum(),
            "total_cost", totalCost.values().stream().mapToLong(AtomicLong::get).sum() / 1000000.0
        );
    }
}