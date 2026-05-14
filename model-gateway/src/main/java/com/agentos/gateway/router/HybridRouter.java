package com.agentos.gateway.router;

import com.agentos.gateway.core.ModelProvider;
import com.agentos.gateway.core.ModelRequest;
import java.util.Comparator;
import java.util.List;

public class HybridRouter implements Router {
    private static final double COST_WEIGHT = 0.4;
    private static final double SPEED_WEIGHT = 0.6;

    @Override
    public ModelProvider route(List<ModelProvider> candidates, ModelRequest request) {
        double maxCost = candidates.stream()
            .mapToDouble(p -> p.getConfig().getCostPer1kTokens()).max().orElse(1);
        long maxLatency = candidates.stream()
            .mapToLong(p -> p.checkHealth().getLatencyMs()).max().orElse(1);

        return candidates.stream()
            .filter(ModelProvider::isAvailable)
            .min(Comparator.comparingDouble(p -> {
                double costScore = (p.getConfig().getCostPer1kTokens() / maxCost) * COST_WEIGHT;
                double speedScore = ((double) p.checkHealth().getLatencyMs() / maxLatency) * SPEED_WEIGHT;
                return costScore + speedScore;
            }))
            .orElse(null);
    }
}