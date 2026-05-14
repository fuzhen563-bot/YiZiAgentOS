package com.agentos.secure;

import com.agentos.secure.browser.BrowserAutomation;
import com.agentos.secure.policy.PermissionBroker;
import com.agentos.secure.policy.PolicyGuard;
import com.agentos.secure.rollback.RollbackManager;
import com.agentos.secure.sandbox.SandboxEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/secure")
public class SecureExecutionController {

    @Autowired private BrowserAutomation browser;
    @Autowired private SandboxEnvironment sandbox;
    @Autowired private PermissionBroker permissionBroker;
    @Autowired private PolicyGuard policyGuard;
    @Autowired private RollbackManager rollbackManager;

    // ===== Browser =====

    @PostMapping("/browser/session")
    public ResponseEntity<?> createSession() {
        var session = browser.createSession();
        return ResponseEntity.ok(Map.of("sessionId", session.getId()));
    }

    @PostMapping("/browser/navigate")
    public ResponseEntity<?> navigate(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(browser.navigate(body.get("sessionId"), body.get("url")));
    }

    @PostMapping("/browser/click")
    public ResponseEntity<?> click(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(browser.click(body.get("sessionId"), body.get("selector")));
    }

    @PostMapping("/browser/type")
    public ResponseEntity<?> type(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(browser.type(body.get("sessionId"), body.get("selector"), body.get("text")));
    }

    @PostMapping("/browser/screenshot")
    public ResponseEntity<?> screenshot(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(browser.screenshot(body.get("sessionId")));
    }

    @PostMapping("/browser/evaluate")
    public ResponseEntity<?> evaluateJS(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(browser.evaluateJS(body.get("sessionId"), body.get("script")));
    }

    @GetMapping("/browser/sessions")
    public ResponseEntity<?> listSessions() {
        return ResponseEntity.ok(browser.listSessions());
    }

    // ===== Sandbox =====

    @PostMapping("/sandbox/create")
    public ResponseEntity<?> createSandbox(@RequestBody Map<String, Object> body) {
        String image = (String) body.getOrDefault("image", "ubuntu:22.04");
        Map<String, String> env = (Map<String, String>) body.get("env");
        var instance = sandbox.create(image, env);
        return ResponseEntity.ok(Map.of("sandboxId", instance.getId()));
    }

    @PostMapping("/sandbox/exec")
    public ResponseEntity<?> exec(@RequestBody Map<String, Object> body) {
        Object timeoutObj = body.getOrDefault("timeout", 30);
        int timeout = timeoutObj instanceof Number n ? n.intValue() : 30;
        return ResponseEntity.ok(sandbox.executeCommand(
            (String) body.get("sandboxId"),
            (String) body.get("command"),
            timeout));
    }

    @DeleteMapping("/sandbox/{id}")
    public ResponseEntity<?> destroySandbox(@PathVariable String id) {
        sandbox.destroy(id);
        return ResponseEntity.ok(Map.of("status", "destroyed"));
    }

    @GetMapping("/sandbox/instances")
    public ResponseEntity<?> listSandboxes() {
        return ResponseEntity.ok(sandbox.listInstances());
    }

    // ===== Policy =====

    @GetMapping("/policy/permissions")
    public ResponseEntity<?> listPermissions() {
        return ResponseEntity.ok(permissionBroker.getPermissions());
    }

    @PostMapping("/policy/evaluate")
    public ResponseEntity<?> evaluate(@RequestBody Map<String, Object> body) {
        String action = (String) body.get("action");
        String resource = (String) body.get("resource");
        Map<String, Object> context = (Map<String, Object>) body.getOrDefault("context", Map.of());
        return ResponseEntity.ok(policyGuard.evaluate(action, resource, context));
    }

    @PostMapping("/policy/approval/request")
    public ResponseEntity<?> requestApproval(@RequestBody Map<String, String> body) {
        var req = permissionBroker.requestApproval(
            body.get("action"), body.get("resource"), body.get("requestedBy"), body.get("reason"));
        return ResponseEntity.ok(Map.of("approvalId", req.getId(), "status", req.getStatus()));
    }

    @PostMapping("/policy/approval/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable String id) {
        var req = permissionBroker.approve(id);
        return ResponseEntity.ok(Map.of("status", req != null ? "approved" : "not_found"));
    }

    @PostMapping("/policy/approval/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable String id) {
        var req = permissionBroker.reject(id);
        return ResponseEntity.ok(Map.of("status", req != null ? "rejected" : "not_found"));
    }

    @GetMapping("/policy/approvals/pending")
    public ResponseEntity<?> pendingApprovals() {
        return ResponseEntity.ok(permissionBroker.getPendingApprovals());
    }

    // ===== Rollback =====

    @PostMapping("/rollback/begin")
    public ResponseEntity<?> beginOperation(@RequestBody Map<String, Object> body) {
        Map<String, Object> before = (Map<String, Object>) body.getOrDefault("beforeState", Map.of());
        var op = rollbackManager.beginOperation(
            (String) body.get("sessionId"),
            (String) body.get("type"),
            (String) body.get("target"),
            before);
        return ResponseEntity.ok(Map.of("operationId", op.getId()));
    }

    @PostMapping("/rollback/{sessionId}/{operationId}")
    public ResponseEntity<?> rollback(@PathVariable String sessionId, @PathVariable String operationId) {
        boolean ok = rollbackManager.rollback(sessionId, operationId);
        return ResponseEntity.ok(Map.of("status", ok ? "rolled_back" : "not_found"));
    }

    @PostMapping("/rollback/{sessionId}/all")
    public ResponseEntity<?> rollbackAll(@PathVariable String sessionId) {
        rollbackManager.rollbackAll(sessionId);
        return ResponseEntity.ok(Map.of("status", "all_rolled_back"));
    }
}