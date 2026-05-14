package com.agentos.gateway.provider;

import com.agentos.gateway.core.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.util.Map;

public class LocalLLMProvider extends AbstractModelProvider {

    public LocalLLMProvider(ProviderConfig config) {
        super(config);
    }

    @Override
    public String getName() {
        return "local";
    }

    @Override
    protected void doHealthCheck() {
        restTemplate.getForEntity(config.getEndpoint() + "/health", String.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ModelResponse call(ModelRequest request) {
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> body = Map.of(
                "model", request.getModelId(),
                "prompt", request.getPrompt(),
                "max_tokens", request.getMaxTokens(),
                "temperature", request.getTemperature()
            );
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeaders());
            var response = restTemplate.exchange(
                config.getEndpoint() + "/v1/completions",
                HttpMethod.POST, entity, Map.class
            );
            Map<String, Object> result = response.getBody();
            if (result == null) {
                return new ModelResponse().failure("Local LLM returned empty response");
            }
            Map<String, Object> usage = (Map<String, Object>) result.get("usage");

            ModelResponse mr = new ModelResponse()
                .success((String) result.get("id"),
                         result.get("response") != null ? result.get("response").toString() : "",
                         request.getModelId(), getName());
            if (usage != null) {
                mr.setPromptTokens(toInt(usage.get("prompt_tokens")));
                mr.setCompletionTokens(toInt(usage.get("completion_tokens")));
                mr.setTotalTokens(mr.getPromptTokens() + mr.getCompletionTokens());
            }
            mr.setLatencyMs(System.currentTimeMillis() - start);
            return mr;
        } catch (Exception e) {
            health.markUnhealthy(e.getMessage());
            return new ModelResponse().failure("Local LLM call failed: " + e.getMessage());
        }
    }

    private int toInt(Object val) {
        if (val instanceof Number n) return n.intValue();
        return 0;
    }
}