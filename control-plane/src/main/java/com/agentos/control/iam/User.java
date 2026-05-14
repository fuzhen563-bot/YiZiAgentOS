package com.agentos.control.iam;

import java.time.LocalDateTime;
import java.util.*;

public class User {
    private String id;
    private String tenantId;
    private String email;
    private String passwordHash;
    private String name;
    private String role;
    private String status;
    private Map<String, Object> profile;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    public User() {}

    public User(String tenantId, String email, String name, String role) {
        this.id = UUID.randomUUID().toString();
        this.tenantId = tenantId;
        this.email = email;
        this.name = name;
        this.role = role;
        this.status = "active";
        this.profile = new HashMap<>();
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getTenantId() { return tenantId; }
    public String getEmail() { return email; }  public void setEmail(String e) { email = e; }
    public String getPasswordHash() { return passwordHash; }  public void setPasswordHash(String p) { passwordHash = p; }
    public String getName() { return name; }  public void setName(String n) { name = n; }
    public String getRole() { return role; }  public void setRole(String r) { role = r; }
    public String getStatus() { return status; }  public void setStatus(String s) { status = s; }
    public Map<String, Object> getProfile() { return profile; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime l) { lastLogin = l; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}