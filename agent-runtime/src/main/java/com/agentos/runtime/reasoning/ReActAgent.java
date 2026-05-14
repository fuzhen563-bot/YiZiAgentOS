package com.agentos.runtime.reasoning;

import com.agentos.runtime.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ReActAgent extends BaseAgent {
    private static final Logger log = LoggerFactory.getLogger(ReActAgent.class);
    private List<Map<String, Object>> availableTools = null;
    private final Map<String, String> toolKeywords = new LinkedHashMap<>();

    public ReActAgent(String id, String name, String role) {
        super(id, name, role);
        toolKeywords.put("github", "github_list_repos");
        toolKeywords.put("repo", "github_list_repos");
        toolKeywords.put("repository", "github_list_repos");
        toolKeywords.put("仓库", "github_list_repos");
        toolKeywords.put("代码", "github_search_code");
        toolKeywords.put("email", "email_read");
        toolKeywords.put("mail", "email_read");
        toolKeywords.put("send.*email", "email_send");
        toolKeywords.put("邮件", "email_read");
        toolKeywords.put("发邮件", "email_send");
        toolKeywords.put("calendar", "calendar_list_events");
        toolKeywords.put("event", "calendar_list_events");
        toolKeywords.put("schedule", "calendar_list_events");
        toolKeywords.put("meeting", "calendar_list_events");
        toolKeywords.put("日历", "calendar_list_events");
        toolKeywords.put("会议", "calendar_list_events");
        toolKeywords.put("contact", "crm_list_contacts");
        toolKeywords.put("crm", "crm_list_contacts");
        toolKeywords.put("客户", "crm_list_contacts");
        toolKeywords.put("联系人", "crm_list_contacts");
        toolKeywords.put("deal", "crm_list_deals");
        toolKeywords.put("销售", "crm_list_deals");
        toolKeywords.put("inventory", "erp_list_inventory");
        toolKeywords.put("stock", "erp_list_inventory");
        toolKeywords.put("库存", "erp_list_inventory");
        toolKeywords.put("order", "erp_get_order");
        toolKeywords.put("订单", "erp_get_order");
        toolKeywords.put("invoice", "erp_get_invoice");
        toolKeywords.put("发票", "erp_get_invoice");
    }

    private boolean isChinese(String text) {
        return text != null && text.codePoints().anyMatch(cp -> Character.UnicodeScript.of(cp) == Character.UnicodeScript.HAN);
    }

    private String languageInstruction(String userMsg) {
        return isChinese(userMsg)
            ? "\n\nIMPORTANT: The user wrote in Chinese. You MUST respond in Chinese (简体中文). Always reply in the same language as the user's message."
            : "\n\nIMPORTANT: Respond in the same language as the user's message. If they write in English, reply in English.";
    }

    @Override
    public CompletableFuture<AgentResponse> execute(AgentRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            long start = System.currentTimeMillis();
            AgentResponse response = AgentResponse.success(getId(), "");
            state = AgentState.THINKING;

            if (availableTools == null || availableTools.isEmpty()) {
                availableTools = ToolRegistryClient.listTools();
            }
            log.info("Available tools: {}", availableTools.size());

            String userMsg = request.getMessage().toLowerCase();
            String matchedTool = matchTool(userMsg);
            String finalAnswer;
            String langInstr = languageInstruction(request.getMessage());

            if (matchedTool != null) {
                state = AgentState.ACTING;
                Map<String, Object> args = extractArgs(userMsg, matchedTool);
                response.addThought("I'll use " + matchedTool + " to help with this request.");
                response.addToolCall(new AgentResponse.ToolCall(matchedTool, args));

                Map<String, Object> toolResult = ToolRegistryClient.callTool(matchedTool, args);
                log.info("Tool {} returned: {}", matchedTool, toolResult);

                state = AgentState.OBSERVING;
                String summary = (String) toolResult.getOrDefault("summary",
                    toolResult.getOrDefault("message", "Tool executed"));
                Object data = toolResult.get("data");

                String formatted = formatResult(matchedTool, summary, data, request.getMessage());
                finalAnswer = formatted;

                String analysisPrompt = "The user asked: \"" + request.getMessage()
                    + "\"\n\nI used tool \"" + matchedTool + "\" and got:\n"
                    + summary + "\nData: " + (data != null ? data : "none")
                    + "\n\nSummarize this for the user in a natural way." + langInstr;
                String llmResponse = LlmClient.chat(analysisPrompt);
                finalAnswer = llmResponse != null && !llmResponse.contains("(Offline mode)")
                    ? llmResponse : formatted;
            } else {
                state = AgentState.THINKING;
                finalAnswer = LlmClient.chat("You are " + name + ", " + role
                    + ". Respond helpfully to: " + request.getMessage() + langInstr);
            }

            response.setMessage(finalAnswer);
            state = AgentState.COMPLETED;
            response.setDurationMs(System.currentTimeMillis() - start);
            response.setSteps(matchedTool != null ? 2 : 1);
            return response;
        });
    }

    private String matchTool(String msg) {
        for (var e : toolKeywords.entrySet()) {
            if (msg.matches(".*" + e.getKey() + ".*")) {
                return e.getValue();
            }
        }
        return null;
    }

    private Map<String, Object> extractArgs(String msg, String tool) {
        Map<String, Object> args = new LinkedHashMap<>();
        if (tool.contains("repo") || tool.contains("list_repos")) {
            String owner = "user";
            String[] words = msg.replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+");
            for (int i = 0; i < words.length; i++) {
                if ((words[i].equals("for") || words[i].equals("username") || words[i].equals("user"))
                    && i + 1 < words.length) {
                    owner = words[i + 1];
                    break;
                }
            }
            args.put("owner", owner);
        }
        if (tool.contains("get_file")) {
            args.put("path", "README.md");
        }
        if (tool.contains("create_issue")) {
            args.put("title", msg.length() > 50 ? msg.substring(0, 50) : msg);
        }
        if (tool.contains("search_code")) {
            args.put("query", msg.replaceAll("search.*?for\\s+", "").trim());
        }
        if (tool.contains("send")) {
            args.put("to", "");
            args.put("subject", "RE: " + msg);
            args.put("body", msg);
        }
        if (tool.contains("create_event")) {
            args.put("title", msg);
            args.put("date", "tomorrow");
        }
        return args;
    }

    private String formatResult(String tool, String summary, Object data, String original) {
        StringBuilder sb = new StringBuilder();
        if (data instanceof List) {
            List<?> items = (List<?>) data;
            sb.append(summary).append("\n\n");
            for (Object item : items) {
                if (item instanceof Map) {
                    Map<?, ?> m = (Map<?, ?>) item;
                    sb.append("- ");
                    for (var e : m.entrySet()) {
                        sb.append(e.getKey()).append(": ").append(e.getValue()).append(", ");
                    }
                    sb.setLength(sb.length() - 2);
                    sb.append("\n");
                }
            }
        } else if (data instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) data;
            sb.append(summary).append("\n");
            for (var e : m.entrySet()) {
                sb.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
            }
        } else {
            sb.append(summary);
        }
        return sb.toString();
    }
}