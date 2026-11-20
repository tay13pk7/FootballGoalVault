package com.spring.learn.boot.goalvault.controller;

import com.spring.learn.boot.goalvault.dto.PlayerBulkResult;
import com.spring.learn.boot.goalvault.dto.PlayerRequest;
import com.spring.learn.boot.goalvault.dto.PlayerResponse;
import com.spring.learn.boot.goalvault.exception.ResourceNotFoundException;
import com.spring.learn.boot.goalvault.service.PlayerService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Player REST endpoints.
 *
 * curl examples:
 *   # 1) Pagination + sort
 *   curl "http://localhost:8083/api/v1/players?page=0&size=5&sort=goalsScored,desc"
 *
 *   # 2) Dynamic filter: forwards on team 3 with 10+ goals
 *   curl "http://localhost:8083/api/v1/players?position=FORWARD&teamId=3&minGoals=10"
 *
 *   # 3) Create (fails validation because position=STRIKER isn't in the allowed set)
 *   curl -X POST http://localhost:8083/api/v1/players \
 *        -H "Content-Type: application/json" \
 *        -d '{"name":"Test","position":"STRIKER","jerseyNumber":9,"teamId":1,"goalsScored":0}'
 *
 *   # 4) Bulk create with partial success — some items fail, others succeed
 *   curl -X POST http://localhost:8083/api/v1/players/bulk \
 *        -H "Content-Type: application/json" \
 *        -d '[{"name":"Ok","position":"FORWARD","jerseyNumber":11,"teamId":1,"goalsScored":0},
 *             {"name":"","position":"WRONG","jerseyNumber":0,"teamId":9999,"goalsScored":-1}]'
 *
 *   # 5) Score a goal (custom action)
 *   curl -X POST http://localhost:8083/api/v1/players/1/score
 */
@RestController
@RequestMapping("/api/v1/players")
public class PlayerController {

    private final PlayerService service;
    // LEARNING: jakarta.validation.Validator lets us run Bean Validation PROGRAMMATICALLY,
    // which is essential for the bulk endpoint — we want per-item validation results, not a single
    // MethodArgumentNotValidException that aborts the whole batch.
    private final Validator validator;

    public PlayerController(PlayerService service, Validator validator) {
        this.service = service;
        this.validator = validator;
    }

    @PostMapping
    public ResponseEntity<PlayerResponse> create(@Valid @RequestBody PlayerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @GetMapping("/{id}")
    public PlayerResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    // Pagination + sorting + dynamic filters. All filter params are optional.
    @GetMapping
    public Page<PlayerResponse> list(
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) Integer minGoals,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return service.list(teamId, position, minGoals, q, pageable);
    }

    @PutMapping("/{id}")
    public PlayerResponse update(@PathVariable Long id, @Valid @RequestBody PlayerRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // LEARNING: Custom action endpoint. Actions that don't fit CRUD are commonly modeled as POST /{id}/action.
    @PostMapping("/{id}/score")
    public PlayerResponse scoreGoal(@PathVariable Long id) {
        return service.scoreGoal(id);
    }

    /**
     * Bulk create with PARTIAL SUCCESS.
     *
     * Each item is validated + processed independently. The response returns HTTP 200 with a
     * per-item result — this is deliberate: the caller wants to know which rows landed and which
     * didn't, without a single bad row nuking the whole batch.
     * Note: we deliberately do NOT annotate the list with @Valid — that would fail-fast at the
     * controller boundary. Instead we validate each element programmatically via the Validator.
     */
    @PostMapping("/bulk")
    public List<PlayerBulkResult> bulkCreate(@RequestBody List<PlayerRequest> requests) {
        List<PlayerBulkResult> results = new ArrayList<>(requests.size());
        for (int i = 0; i < requests.size(); i++) {
            PlayerRequest req = requests.get(i);
            if (req == null) {
                results.add(PlayerBulkResult.failed(i, "request body was null"));
                continue;
            }
            Set<ConstraintViolation<PlayerRequest>> violations = validator.validate(req);
            if (!violations.isEmpty()) {
                String msg = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .collect(Collectors.joining("; "));
                results.add(PlayerBulkResult.failed(i, msg));
                continue;
            }
            try {
                PlayerResponse created = service.create(req);
                results.add(PlayerBulkResult.ok(i, created.id()));
            } catch (ResourceNotFoundException | IllegalArgumentException ex) {
                results.add(PlayerBulkResult.failed(i, ex.getMessage()));
            }
        }
        return results;
    }
}
