package com.agentos.evolution.config;

import com.agentos.evolution.market.SkillMarketplace;
import com.agentos.evolution.reflection.ReflectionEngine;
import com.agentos.evolution.skill.SkillEvolutionEngine;
import com.agentos.evolution.sop.SopEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EvolutionConfig {

    @Bean
    public ReflectionEngine reflectionEngine() { return new ReflectionEngine(); }

    @Bean
    public SopEngine sopEngine() { return new SopEngine(); }

    @Bean
    public SkillEvolutionEngine skillEvolutionEngine() { return new SkillEvolutionEngine(); }

    @Bean
    public SkillMarketplace skillMarketplace() { return new SkillMarketplace(); }
}