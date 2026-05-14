package com.agentos.secure.browser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BrowserAutomation {
    private static final Logger log = LoggerFactory.getLogger(BrowserAutomation.class);
    private final Map<String, BrowserSession> sessions = new ConcurrentHashMap<>();

    public BrowserSession createSession() {
        String id = UUID.randomUUID().toString();
        BrowserSession session = new BrowserSession(id);
        sessions.put(id, session);
        log.info("Created browser session: {}", id);
        return session;
    }

    public BrowserSession getSession(String id) {
        BrowserSession session = sessions.get(id);
        if (session == null) return null;
        if (!session.isActive()) {
            sessions.remove(id);
            return null;
        }
        return session;
    }

    public void closeSession(String id) {
        BrowserSession session = sessions.get(id);
        if (session != null) {
            session.close();
            sessions.remove(id);
            log.info("Closed browser session: {}", id);
        }
    }

    public Map<String, Object> navigate(String sessionId, String url) {
        BrowserSession session = checkSession(sessionId);
        if (session == null) return error("Session not found: " + sessionId);
        session.putState("currentUrl", url);
        log.info("Navigated to: {}", url);
        return ok("navigated", url);
    }

    public Map<String, Object> click(String sessionId, String selector) {
        BrowserSession session = checkSession(sessionId);
        if (session == null) return error("Session not found: " + sessionId);
        log.info("Clicked element: {}", selector);
        return ok("clicked", selector);
    }

    public Map<String, Object> type(String sessionId, String selector, String text) {
        BrowserSession session = checkSession(sessionId);
        if (session == null) return error("Session not found");
        log.info("Typed into {}: {}", selector, text);
        return ok("typed", text);
    }

    public Map<String, Object> screenshot(String sessionId) {
        BrowserSession session = checkSession(sessionId);
        if (session == null) return error("Session not found");
        return ok("screenshot", "data:image/png;base64,...");
    }

    public Map<String, Object> getPageContent(String sessionId) {
        BrowserSession session = checkSession(sessionId);
        if (session == null) return error("Session not found");
        return ok("content", "<html><body>Page content</body></html>");
    }

    public Map<String, Object> evaluateJS(String sessionId, String script) {
        BrowserSession session = checkSession(sessionId);
        if (session == null) return error("Session not found");
        log.info("Evaluated JS: {}", script.substring(0, Math.min(50, script.length())));
        return ok("result", null);
    }

    public List<Map<String, Object>> listSessions() {
        return sessions.values().stream()
            .filter(BrowserSession::isActive)
            .map(s -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", s.getId());
                m.put("createdAt", s.getCreatedAt());
                return m;
            })
            .toList();
    }

    private BrowserSession checkSession(String id) {
        BrowserSession session = sessions.get(id);
        if (session == null || !session.isActive()) return null;
        return session;
    }

    private Map<String, Object> ok(String action, Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "success");
        result.put("action", action);
        if (value != null) result.put("value", value);
        return result;
    }

    private Map<String, Object> error(String message) {
        return Map.of("status", "error", "message", message);
    }
}