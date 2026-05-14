package com.agentos.gateway.provider;

import com.agentos.gateway.core.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;

public class ClaudeProvider extends AbstractModelProvider {

    public ClaudeProvider(ProviderConfig config) {
        super(config);
    }

    @Override
    public String getName() {
        return "claude";
    }

    @Override
    protected void doHealthCheck() {
        restTemplate.getForEntity(config.getEndpoint() + "/v1/models", String.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ModelResponse call(ModelRequest request) {
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> body = Map.of(
                "model", request.getModelId(),
                "max_tokens", request.getMaxTokens(),
                "messages", List.of(Map.of("role", "user", "content", request.getPrompt()))
            );
            HttpHeaders headers = buildHeaders();
            headers.set("anthropic-version", "2023-06-01");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            var response = restTemplate.exchange(
                config.getEndpoint() + "/v1/messages",
                HttpMethod.POST, entity, Map.class
            );
            Map<String, Object> result = response.getBody();
            if (result == null) {
                return new ModelResponse().failure("Claude returned empty response");
            }
            List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");
            Map<String, Object> usage = (Map<String, Object>) result.get("usage");

            String text = "";
            if (content != null && !content.isEmpty() && content.get(0) != null) {
                Object t = content.get(0).get("text");
                if (t != null) text = t.toString();
            }
            ModelResponse mr = new ModelResponse()
                .success((String) result.get("id"), text, request.getModelId(), getName());
            if (usage != null) {
                mr.setPromptTokens(toInt(usage.get("input_tokens")));
                mr.setCompletionTokens(toInt(usage.get("output_tokens")));
                mr.setTotalTokens(mr.getPromptTokens() + mr.getCompletionTokens());
            }
            mr.setLatencyMs(System.currentTimeMillis() - start);
            return mr;
        } catch (Exception e) {
            health.markUnhealthy(e.getMessage());
            return new ModelResponse().failure("Claude call failed: " + e.getMessage());
        }
    }

    private int toInt(Object val) {
        if (val instanceof Number n) return n.intValue();
        return 0;
    }
}