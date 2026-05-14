package com.agentos.runtime.core;

import java.util.*;

public class AgentRequest {
    private String conversationId;
    private String message;
    private String userId;
    private String workspaceId;
    private List<Map<String, Object>> conversationHistory;
    private Map<String, Object> context;
    private int maxSteps;

    public AgentRequest() {}

    public AgentRequest(String message, String userId) {
        this.message = message;
        this.userId = userId;
        this.conversationHistory = new ArrayList<>();
        this.context = new HashMap<>();
        this.maxSteps = 25;
    }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String id) { this.conversationId = id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public List<Map<String, Object>> getConversationHistory() { return conversationHistory; }
    public void setConversationHistory(List<Map<String, Object>> conversationHistory) { this.conversationHistory = conversationHistory; }
    public Map<String, Object> getContext() { return context; }
    public void setContext(Map<String, Object> context) { this.context = context; }
    public int getMaxSteps() { return maxSteps; }
    public void setMaxSteps(int maxSteps) { this.maxSteps = maxSteps; }
}