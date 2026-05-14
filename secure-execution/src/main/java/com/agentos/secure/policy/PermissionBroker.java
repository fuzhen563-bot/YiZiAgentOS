package com.agentos.secure.policy;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PermissionBroker {
    private final Map<String, Permission> permissions = new ConcurrentHashMap<>();
    private final Map<String, ApprovalRequest> pendingApprovals = new ConcurrentHashMap<>();

    public static class Permission {
        private String action;
        private String resource;
        private String riskLevel;
        private boolean requiresApproval;

        public Permission(String action, String resource, String riskLevel) {
            this.action = action;
            this.resource = resource;
            this.riskLevel = riskLevel;
            this.requiresApproval = "high".equals(riskLevel) || "critical".equals(riskLevel);
        }

        public String getAction() { return action; }
        public String getResource() { return resource; }
        public String getRiskLevel() { return riskLevel; }
        public boolean isRequiresApproval() { return requiresApproval; }
        public void setRequiresApproval(boolean v) { this.requiresApproval = v; }

        public String key() { return action + ":" + resource; }
    }

    public static class ApprovalRequest {
        private String id;
        private String action;
        private String resource;
        private String requestedBy;
        private String reason;
        private String status;
        private long createdAt;

        public ApprovalRequest(String action, String resource, String requestedBy, String reason) {
            this.id = UUID.randomUUID().toString();
            this.action = action;
            this.resource = resource;
            this.requestedBy = requestedBy;
            this.reason = reason;
            this.status = "pending";
            this.createdAt = System.currentTimeMillis();
        }

        public String getId() { return id; }
        public String getStatus() { return status; }
        public void approve() { this.status = "approved"; }
        public void reject() { this.status = "rejected"; }
    }

    public PermissionBroker() {
        registerDefaultPermissions();
    }

    private void registerDefaultPermissions() {
        register("file.write", "filesystem", "high");
        register("file.delete", "filesystem", "critical");
        register("file.read", "filesystem", "low");
        register("shell.exec", "shell", "critical");
        register("shell.read", "shell", "high");
        register("network.http", "network", "low");
        register("network.ssh", "network", "high");
        register("db.query", "database", "high");
        register("db.write", "database", "critical");
        register("browser.navigate", "browser", "low");
        register("browser.click", "browser", "low");
        register("browser.type", "browser", "low");
        register("email.send", "email", "high");
        register("admin.config", "admin", "critical");
    }

    public void register(String action, String resource, String riskLevel) {
        Permission p = new Permission(action, resource, riskLevel);
        permissions.put(p.key(), p);
    }

    public Permission check(String action, String resource) {
        return permissions.get(action + ":" + resource);
    }

    public ApprovalRequest requestApproval(String action, String resource, String requestedBy, String reason) {
        ApprovalRequest req = new ApprovalRequest(action, resource, requestedBy, reason);
        pendingApprovals.put(req.getId(), req);
        return req;
    }

    public ApprovalRequest approve(String requestId) {
        ApprovalRequest req = pendingApprovals.get(requestId);
        if (req != null) {
            req.approve();
            pendingApprovals.remove(requestId);
        }
        return req;
    }

    public ApprovalRequest reject(String requestId) {
        ApprovalRequest req = pendingApprovals.get(requestId);
        if (req != null) {
            req.reject();
            pendingApprovals.remove(requestId);
        }
        return req;
    }

    public List<ApprovalRequest> getPendingApprovals() {
        return pendingApprovals.values().stream()
            .filter(r -> "pending".equals(r.getStatus()))
            .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getPermissions() {
        return permissions.values().stream()
            .map(p -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("action", p.getAction());
                m.put("resource", p.getResource());
                m.put("riskLevel", p.getRiskLevel());
                m.put("requiresApproval", p.isRequiresApproval());
                return m;
            })
            .collect(Collectors.toList());
    }
}