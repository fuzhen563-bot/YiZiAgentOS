package com.agentos.gateway.provider;

import com.agentos.gateway.core.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;

public class OpenAIProvider extends AbstractModelProvider {

    public OpenAIProvider(ProviderConfig config) {
        super(config);
    }

    @Override
    public String getName() {
        return "openai";
    }

    @Override
    protected void doHealthCheck() {
        restTemplate.getForEntity(config.getEndpoint() + "/models", String.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ModelResponse call(ModelRequest request) {
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> body = Map.of(
                "model", request.getModelId(),
                "messages", List.of(Map.of("role", "user", "content", request.getPrompt())),
                "max_tokens", request.getMaxTokens(),
                "temperature", request.getTemperature()
            );
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeaders());
            var response = restTemplate.exchange(
                config.getEndpoint() + "/v1/chat/completions",
                HttpMethod.POST, entity, Map.class
            );
            Map<String, Object> result = response.getBody();
            if (result == null) {
                return new ModelResponse().failure("OpenAI returned empty response");
            }
            List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
            if (choices == null || choices.isEmpty()) {
                return new ModelResponse().failure("OpenAI returned no choices");
            }
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            Map<String, Object> usage = (Map<String, Object>) result.get("usage");

            ModelResponse mr = new ModelResponse()
                .success((String) result.get("id"),
                         message != null ? (String) message.get("content") : "",
                         request.getModelId(), getName());
            if (usage != null) {
                mr.setPromptTokens(toInt(usage.get("prompt_tokens")));
                mr.setCompletionTokens(toInt(usage.get("completion_tokens")));
                mr.setTotalTokens(toInt(usage.get("total_tokens")));
            }
            mr.setLatencyMs(System.currentTimeMillis() - start);
            return mr;
        } catch (Exception e) {
            health.markUnhealthy(e.getMessage());
            return new ModelResponse().failure("OpenAI call failed: " + e.getMessage());
        }
    }

    private int toInt(Object val) {
        if (val instanceof Number n) return n.intValue();
        return 0;
    }
}