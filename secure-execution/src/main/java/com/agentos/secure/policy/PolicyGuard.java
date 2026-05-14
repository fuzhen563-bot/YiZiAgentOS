package com.agentos.secure.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class PolicyGuard {
    private static final Logger log = LoggerFactory.getLogger(PolicyGuard.class);
    private final Map<String, PolicyRule> rules = new ConcurrentHashMap<>();

    public static class PolicyRule {
        private String id;
        private String name;
        private String action;
        private String resource;
        private String effect;
        private List<String> conditions;
        private Map<String, Object> config;

        public PolicyRule() {}

        public PolicyRule(String name, String action, String resource, String effect) {
            this.id = UUID.randomUUID().toString();
            this.name = name;
            this.action = action;
            this.resource = resource;
            this.effect = effect;
            this.conditions = new ArrayList<>();
            this.config = new HashMap<>();
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getAction() { return action; }
        public String getResource() { return resource; }
        public String getEffect() { return effect; }
        public List<String> getConditions() { return conditions; }
        public void setConditions(List<String> conditions) { this.conditions = conditions; }
        public Map<String, Object> getConfig() { return config; }
        public void setConfig(Map<String, Object> config) { this.config = config; }

        public boolean matches(String action, String resource) {
            return wildcardMatch(this.action, action) && wildcardMatch(this.resource, resource);
        }

        private boolean wildcardMatch(String pattern, String value) {
            if ("*".equals(pattern)) return true;
            if (pattern.endsWith("*")) return value.startsWith(pattern.substring(0, pattern.length() - 1));
            return pattern.equals(value);
        }
    }

    public PolicyGuard() {
        registerDefaultRules();
    }

    private void registerDefaultRules() {
        addRule(new PolicyRule("block-dangerous-shell", "shell.exec", "*", "deny"));
        PolicyRule blockRmRf = new PolicyRule("block-rm-rf", "shell.exec", "*", "deny");
        blockRmRf.setConditions(List.of("command contains 'rm -rf'"));
        addRule(blockRmRf);
        PolicyRule blockFormat = new PolicyRule("block-format", "shell.exec", "*", "deny");
        blockFormat.setConditions(List.of("command contains 'mkfs'", "command contains 'format'"));
        addRule(blockFormat);
        addRule(new PolicyRule("allow-browser", "browser.*", "*", "allow"));
        addRule(new PolicyRule("allow-file-read", "file.read", "*", "allow"));
        addRule(new PolicyRule("require-approval-email", "email.send", "*", "require_approval"));
        addRule(new PolicyRule("require-approval-db-write", "db.write", "*", "require_approval"));
        addRule(new PolicyRule("block-admin-config", "admin.config", "*", "require_approval"));
    }

    public void addRule(PolicyRule rule) {
        rules.put(rule.getId(), rule);
    }

    public void removeRule(String id) {
        rules.remove(id);
    }

    public List<PolicyRule> getAllRules() {
        return List.copyOf(rules.values());
    }

    public Decision evaluate(String action, String resource, Map<String, Object> context) {
        for (PolicyRule rule : rules.values()) {
            if (rule.matches(action, resource)) {
                String effect = rule.getEffect();
                List<String> conditions = rule.getConditions();
                if (conditions != null && !conditions.isEmpty()) {
                    boolean allMet = conditions.stream().allMatch(c -> evaluateCondition(c, context));
                    if (!allMet) continue;
                }
                return switch (effect) {
                    case "deny" -> new Decision(false, true, null, "Blocked by policy: " + rule.getName());
                    case "require_approval" -> new Decision(false, false, rule.getId(), "Requires approval: " + rule.getName());
                    default -> new Decision(true, false, null, null);
                };
            }
        }
        return new Decision(true, false, null, null);
    }

    private boolean evaluateCondition(String condition, Map<String, Object> context) {
        return true;
    }

    public static class Decision {
        private final boolean allowed;
        private final boolean blocked;
        private final String requiredRuleId;
        private final String reason;

        public Decision(boolean allowed, boolean blocked, String ruleId, String reason) {
            this.allowed = allowed;
            this.blocked = blocked;
            this.requiredRuleId = ruleId;
            this.reason = reason;
        }

        public boolean isAllowed() { return allowed; }
        public boolean isBlocked() { return blocked; }
        public boolean requiresApproval() { return requiredRuleId != null; }
        public String getRequiredRuleId() { return requiredRuleId; }
        public String getReason() { return reason; }
    }
}