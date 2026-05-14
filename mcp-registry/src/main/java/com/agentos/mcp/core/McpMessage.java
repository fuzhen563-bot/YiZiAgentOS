package com.agentos.mcp.core;

import java.util.Map;

public class McpMessage {
    private String jsonrpc = "2.0";
    private String id;
    private String method;
    private Map<String, Object> params;
    private Map<String, Object> result;
    private McpError error;

    public McpMessage() {}

    public McpMessage(String id, String method, Map<String, Object> params) {
        this.id = id;
        this.method = method;
        this.params = params;
    }

    public static McpMessage request(String id, String method, Map<String, Object> params) {
        return new McpMessage(id, method, params);
    }

    public static McpMessage response(String id, Map<String, Object> result) {
        McpMessage msg = new McpMessage();
        msg.id = id;
        msg.result = result;
        return msg;
    }

    public String getJsonrpc() { return jsonrpc; }
    public String getId() { return id; }
    public String getMethod() { return method; }
    public Map<String, Object> getParams() { return params; }
    public Map<String, Object> getResult() { return result; }
    public void setResult(Map<String, Object> result) { this.result = result; }
    public McpError getError() { return error; }
    public void setError(McpError error) { this.error = error; }

    public static class McpError {
        private int code;
        private String message;
        private Object data;

        public McpError(int code, String message) { this.code = code; this.message = message; }

        public int getCode() { return code; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }
}