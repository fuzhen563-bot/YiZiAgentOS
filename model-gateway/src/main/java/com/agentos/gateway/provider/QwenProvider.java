package com.agentos.gateway.provider;

import com.agentos.gateway.core.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;

public class QwenProvider extends AbstractModelProvider {

    public QwenProvider(ProviderConfig config) {
        super(config);
    }

    @Override
    public String getName() {
        return "qwen";
    }

    @Override
    protected void doHealthCheck() {
        restTemplate.getForEntity(config.getEndpoint() + "/api/v1/models", String.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ModelResponse call(ModelRequest request) {
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> body = Map.of(
                "model", request.getModelId(),
                "input", Map.of("messages", List.of(
                    Map.of("role", "user", "content", request.getPrompt())
                )),
                "parameters", Map.of(
                    "max_tokens", request.getMaxTokens(),
                    "temperature", request.getTemperature()
                )
            );
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeaders());
            var response = restTemplate.exchange(
                config.getEndpoint() + "/api/v1/services/aigc/text-generation/generation",
                HttpMethod.POST, entity, Map.class
            );
            Map<String, Object> result = response.getBody();
            if (result == null) {
                return new ModelResponse().failure("Qwen returned empty response");
            }
            Map<String, Object> output = (Map<String, Object>) result.get("output");
            Map<String, Object> usage = (Map<String, Object>) result.get("usage");

            ModelResponse mr = new ModelResponse()
                .success((String) result.get("request_id"),
                         output != null ? (String) output.get("text") : "",
                         request.getModelId(), getName());
            if (usage != null) {
                mr.setPromptTokens(toInt(usage.get("input_tokens")));
                mr.setCompletionTokens(toInt(usage.get("output_tokens")));
                mr.setTotalTokens(mr.getPromptTokens() + mr.getCompletionTokens());
            }
            mr.setLatencyMs(System.currentTimeMillis() - start);
            return mr;
        } catch (Exception e) {
            health.markUnhealthy(e.getMessage());
            return new ModelResponse().failure("Qwen call failed: " + e.getMessage());
        }
    }

    private int toInt(Object val) {
        if (val instanceof Number n) return n.intValue();
        return 0;
    }
}