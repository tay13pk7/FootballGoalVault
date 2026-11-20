package com.spring.learn.boot.goalvault.controller;

import com.spring.learn.boot.goalvault.dto.MatchRequest;
import com.spring.learn.boot.goalvault.dto.MatchResponse;
import com.spring.learn.boot.goalvault.notification.NotificationService;
import com.spring.learn.boot.goalvault.service.IdempotencyService;
import com.spring.learn.boot.goalvault.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Match REST endpoints.
 *
 * curl examples:
 *   # Create a match with an Idempotency-Key. Second call with same key returns the ORIGINAL response.
 *   curl -X POST http://localhost:8083/api/v1/matches \
 *        -H "Content-Type: application/json" \
 *        -H "Idempotency-Key: 6f8a2c3d-e4b1-4a92-9f61-abc123456789" \
 *        -d '{"homeTeamId":1,"awayTeamId":2,"matchDate":"2025-09-01T15:00:00","homeScore":2,"awayScore":1,"stadium":"North Park Arena"}'
 *
 *   # Same key again -> same body, no duplicate row.
 *   # No header -> normal create, no idempotency guarantee.
 */
@RestController
@RequestMapping("/api/v1/matches")
public class MatchController {

    private final MatchService service;
    private final IdempotencyService idempotency;
    private final NotificationService notifications;

    public MatchController(MatchService service,
                           IdempotencyService idempotency,
                           NotificationService notifications) {
        this.service = service;
        this.idempotency = idempotency;
        this.notifications = notifications;
    }

    /**
     * POST /api/v1/matches with optional Idempotency-Key header.
     *
     * Kicks off an @Async fan-notification job after the match is persisted so the caller
     * gets its response immediately while the notification runs in the background.
     * The call reaches NotificationService via its Spring proxy (a different bean), so
     * the @Async advice actually fires — self-invocation would silently be synchronous.
     */
    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody MatchRequest req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return idempotency.executeIdempotent(idempotencyKey, () -> {
            MatchResponse created = service.create(req);
            notifications.sendMatchNotification(created.id());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        });
    }

    @GetMapping("/{id}")
    public MatchResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public Page<MatchResponse> list(
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String stadium,
            @PageableDefault(size = 20, sort = "matchDate") Pageable pageable) {
        return service.list(teamId, stadium, pageable);
    }

    @PutMapping("/{id}")
    public MatchResponse update(@PathVariable Long id, @Valid @RequestBody MatchRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
