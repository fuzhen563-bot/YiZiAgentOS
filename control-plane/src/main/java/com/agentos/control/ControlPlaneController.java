package com.agentos.control;

import com.agentos.control.billing.BillingManager;
import com.agentos.control.iam.IdentityManager;
import com.agentos.control.iam.User;
import com.agentos.control.rbac.AccessControl;
import com.agentos.control.tenant.Tenant;
import com.agentos.control.tenant.TenantManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/control")
public class ControlPlaneController {

    @Autowired private TenantManager tenantManager;
    @Autowired private IdentityManager identityManager;
    @Autowired private AccessControl accessControl;
    @Autowired private BillingManager billingManager;

    // ===== Tenants =====
    @PostMapping("/tenants")
    public ResponseEntity<?> createTenant(@RequestBody Map<String,String> body) {
        Tenant t = tenantManager.create(body.get("name"), body.get("slug"));
        billingManager.subscribe(t.getId(), "free");
        return ResponseEntity.ok(Map.of("id",t.getId(),"name",t.getName(),"plan",t.getPlan()));
    }

    @GetMapping("/tenants")
    public ResponseEntity<?> listTenants() { return ResponseEntity.ok(tenantManager.list()); }

    @GetMapping("/tenants/{id}")
    public ResponseEntity<?> getTenant(@PathVariable String id) {
        Tenant t = tenantManager.get(id);
        return t != null ? ResponseEntity.ok(t) : ResponseEntity.notFound().build();
    }

    @PostMapping("/tenants/{id}/plan")
    public ResponseEntity<?> updatePlan(@PathVariable String id, @RequestParam String plan) {
        tenantManager.updatePlan(id, plan);
        billingManager.changePlan(id, plan);
        return ResponseEntity.ok(Map.of("status","updated","plan",plan));
    }

    @GetMapping("/tenants/stats") public ResponseEntity<?> tenantStats() { return ResponseEntity.ok(tenantManager.getStats()); }

    // ===== Users =====
    @PostMapping("/users/register")
    public ResponseEntity<?> register(@RequestBody Map<String,String> body) {
        User u = identityManager.register(body.get("tenantId"), body.get("email"), body.get("name"), body.get("role"));
        if (u == null) return ResponseEntity.badRequest().body(Map.of("error","Email already exists"));
        return ResponseEntity.ok(Map.of("id",u.getId(),"email",u.getEmail(),"role",u.getRole()));
    }

    @PostMapping("/users/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> body) {
        User u = identityManager.authenticate(body.get("email"), body.get("password"));
        return u != null ? ResponseEntity.ok(Map.of("id",u.getId(),"name",u.getName(),"role",u.getRole()))
            : ResponseEntity.status(401).body(Map.of("error","Invalid credentials"));
    }

    @GetMapping("/users") public ResponseEntity<?> listUsers(@RequestParam String tenantId) { return ResponseEntity.ok(identityManager.listByTenant(tenantId)); }

    @PostMapping("/users/{id}/role") public ResponseEntity<?> updateRole(@PathVariable String id, @RequestParam String role) { identityManager.updateRole(id, role); return ResponseEntity.ok(Map.of("status","updated")); }

    // ===== RBAC =====
    @GetMapping("/roles") public ResponseEntity<?> listRoles() { return ResponseEntity.ok(accessControl.listRoles()); }

    @PostMapping("/roles/check")
    public ResponseEntity<?> checkPermission(@RequestBody Map<String,String> body) {
        boolean allowed = accessControl.checkPermission(body.get("role"), body.get("action"), body.get("resource"));
        return ResponseEntity.ok(Map.of("allowed",allowed));
    }

    @PostMapping("/policies")
    public ResponseEntity<?> addPolicy(@RequestBody Map<String,String> body) {
        var p = accessControl.addPolicy(body.get("name"), body.get("effect"), body.get("action"), body.get("resource"));
        return ResponseEntity.ok(Map.of("id",p.getId()));
    }

    // ===== Billing =====
    @GetMapping("/billing/{tenantId}")
    public ResponseEntity<?> getBilling(@PathVariable String tenantId) { return ResponseEntity.ok(billingManager.getInvoice(tenantId)); }

    @PostMapping("/usage/record")
    public ResponseEntity<?> recordUsage(@RequestBody Map<String,Object> body) {
        billingManager.recordUsage((String)body.get("tenantId"), (String)body.get("resource"), ((Number)body.get("amount")).longValue());
        return ResponseEntity.ok(Map.of("status","recorded"));
    }
}