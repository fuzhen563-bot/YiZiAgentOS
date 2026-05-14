package com.agentos.secure.rollback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RollbackManager {
    private static final Logger log = LoggerFactory.getLogger(RollbackManager.class);
    private final Map<String, List<Operation>> operationLogs = new ConcurrentHashMap<>();
    private final Map<String, LinkedList<Snapshot>> snapshots = new ConcurrentHashMap<>();

    public static class Operation {
        private String id;
        private String type;
        private String target;
        private Map<String, Object> beforeState;
        private Map<String, Object> afterState;
        private long timestamp;
        private boolean rolledBack;

        public Operation(String type, String target, Map<String, Object> beforeState) {
            this.id = UUID.randomUUID().toString();
            this.type = type;
            this.target = target;
            this.beforeState = beforeState;
            this.timestamp = System.currentTimeMillis();
            this.rolledBack = false;
        }

        public String getId() { return id; }
        public String getType() { return type; }
        public String getTarget() { return target; }
        public boolean isRolledBack() { return rolledBack; }
        public void markRolledBack() { this.rolledBack = true; }
        public Map<String, Object> getBeforeState() { return beforeState; }
        public void setAfterState(Map<String, Object> afterState) { this.afterState = afterState; }
    }

    public static class Snapshot {
        private String id;
        private String resourceId;
        private Map<String, Object> data;
        private long timestamp;

        public Snapshot(String resourceId, Map<String, Object> data) {
            this.id = UUID.randomUUID().toString();
            this.resourceId = resourceId;
            this.data = new HashMap<>(data);
            this.timestamp = System.currentTimeMillis();
        }

        public String getId() { return id; }
        public String getResourceId() { return resourceId; }
        public Map<String, Object> getData() { return data; }
        public long getTimestamp() { return timestamp; }
    }

    public Operation beginOperation(String sessionId, String type, String target, Map<String, Object> beforeState) {
        Operation op = new Operation(type, target, beforeState);
        operationLogs.computeIfAbsent(sessionId, k -> Collections.synchronizedList(new ArrayList<>())).add(op);
        log.info("Operation started: {} on {} (session: {})", type, target, sessionId);
        return op;
    }

    public void completeOperation(String sessionId, String operationId, Map<String, Object> afterState) {
        List<Operation> ops = operationLogs.get(sessionId);
        if (ops == null) return;
        ops.stream().filter(o -> o.getId().equals(operationId)).findFirst()
            .ifPresent(o -> o.setAfterState(afterState));
    }

    public boolean rollback(String sessionId, String operationId) {
        List<Operation> ops = operationLogs.get(sessionId);
        if (ops == null) return false;
        Optional<Operation> target = ops.stream()
            .filter(o -> o.getId().equals(operationId) && !o.isRolledBack())
            .findFirst();
        if (target.isPresent()) {
            Operation op = target.get();
            op.markRolledBack();
            log.info("Rolled back operation: {} on {} (session: {})", op.getType(), op.getTarget(), sessionId);
            return true;
        }
        return false;
    }

    public void rollbackAll(String sessionId) {
        List<Operation> ops = operationLogs.get(sessionId);
        if (ops == null) return;
        for (int i = ops.size() - 1; i >= 0; i--) {
            Operation op = ops.get(i);
            if (!op.isRolledBack()) {
                op.markRolledBack();
            }
        }
        log.info("Rolled back all {} operations in session {}", ops.size(), sessionId);
    }

    public Snapshot takeSnapshot(String resourceId, Map<String, Object> data) {
        Snapshot snap = new Snapshot(resourceId, data);
        snapshots.computeIfAbsent(resourceId, k -> new LinkedList<>()).addLast(snap);
        log.info("Snapshot taken for resource: {}", resourceId);
        return snap;
    }

    public Snapshot restoreSnapshot(String resourceId) {
        LinkedList<Snapshot> snaps = snapshots.get(resourceId);
        if (snaps == null || snaps.isEmpty()) return null;
        Snapshot last = snaps.peekLast();
        log.info("Restored snapshot for resource: {} from {}", resourceId, last.getTimestamp());
        return last;
    }

    public List<Operation> getOperations(String sessionId) {
        return List.copyOf(operationLogs.getOrDefault(sessionId, List.of()));
    }
}