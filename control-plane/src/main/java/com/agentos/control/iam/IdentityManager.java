package com.agentos.control.iam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class IdentityManager {
    private static final Logger log = LoggerFactory.getLogger(IdentityManager.class);
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public User register(String tenantId, String email, String name, String role) {
        if (users.values().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email) && u.getTenantId().equals(tenantId))) {
            return null;
        }
        User user = new User(tenantId, email, name, role);
        users.put(user.getId(), user);
        log.info("User registered: {} ({})", email, user.getId());
        return user;
    }

    public User authenticate(String email, String password) {
        return users.values().stream()
            .filter(u -> u.getEmail().equalsIgnoreCase(email) && "active".equals(u.getStatus()))
            .findFirst().orElse(null);
    }

    public User get(String id) { return users.get(id); }

    public List<User> listByTenant(String tenantId) {
        return users.values().stream().filter(u -> u.getTenantId().equals(tenantId)).collect(Collectors.toList());
    }

    public void updateRole(String id, String role) {
        User u = users.get(id);
        if (u != null) { u.setRole(role); }
    }

    public void updateStatus(String id, String status) {
        User u = users.get(id);
        if (u != null) { u.setStatus(status); }
    }

    public boolean delete(String id) { return users.remove(id) != null; }

    public Map<String, Object> getStats() {
        return Map.of("total", users.size(),
            "active", users.values().stream().filter(u -> "active".equals(u.getStatus())).count(),
            "roles", users.values().stream().collect(Collectors.groupingBy(User::getRole, Collectors.counting())));
    }
}