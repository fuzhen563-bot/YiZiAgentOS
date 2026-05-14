package com.agentos.mcp.core;

import java.util.Map;

public class McpSchema {
    public static Map<String, Object> parse(String jsonSchema) {
        return Map.of();
    }

    public static boolean validate(Map<String, Object> args, Map<String, Object> schema) {
        return true;
    }

    public static String inferType(Object value) {
        if (value instanceof String) return "string";
        if (value instanceof Number) return "number";
        if (value instanceof Boolean) return "boolean";
        if (value instanceof java.util.List) return "array";
        if (value instanceof Map) return "object";
        return "string";
    }
}