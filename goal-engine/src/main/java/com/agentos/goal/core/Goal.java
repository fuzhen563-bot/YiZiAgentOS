package com.agentos.goal.core;

import java.time.LocalDateTime;
import java.util.*;

public class Goal {
    private String id;
    private String title;
    private String description;
    private String workspaceId;
    private String agentId;
    private GoalStatus status;
    private int priority;
    private Map<String, Double> kpis;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> taskIds;

    public Goal() {}

    public Goal(String title, String description, String workspaceId) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.workspaceId = workspaceId;
        this.status = GoalStatus.ACTIVE;
        this.kpis = new LinkedHashMap<>();
        this.taskIds = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public String getWorkspaceId() { return workspaceId; }
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public GoalStatus getStatus() { return status; }
    public void setStatus(GoalStatus status) { this.status = status; updatedAt = LocalDateTime.now(); }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public Map<String, Double> getKpis() { return kpis; }
    public void setKpi(String name, double value) { kpis.put(name, value); updatedAt = LocalDateTime.now(); }
    public Double getKpi(String name) { return kpis.get(name); }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<String> getTaskIds() { return taskIds; }
    public void addTask(String taskId) { taskIds.add(taskId); }

    public boolean isOverdue() {
        return deadline != null && LocalDateTime.now().isAfter(deadline) && status == GoalStatus.ACTIVE;
    }
}