package com.agentos.control.tenant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TenantManager {
    private static final Logger log = LoggerFactory.getLogger(TenantManager.class);
    private final Map<String, Tenant> tenants = new ConcurrentHashMap<>();

    public Tenant create(String name, String slug) {
        Tenant t = new Tenant(name, slug);
        tenants.put(t.getId(), t);
        log.info("Tenant created: {} ({})", name, t.getId());
        return t;
    }

    public Tenant get(String id) { return tenants.get(id); }
    public Tenant getBySlug(String slug) { return tenants.values().stream().filter(t -> t.getSlug().equals(slug)).findFirst().orElse(null); }

    public List<Tenant> list() { return tenants.values().stream().sorted((a,b)->b.getCreatedAt().compareTo(a.getCreatedAt())).collect(Collectors.toList()); }

    public void updatePlan(String id, String plan) {
        Tenant t = tenants.get(id);
        if (t != null) {
            t.setPlan(plan);
            switch (plan) {
                case "free" -> { t.setQuota("users",10); t.setQuota("agents",5); t.setQuota("storage_gb",2); }
                case "pro" -> { t.setQuota("users",50); t.setQuota("agents",20); t.setQuota("storage_gb",20); }
                case "enterprise" -> { t.setQuota("users",1000); t.setQuota("agents",100); t.setQuota("storage_gb",500); }
            }
        }
    }

    public boolean delete(String id) { return tenants.remove(id) != null; }
    public Map<String,Object> getStats() {
        return Map.of("total",tenants.size(),"active",tenants.values().stream().filter(t->"active".equals(t.getStatus())).count(),
            "plans",tenants.values().stream().collect(Collectors.groupingBy(Tenant::getPlan,Collectors.counting())));
    }
}