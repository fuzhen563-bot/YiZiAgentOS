package com.agentos.gateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

public class PromptInjectionDetector {
    private static final Logger log = LoggerFactory.getLogger(PromptInjectionDetector.class);
    private static final List<Pattern> INJECTION_PATTERNS = List.of(
        Pattern.compile("ignore\\s+(all\\s+)?previous\\s+instructions", Pattern.CASE_INSENSITIVE),
        Pattern.compile("forget\\s+(all\\s+)?(previous|prior)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("you\\s+are\\s+(now|free|released)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("system\\s+prompt\\s*:", Pattern.CASE_INSENSITIVE),
        Pattern.compile("ACT\\s+AS\\s+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("DAN|do\\s+anything\\s+now", Pattern.CASE_INSENSITIVE)
    );

    public boolean isInjectionAttempt(String prompt) {
        if (prompt == null) return false;
        for (Pattern pattern : INJECTION_PATTERNS) {
            if (pattern.matcher(prompt).find()) {
                log.warn("Prompt injection detected: {}", pattern.pattern());
                return true;
            }
        }
        return false;
    }
}