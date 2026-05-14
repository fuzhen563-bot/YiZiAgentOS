package com.agentos.gateway;

import com.agentos.gateway.core.ModelGatewayService;
import com.agentos.gateway.core.ModelRequest;
import com.agentos.gateway.core.ModelResponse;
import com.agentos.gateway.core.ModelRole;
import com.agentos.gateway.router.ModelRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/gateway")
public class GatewayController {

    @Autowired
    private ModelGatewayService gatewayService;

    @PostMapping("/chat")
    public ResponseEntity<ModelResponse> chat(@RequestBody ChatRequest request) {
        ModelRequest modelRequest = new ModelRequest(request.model, ModelRole.CHAT, request.prompt);
        modelRequest.setUserId(request.userId);
        modelRequest.setWorkspaceId(request.workspaceId);
        modelRequest.setMaxTokens(request.maxTokens);
        modelRequest.setTemperature(request.temperature);
        ModelResponse response = gatewayService.call(modelRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/models")
    public ResponseEntity<?> getModels() {
        return ResponseEntity.ok(gatewayService.getRouter().getAvailableModels());
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "providers",
            gatewayService.getProviders().stream().map(p -> Map.of(
                "name", p.getName(),
                "available", p.isAvailable()
            )).toList()));
    }

    @GetMapping("/usage")
    public ResponseEntity<?> getUsage(@RequestParam String workspaceId) {
        return ResponseEntity.ok(gatewayService.getBillingService().getUsageReport(workspaceId));
    }

    public static class ChatRequest {
        public String model;
        public String prompt;
        public String userId;
        public String workspaceId;
        public int maxTokens = 4096;
        public double temperature = 0.7;
    }
}