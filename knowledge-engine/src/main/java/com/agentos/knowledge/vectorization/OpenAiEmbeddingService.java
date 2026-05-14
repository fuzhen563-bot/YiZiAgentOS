package com.agentos.knowledge.vectorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import java.util.*;

public class OpenAiEmbeddingService implements EmbeddingService {
    private static final Logger log = LoggerFactory.getLogger(OpenAiEmbeddingService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiKey;
    private final String model;

    public OpenAiEmbeddingService(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public float[] embed(String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            Map<String, Object> body = Map.of("model", model, "input", text);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            var response = restTemplate.postForEntity(
                "https://api.openai.com/v1/embeddings",
                entity, Map.class
            );
            Map<String, Object> result = response.getBody();
            if (result == null || result.get("data") == null) {
                log.error("Embedding API returned null response");
                return new float[1536];
            }
            List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
            List<Double> embedding = (List<Double>) data.get(0).get("embedding");
            float[] floats = new float[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                floats[i] = embedding.get(i).floatValue();
            }
            return floats;
        } catch (Exception e) {
            log.error("Embedding failed: {}", e.getMessage());
            return new float[1536];
        }
    }

    @Override
    public List<Float> embedAsync(String text) {
        float[] result = embed(text);
        List<Float> list = new ArrayList<>(result.length);
        for (float f : result) list.add(f);
        return list;
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        return texts.stream().map(this::embed).toList();
    }

    @Override
    public int dimensions() { return 1536; }
}