package com.agentos.knowledge.graph;

import java.util.*;

public class KnowledgeGraph {
    private final Map<String, Entity> entities = new HashMap<>();
    private final List<Relation> relations = new ArrayList<>();

    public static class Entity {
        private String id;
        private String name;
        private String type;
        private Map<String, Object> properties = new HashMap<>();

        public Entity(String id, String name, String type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getType() { return type; }
        public Map<String, Object> getProperties() { return properties; }
        public void setProperty(String key, Object value) { properties.put(key, value); }
    }

    public static class Relation {
        private String sourceId;
        private String targetId;
        private String type;
        private Map<String, Object> properties = new HashMap<>();

        public Relation(String sourceId, String targetId, String type) {
            this.sourceId = sourceId;
            this.targetId = targetId;
            this.type = type;
        }

        public String getSourceId() { return sourceId; }
        public String getTargetId() { return targetId; }
        public String getType() { return type; }
        public Map<String, Object> getProperties() { return properties; }
    }

    public Entity addEntity(String id, String name, String type) {
        Entity entity = new Entity(id, name, type);
        entities.put(id, entity);
        return entity;
    }

    public void addRelation(String sourceId, String targetId, String type) {
        if (entities.containsKey(sourceId) && entities.containsKey(targetId)) {
            relations.add(new Relation(sourceId, targetId, type));
        }
    }

    public Entity getEntity(String id) { return entities.get(id); }
    public Map<String, Entity> getEntities() { return entities; }

    public Map<String, Long> getEntityTypeDistribution() {
        return entities.values().stream()
            .collect(java.util.stream.Collectors.groupingBy(
                Entity::getType, java.util.stream.Collectors.counting()));
    }

    public List<Entity> getEntitiesByType(String type) {
        return entities.values().stream().filter(e -> e.getType().equals(type)).toList();
    }

    public List<Relation> getRelations(String entityId) {
        return relations.stream()
            .filter(r -> r.getSourceId().equals(entityId) || r.getTargetId().equals(entityId))
            .toList();
    }

    public List<Map<String, Object>> getNeighbors(String entityId, int depth) {
        List<Map<String, Object>> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(entityId);
        visited.add(entityId);
        int currentDepth = 0;
        while (!queue.isEmpty() && currentDepth < depth) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                String current = queue.poll();
                for (Relation r : getRelations(current)) {
                    String neighbor = r.getSourceId().equals(current) ? r.getTargetId() : r.getSourceId();
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                        Entity e = entities.get(neighbor);
                        if (e == null) continue;
                        result.add(Map.of(
                            "entity", Map.of("id", e.getId(), "name", e.getName(), "type", e.getType()),
                            "relation", Map.of("type", r.getType(), "direction",
                                r.getSourceId().equals(current) ? "outgoing" : "incoming")
                        ));
                    }
                }
            }
            currentDepth++;
        }
        return result;
    }
}