package com.agentos.gateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Pattern;

public class ContentFilter {
    private static final Logger log = LoggerFactory.getLogger(ContentFilter.class);
    private static final List<Pattern> BLOCKED_PATTERNS = List.of(
        Pattern.compile("(rm\\s+-rf|format\\s+|mkfs\\.)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(DROP\\s+TABLE|DELETE\\s+FROM|TRUNCATE\\s+)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(<script>|<iframe>|javascript:)"),
        Pattern.compile("(https?://\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})")
    );

    public String filter(String content) {
        if (content == null) return null;
        String filtered = content;
        for (Pattern pattern : BLOCKED_PATTERNS) {
            if (pattern.matcher(filtered).find()) {
                log.warn("Content blocked by pattern: {}", pattern.pattern());
                return null;
            }
        }
        return filtered;
    }
}