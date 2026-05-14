package com.agentos.secure.sandbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SandboxEnvironment {
    private static final Logger log = LoggerFactory.getLogger(SandboxEnvironment.class);
    private final Map<String, SandboxInstance> instances = new ConcurrentHashMap<>();

    public SandboxInstance create(String image, Map<String, String> env) {
        String id = UUID.randomUUID().toString();
        SandboxInstance instance = new SandboxInstance(id, image, env);
        instances.put(id, instance);
        log.info("Created sandbox: {} (image: {})", id, image);
        return instance;
    }

    public SandboxInstance get(String id) {
        return instances.get(id);
    }

    public Map<String, Object> executeCommand(String sandboxId, String command, int timeoutSeconds) {
        SandboxInstance instance = instances.get(sandboxId);
        if (instance == null) return Map.of("error", "Sandbox not found");
        if (!instance.isRunning()) return Map.of("error", "Sandbox not running");
        instance.addCommand(command);
        instance.incrementExecCount();
        log.info("Executed in sandbox {}: {}", sandboxId, command.substring(0, Math.min(50, command.length())));
        return Map.of("status", "executed", "output", "[sandbox output]", "exitCode", 0);
    }

    public void destroy(String id) {
        SandboxInstance instance = instances.remove(id);
        if (instance != null) {
            instance.stop();
            log.info("Destroyed sandbox: {}", id);
        }
    }

    public List<Map<String, Object>> listInstances() {
        return instances.values().stream()
            .filter(SandboxInstance::isRunning)
            .map(i -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", i.getId());
                m.put("image", i.getImage());
                m.put("execCount", i.getExecCount());
                return m;
            })
            .toList();
    }

    public void destroyAll() {
        instances.values().forEach(SandboxInstance::stop);
        instances.clear();
        log.info("Destroyed all sandbox instances");
    }

    public static class SandboxInstance {
        private final String id;
        private final String image;
        private final Map<String, String> env;
        private volatile boolean running;
        private final List<String> commands = new ArrayList<>();
        private int execCount;

        SandboxInstance(String id, String image, Map<String, String> env) {
            this.id = id;
            this.image = image;
            this.env = env != null ? env : Map.of();
            this.running = true;
        }

        public String getId() { return id; }
        public String getImage() { return image; }
        public boolean isRunning() { return running; }
        public void stop() { this.running = false; }
        public int getExecCount() { return execCount; }
        public void incrementExecCount() { execCount++; }
        public void addCommand(String cmd) { commands.add(cmd); }
        public List<String> getCommands() { return List.copyOf(commands); }
    }
}