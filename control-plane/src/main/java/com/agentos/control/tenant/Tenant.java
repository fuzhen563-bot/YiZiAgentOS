package com.agentos.control.tenant;

import java.time.LocalDateTime;
import java.util.*;

public class Tenant {
    private String id;
    private String name;
    private String slug;
    private String plan;
    private String status;
    private Map<String, Object> settings;
    private Map<String, Integer> quotas;
    private int userCount;
    private int agentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Tenant() {}

    public Tenant(String name, String slug) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.slug = slug;
        this.plan = "free";
        this.status = "active";
        this.settings = new HashMap<>();
        this.quotas = new HashMap<>(Map.of("users", 10, "agents", 5, "storage_gb", 2));
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getName() { return name; }  public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public String getPlan() { return plan; }  public void setPlan(String plan) { this.plan = plan; }
    public String getStatus() { return status; }  public void setStatus(String status) { this.status = status; }
    public Map<String, Object> getSettings() { return settings; }
    public Map<String, Integer> getQuotas() { return quotas; }  public void setQuota(String key, int val) { quotas.put(key, val); }
    public int getUserCount() { return userCount; }  public void setUserCount(int n) { userCount = n; }
    public int getAgentCount() { return agentCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isOverQuota(String resource) {
        return quotas.getOrDefault(resource, 0) <= switch (resource) {
            case "users" -> userCount;
            case "agents" -> agentCount;
            default -> 0;
        };
    }
}