package com.agentos.control.rbac;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AccessControl {
    private final Map<String, Role> roles = new ConcurrentHashMap<>();
    private final Map<String, Policy> policies = new ConcurrentHashMap<>();

    public static class Role {
        private String id;
        private String name;
        private Set<String> permissions;

        public Role(String name, Set<String> permissions) {
            this.id = UUID.randomUUID().toString();
            this.name = name;
            this.permissions = permissions;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public Set<String> getPermissions() { return permissions; }
    }

    public static class Policy {
        private String id;
        private String name;
        private String effect;
        private String action;
        private String resource;
        private Map<String, Object> conditions;

        public Policy(String name, String effect, String action, String resource) {
            this.id = UUID.randomUUID().toString();
            this.name = name;
            this.effect = effect;
            this.action = action;
            this.resource = resource;
            this.conditions = new HashMap<>();
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getEffect() { return effect; }
        public String getAction() { return action; }
        public String getResource() { return resource; }
        public Map<String, Object> getConditions() { return conditions; }
        public void setConditions(Map<String, Object> c) { conditions = c; }

        public boolean matches(String action, String resource) {
            return wildcardMatch(this.action, action) && wildcardMatch(this.resource, resource);
        }

        private boolean wildcardMatch(String p, String v) {
            if ("*".equals(p)) return true;
            if (p.endsWith("*")) return v.startsWith(p.substring(0, p.length()-1));
            return p.equals(v);
        }
    }

    public AccessControl() { initDefaultRoles(); }

    private void initDefaultRoles() {
        addRole("admin", Set.of("*"));
        addRole("owner", Set.of("tenant.*", "billing.*", "user.*", "agent.*", "tool.*", "audit.*"));
        addRole("editor", Set.of("agent.*", "tool.*", "knowledge.*", "chat.*"));
        addRole("viewer", Set.of("chat.*", "knowledge.read"));
        addRole("member", Set.of("chat.*", "agent.execute", "knowledge.read"));
    }

    public void addRole(String name, Set<String> permissions) {
        roles.put(name, new Role(name, permissions));
    }

    public Role getRole(String name) { return roles.get(name); }

    public boolean checkPermission(String roleName, String action, String resource) {
        Role r = roles.get(roleName);
        if (r == null) return false;
        String target = action + ":" + resource;
        return r.getPermissions().stream().anyMatch(p -> {
            if (p.equals("*")) return true;
            if (p.endsWith(".*")) {
                String prefix = p.substring(0, p.length()-1);
                return target.startsWith(prefix);
            }
            return p.equals(target) || p.equals(action) || p.equals("*:" + resource);
        });
    }

    public Policy addPolicy(String name, String effect, String action, String resource) {
        Policy p = new Policy(name, effect, action, resource);
        policies.put(p.getId(), p);
        return p;
    }

    public String evaluatePolicy(String action, String resource, Map<String, Object> context) {
        for (Policy p : policies.values()) {
            if (p.matches(action, resource) && "deny".equals(p.getEffect())) return "deny";
        }
        return "allow";
    }

    public List<Map<String, Object>> listRoles() {
        return roles.values().stream().map(r -> Map.of("id", r.getId(), "name", r.getName(), "permissions", r.getPermissions())).toList();
    }
}