package com.agentos.evolution;

import com.agentos.evolution.market.SkillMarketplace;
import com.agentos.evolution.reflection.ReflectionEngine;
import com.agentos.evolution.skill.SkillEvolutionEngine;
import com.agentos.evolution.sop.SopEngine;
import com.agentos.evolution.sop.SopEngine.SopStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/evolution")
public class EvolutionController {

    @Autowired private ReflectionEngine reflection;
    @Autowired private SopEngine sopEngine;
    @Autowired private SkillEvolutionEngine skillEvolution;
    @Autowired private SkillMarketplace marketplace;

    // ===== Reflection =====

    @PostMapping("/tasks/start")
    public ResponseEntity<?> startTask(@RequestBody Map<String, String> body) {
        var task = reflection.startTask(body.get("agentId"), body.get("goal"));
        return ResponseEntity.ok(Map.of("taskId", task.getId()));
    }

    @PostMapping("/tasks/{id}/complete")
    public ResponseEntity<?> completeTask(@PathVariable String id, @RequestParam int tokens) {
        reflection.completeTask(id, tokens);
        return ResponseEntity.ok(Map.of("status", "completed"));
    }

    @PostMapping("/tasks/{id}/fail")
    public ResponseEntity<?> failTask(@PathVariable String id, @RequestBody Map<String, String> body) {
        reflection.failTask(id, body.get("error"));
        return ResponseEntity.ok(Map.of("status", "failed"));
    }

    @GetMapping("/tasks/{id}/analyze")
    public ResponseEntity<?> analyzeTask(@PathVariable String id) {
        return ResponseEntity.ok(reflection.analyze(id));
    }

    @GetMapping("/agents/{agentId}/summary")
    public ResponseEntity<?> agentSummary(@PathVariable String agentId) {
        return ResponseEntity.ok(reflection.getAgentSummary(agentId));
    }

    @GetMapping("/tasks")
    public ResponseEntity<?> listTasks(@RequestParam(required = false) String agentId) {
        return ResponseEntity.ok(reflection.listTasks(agentId));
    }

    // ===== SOP =====

    @PostMapping("/sop/extract")
    public ResponseEntity<?> extractSop(@RequestBody Map<String, String> body) {
        var sop = sopEngine.extractSop(body.get("agentId"), body.get("name"), body.get("goal"));
        return ResponseEntity.ok(Map.of("sopId", sop.getId(), "steps", sop.getSteps().size()));
    }

    @PostMapping("/sop/create")
    public ResponseEntity<?> createSop(@RequestBody CreateSopRequest req) {
        var sop = sopEngine.createSop(req.name, req.goal, req.steps);
        return ResponseEntity.ok(Map.of("sopId", sop.getId()));
    }

    @GetMapping("/sop/search")
    public ResponseEntity<?> searchSop(@RequestParam String q) {
        return ResponseEntity.ok(sopEngine.searchSops(q));
    }

    @PostMapping("/sop/{id}/evaluate")
    public ResponseEntity<?> evaluateSop(@PathVariable String id) {
        sopEngine.evaluateQuality(id);
        var sop = sopEngine.getSop(id);
        return ResponseEntity.ok(Map.of("quality", sop != null ? sop.getQuality() : "not_found"));
    }

    @PostMapping("/behavior/log")
    public ResponseEntity<?> logBehavior(@RequestBody Map<String, String> body) {
        sopEngine.logBehavior(body.get("agentId"), body.get("action"), body.get("context"));
        return ResponseEntity.ok(Map.of("status", "logged"));
    }

    // ===== Skill Evolution =====

    @PostMapping("/skills/create")
    public ResponseEntity<?> createSkill(@RequestBody Map<String, String> body) {
        var skill = skillEvolution.createSkill(body.get("name"), body.get("description"), body.get("prompt"));
        return ResponseEntity.ok(Map.of("skillId", skill.getId()));
    }

    @PostMapping("/skills/{id}/mutate")
    public ResponseEntity<?> mutateSkill(@PathVariable String id, @RequestBody Map<String, String> body) {
        var child = skillEvolution.mutateSkill(id, body.get("mutation"));
        return ResponseEntity.ok(Map.of("newSkillId", child.getId()));
    }

    @PostMapping("/skills/{id}/evaluate")
    public ResponseEntity<?> evaluateSkill(@PathVariable String id) {
        skillEvolution.evaluate(id);
        var skill = skillEvolution.getSkill(id);
        return ResponseEntity.ok(Map.of("score", skill.getScore(), "status", skill.getStatus()));
    }

    @PostMapping("/skills/{id}/validate")
    public ResponseEntity<?> validateSkill(@PathVariable String id) {
        skillEvolution.sandboxValidate(id);
        var skill = skillEvolution.getSkill(id);
        return ResponseEntity.ok(Map.of("status", skill.getStatus()));
    }

    @GetMapping("/skills/candidates")
    public ResponseEntity<?> getCandidates() {
        return ResponseEntity.ok(skillEvolution.getEvolutionCandidates());
    }

    @PostMapping("/skills/eliminate")
    public ResponseEntity<?> eliminate(@RequestParam double threshold) {
        skillEvolution.eliminateLowPerformers(threshold);
        return ResponseEntity.ok(Map.of("status", "eliminated"));
    }

    // ===== Marketplace =====

    @PostMapping("/market/publish")
    public ResponseEntity<?> publish(@RequestBody PublishRequest req) {
        var listing = marketplace.publish(req.skillId, req.name, req.description, req.author);
        return ResponseEntity.ok(Map.of("listingId", listing.getId()));
    }

    @GetMapping("/market/search")
    public ResponseEntity<?> marketSearch(@RequestParam String q) {
        return ResponseEntity.ok(marketplace.search(q));
    }

    @GetMapping("/market/trending")
    public ResponseEntity<?> trending() {
        return ResponseEntity.ok(marketplace.getTrending());
    }

    @GetMapping("/market/top-rated")
    public ResponseEntity<?> topRated() {
        return ResponseEntity.ok(marketplace.getTopRated());
    }

    @PostMapping("/market/{id}/download")
    public ResponseEntity<?> download(@PathVariable String id) {
        marketplace.recordDownload(id);
        return ResponseEntity.ok(Map.of("status", "downloaded"));
    }

    @PostMapping("/market/{id}/rate")
    public ResponseEntity<?> rate(@PathVariable String id, @RequestParam double rating) {
        marketplace.rate(id, rating);
        return ResponseEntity.ok(Map.of("status", "rated"));
    }

    @GetMapping("/market/stats")
    public ResponseEntity<?> marketStats() {
        return ResponseEntity.ok(marketplace.getStats());
    }

    public static class CreateSopRequest {
        public String name; public String goal; public List<SopStep> steps;
    }

    public static class PublishRequest {
        public String skillId; public String name; public String description; public String author;
    }
}