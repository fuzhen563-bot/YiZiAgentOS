package com.agentos.gateway.core;

import com.agentos.gateway.billing.BillingService;
import com.agentos.gateway.billing.CostCalculator;
import com.agentos.gateway.billing.TokenCounter;
import com.agentos.gateway.circuitbreaker.FailoverManager;
import com.agentos.gateway.router.ModelRouter;
import com.agentos.gateway.security.ContentFilter;
import com.agentos.gateway.security.PromptInjectionDetector;
import com.agentos.gateway.security.SensitiveDataMasker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ModelGatewayService {
    private static final Logger log = LoggerFactory.getLogger(ModelGatewayService.class);
    private final ModelRouter router;
    private final FailoverManager failoverManager;
    private final BillingService billingService;
    private final ContentFilter contentFilter;
    private final PromptInjectionDetector injectionDetector;
    private final SensitiveDataMasker dataMasker;
    private final CostCalculator costCalculator;
    private final List<ModelProvider> providers;

    public ModelGatewayService() {
        this.router = new ModelRouter();
        this.failoverManager = new FailoverManager();
        this.contentFilter = new ContentFilter();
        this.injectionDetector = new PromptInjectionDetector();
        this.dataMasker = new SensitiveDataMasker();
        this.costCalculator = new CostCalculator();
        TokenCounter tokenCounter = new TokenCounter();
        this.billingService = new BillingService(tokenCounter, costCalculator);
        this.providers = new ArrayList<>();
    }

    public void registerProvider(ModelProvider provider, List<String> modelIds) {
        providers.add(provider);
        for (String modelId : modelIds) {
            router.registerProvider(provider, modelId);
        }
        failoverManager.registerProvider(provider.getName(), 3, 30000);
        failoverManager.setFailoverOrder(provider.getName(), providers.stream().map(ModelProvider::getName).toList());
    }

    public ModelResponse call(ModelRequest request) {
        String prompt = request.getPrompt();
        if (prompt == null) return new ModelResponse().failure("Empty prompt");

        if (injectionDetector.isInjectionAttempt(prompt)) {
            return new ModelResponse().failure("Prompt injection detected");
        }
        String filteredPrompt = contentFilter.filter(prompt);
        if (filteredPrompt == null) {
            return new ModelResponse().failure("Content blocked by security filter");
        }
        request.setPrompt(dataMasker.mask(filteredPrompt));

        ModelProvider preferred = router.resolve(request);
        if (preferred != null) {
            failoverManager.setFailoverOrder(request.getModelId(), resolveFailoverOrder(request.getModelId(), preferred));
        }

        ModelResponse response = failoverManager.executeWithFailover(request, providers);
        if (response.isSuccess()) {
            double cost = costCalculator.calculate(response,
                providers.stream().filter(p -> p.getName().equals(response.getProvider()))
                    .findFirst().map(ModelProvider::getConfig).orElse(null));
            response.setCost(cost);
            billingService.recordUsage(response, request.getUserId(), request.getWorkspaceId());
        }
        return response;
    }

    public ModelRouter getRouter() { return router; }
    public BillingService getBillingService() { return billingService; }
    public List<ModelProvider> getProviders() { return providers; }

    private List<String> resolveFailoverOrder(String modelId, ModelProvider preferred) {
        List<String> order = new ArrayList<>();
        order.add(preferred.getName());
        for (ModelProvider p : providers) {
            if (!p.getName().equals(preferred.getName())) {
                order.add(p.getName());
            }
        }
        return order;
    }
}