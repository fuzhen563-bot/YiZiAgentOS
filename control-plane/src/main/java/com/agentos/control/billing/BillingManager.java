package com.agentos.control.billing;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BillingManager {
    private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();
    private final Map<String, List<UsageRecord>> usage = new ConcurrentHashMap<>();

    public static class Subscription {
        private String id;
        private String tenantId;
        private String plan;
        private String status;
        private double monthlyPrice;
        private LocalDateTime startedAt;
        private LocalDateTime nextBillingAt;

        public Subscription(String tenantId, String plan) {
            this.id = UUID.randomUUID().toString();
            this.tenantId = tenantId;
            this.plan = plan;
            this.status = "active";
            this.monthlyPrice = switch (plan) {
                case "free" -> 0; case "pro" -> 99; case "enterprise" -> 499; default -> 0;
            };
            this.startedAt = LocalDateTime.now();
            this.nextBillingAt = startedAt.plusMonths(1);
        }

        public String getId() { return id; }
        public String getTenantId() { return tenantId; }
        public String getPlan() { return plan; }
        public void setPlan(String plan) { this.plan = plan; monthlyPrice = switch (plan) { case "free"->0; case "pro"->99; case "enterprise"->499; default->0; }; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public double getMonthlyPrice() { return monthlyPrice; }
        public LocalDateTime getStartedAt() { return startedAt; }
        public LocalDateTime getNextBillingAt() { return nextBillingAt; }
    }

    public static class UsageRecord {
        private String id;
        private String tenantId;
        private String resource;
        private long amount;
        private LocalDateTime recordedAt;

        public UsageRecord(String tenantId, String resource, long amount) {
            this.id = UUID.randomUUID().toString();
            this.tenantId = tenantId;
            this.resource = resource;
            this.amount = amount;
            this.recordedAt = LocalDateTime.now();
        }
        public String getResource() { return resource; }
        public long getAmount() { return amount; }
        public LocalDateTime getRecordedAt() { return recordedAt; }
    }

    public Subscription subscribe(String tenantId, String plan) {
        Subscription sub = new Subscription(tenantId, plan);
        subscriptions.put(sub.getId(), sub);
        return sub;
    }

    public Subscription getSubscription(String tenantId) {
        return subscriptions.values().stream().filter(s -> s.getTenantId().equals(tenantId)).findFirst().orElse(null);
    }

    public void changePlan(String tenantId, String plan) {
        Subscription sub = getSubscription(tenantId);
        if (sub != null) sub.setPlan(plan);
    }

    public void recordUsage(String tenantId, String resource, long amount) {
        usage.computeIfAbsent(tenantId, k -> Collections.synchronizedList(new ArrayList<>())).add(new UsageRecord(tenantId, resource, amount));
    }

    public Map<String, Long> getUsageSummary(String tenantId) {
        List<UsageRecord> records = usage.getOrDefault(tenantId, List.of());
        Map<String, Long> summary = new HashMap<>();
        for (UsageRecord r : records) {
            summary.merge(r.getResource(), r.getAmount(), Long::sum);
        }
        return summary;
    }

    public Map<String, Object> getInvoice(String tenantId) {
        Subscription sub = getSubscription(tenantId);
        if (sub == null) return Map.of("error", "No subscription");
        Map<String, Long> usageSummary = getUsageSummary(tenantId);
        double overage = usageSummary.values().stream().mapToLong(Long::longValue).sum() * 0.0001;
        return Map.of("plan", sub.getPlan(), "basePrice", sub.getMonthlyPrice(), "overage", Math.round(overage * 100) / 100.0,
            "total", Math.round((sub.getMonthlyPrice() + overage) * 100) / 100.0, "period", sub.getStartedAt().toString());
    }
}