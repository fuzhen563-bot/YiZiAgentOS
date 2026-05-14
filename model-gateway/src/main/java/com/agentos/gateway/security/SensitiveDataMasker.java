package com.agentos.gateway.security;

import java.util.regex.Pattern;

public class SensitiveDataMasker {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("1[3-9]\\d{9}");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]");
    private static final Pattern API_KEY_PATTERN = Pattern.compile("(sk-[a-zA-Z0-9]{20,}|api[_-]?key['\"]?\\s*[:=]\\s*['\"]?[a-zA-Z0-9_-]{20,})", Pattern.CASE_INSENSITIVE);

    public String mask(String content) {
        if (content == null) return null;
        content = EMAIL_PATTERN.matcher(content).replaceAll("***@***.***");
        content = PHONE_PATTERN.matcher(content).replaceAll("138****0000");
        content = ID_CARD_PATTERN.matcher(content).replaceAll("******************");
        content = API_KEY_PATTERN.matcher(content).replaceAll("***API_KEY_MASKED***");
        return content;
    }
}