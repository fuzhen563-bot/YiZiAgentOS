package com.agentos.gateway.router;

import com.agentos.gateway.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ModelRouter {
    private static final Logger log = LoggerFactory.getLogger(ModelRouter.class);
    private final Map<String, List<ModelProvider>> modelMap = new ConcurrentHashMap<>();
    private final Map<String, Router> routers = new ConcurrentHashMap<>();
    private final Map<String, RoutingStrategy> modelStrategies = new ConcurrentHashMap<>();

    public ModelRouter() {
        routers.put("cost", new CostPriorityRouter());
        routers.put("speed", new SpeedPriorityRouter());
        routers.put("hybrid", new HybridRouter());
    }

    public void registerProvider(ModelProvider provider, String modelId) {
        modelMap.computeIfAbsent(modelId, k -> Collections.synchronizedList(new ArrayList<>())).add(provider);
        log.info("Registered provider {} for model {}", provider.getName(), modelId);
    }

    public void setStrategy(String modelId, RoutingStrategy strategy) {
        modelStrategies.put(modelId, strategy);
    }

    public ModelProvider resolve(ModelRequest request) {
        List<ModelProvider> candidates = modelMap.get(request.getModelId());
        if (candidates == null || candidates.isEmpty()) {
            log.warn("No providers registered for model {}", request.getModelId());
            return null;
        }
        RoutingStrategy strategy = modelStrategies.getOrDefault(request.getModelId(), RoutingStrategy.HYBRID);
        String routerKey = switch (strategy) {
            case COST_PRIORITY -> "cost";
            case SPEED_PRIORITY -> "speed";
            case FIXED -> "cost";
            default -> "hybrid";
        };
        Router router = routers.get(routerKey);
        if (router == null) {
            router = routers.get("hybrid");
        }
        ModelProvider selected = router.route(candidates, request);
        if (selected != null) {
            log.debug("Routed model {} to provider {} via {} strategy",
                request.getModelId(), selected.getName(), strategy);
        }
        return selected;
    }

    public List<String> getAvailableModels() {
        return modelMap.entrySet().stream()
            .filter(e -> e.getValue().stream().anyMatch(ModelProvider::isAvailable))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
}