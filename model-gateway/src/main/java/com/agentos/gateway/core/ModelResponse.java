package com.agentos.gateway.core;

public class ModelResponse {
    private String id;
    private String content;
    private String modelId;
    private String provider;
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    private double cost;
    private long latencyMs;
    private boolean success;
    private String errorMessage;

    public ModelResponse() {}

    public ModelResponse success(String id, String content, String modelId, String provider) {
        this.id = id;
        this.content = content;
        this.modelId = modelId;
        this.provider = provider;
        this.success = true;
        return this;
    }

    public ModelResponse failure(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
        return this;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public int getPromptTokens() { return promptTokens; }
    public void setPromptTokens(int promptTokens) { this.promptTokens = promptTokens; }
    public int getCompletionTokens() { return completionTokens; }
    public void setCompletionTokens(int completionTokens) { this.completionTokens = completionTokens; }
    public int getTotalTokens() { return totalTokens; }
    public void setTotalTokens(int totalTokens) { this.totalTokens = totalTokens; }
    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
    public long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(long latencyMs) { this.latencyMs = latencyMs; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}