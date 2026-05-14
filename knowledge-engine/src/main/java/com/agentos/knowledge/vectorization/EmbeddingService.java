package com.agentos.knowledge.vectorization;

import java.util.List;

public interface EmbeddingService {
    float[] embed(String text);
    List<Float> embedAsync(String text);
    List<float[]> embedBatch(List<String> texts);
    int dimensions();
}