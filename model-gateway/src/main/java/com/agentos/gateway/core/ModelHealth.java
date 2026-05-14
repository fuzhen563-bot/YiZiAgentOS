package com.agentos.gateway.core;

public class ModelHealth {
    private String provider;
    private boolean healthy;
    private long latencyMs;
    private String lastError;
    private long lastCheckedAt;
    private int consecutiveFailures;

    public ModelHealth(String provider) {
        this.provider = provider;
        this.healthy = true;
        this.lastCheckedAt = System.currentTimeMillis();
    }

    public void markHealthy(long latencyMs) {
        this.healthy = true;
        this.latencyMs = latencyMs;
        this.lastError = null;
        this.consecutiveFailures = 0;
        this.lastCheckedAt = System.currentTimeMillis();
    }

    public void markUnhealthy(String error) {
        this.healthy = false;
        this.lastError = error;
        this.consecutiveFailures++;
        this.lastCheckedAt = System.currentTimeMillis();
    }

    public String getProvider() { return provider; }
    public boolean isHealthy() { return healthy; }
    public long getLatencyMs() { return latencyMs; }
    public String getLastError() { return lastError; }
    public long getLastCheckedAt() { return lastCheckedAt; }
    public int getConsecutiveFailures() { return consecutiveFailures; }
}