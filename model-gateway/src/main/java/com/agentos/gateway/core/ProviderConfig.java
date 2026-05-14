package com.agentos.gateway.core;

import java.util.Map;

public class ProviderConfig {
    private String name;
    private String apiKey;
    private String endpoint;
    private Map<String, ModelConfig> models;
    private int timeoutSeconds = 30;
    private int maxRetries = 3;
    private double costPer1kTokens;

    public ProviderConfig() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public Map<String, ModelConfig> getModels() { return models; }
    public void setModels(Map<String, ModelConfig> models) { this.models = models; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public double getCostPer1kTokens() { return costPer1kTokens; }
    public void setCostPer1kTokens(double costPer1kTokens) { this.costPer1kTokens = costPer1kTokens; }

    public static class ModelConfig {
        private String id;
        private String displayName;
        private ModelRole role;
        private int contextWindow;
        private double costPer1kInputTokens;
        private double costPer1kOutputTokens;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public ModelRole getRole() { return role; }
        public void setRole(ModelRole role) { this.role = role; }
        public int getContextWindow() { return contextWindow; }
        public void setContextWindow(int contextWindow) { this.contextWindow = contextWindow; }
        public double getCostPer1kInputTokens() { return costPer1kInputTokens; }
        public void setCostPer1kInputTokens(double costPer1kInputTokens) { this.costPer1kInputTokens = costPer1kInputTokens; }
        public double getCostPer1kOutputTokens() { return costPer1kOutputTokens; }
        public void setCostPer1kOutputTokens(double costPer1kOutputTokens) { this.costPer1kOutputTokens = costPer1kOutputTokens; }
    }
}