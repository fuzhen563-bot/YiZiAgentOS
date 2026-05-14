package com.agentos.gateway.core;

import java.util.Map;

public class ModelRequest {
    private String modelId;
    private ModelRole role;
    private String prompt;
    private Map<String, Object> parameters;
    private String userId;
    private String workspaceId;
    private int maxTokens;
    private double temperature;

    public ModelRequest() {}

    public ModelRequest(String modelId, ModelRole role, String prompt) {
        this.modelId = modelId;
        this.role = role;
        this.prompt = prompt;
        this.temperature = 0.7;
        this.maxTokens = 4096;
    }

    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public ModelRole getRole() { return role; }
    public void setRole(ModelRole role) { this.role = role; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
}