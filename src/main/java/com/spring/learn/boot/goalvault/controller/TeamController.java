package com.spring.learn.boot.goalvault.controller;

import com.spring.learn.boot.goalvault.dto.TeamRequest;
import com.spring.learn.boot.goalvault.dto.TeamResponse;
import com.spring.learn.boot.goalvault.dto.TeamResponseV2;
import com.spring.learn.boot.goalvault.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Team REST endpoints.
 *
 * curl examples:
 *   # Default v1 shape
 *   curl http://localhost:8083/api/v1/teams/1
 *
 *   # Opt-in v2 shape (adds playerCount + averageTeamRating)
 *   curl -H "X-API-Version: 2" http://localhost:8083/api/v1/teams/1
 */
// LEARNING: @RestController = @Controller + @ResponseBody. Every handler returns data (JSON), not a view name.
@RestController
// LEARNING: @RequestMapping at class level prefixes every handler path in this class.
// Versioning the API under /api/v1 lets us evolve v2 without breaking clients.
@RequestMapping("/api/v1/teams")
public class TeamController {

    private final TeamService service;

    public TeamController(TeamService service) {
        this.service = service;
    }

    // LEARNING: @PostMapping maps HTTP POST to this method. @Valid triggers Bean Validation
    // on the incoming DTO — failures become MethodArgumentNotValidException handled globally.
    @PostMapping
    public ResponseEntity<TeamResponse> create(@Valid @RequestBody TeamRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    /**
     * GET /api/v1/teams/{id} — HEADER-BASED VERSIONING demo.
     *
     * If X-API-Version: 2 -> return TeamResponseV2 (adds playerCount + averageTeamRating).
     * Otherwise (or v1) -> return TeamResponse.
     *
     * This is a simplified illustration of HEADER-BASED versioning. The rest of GoalVault uses
     * URI versioning (/api/v1/...). Header versioning keeps the URI stable but hides the version
     * inside metadata — some consider it more "RESTful" (URIs identify resources, not variants);
     * others find URI versioning easier to debug and cache. Real APIs pick ONE approach and stick.
     */
    // LEARNING: @GetMapping + @PathVariable extracts /{id} from the URL and binds it to the parameter.
    // LEARNING: @RequestHeader reads an HTTP request header. required=false + defaultValue makes it optional.
    @GetMapping("/{id}")
    public ResponseEntity<?> get(
            @PathVariable Long id,
            @RequestHeader(value = "X-API-Version", required = false, defaultValue = "1") String apiVersion) {
        if ("2".equals(apiVersion)) {
            return ResponseEntity.ok(service.getV2(id));
        }
        // Default to v1 for any other value (unknown/absent).
        return ResponseEntity.ok(service.get(id));
    }

    // Explicit v2 return type helper — used by tests/tools that want a typed method reference.
    // (Not strictly needed; kept alongside get() for clarity when reading.)
    public TeamResponseV2 getExplicitV2(Long id) {
        return service.getV2(id);
    }

    // LEARNING: @RequestParam binds ?city=xxx query params. required=false makes it optional.
    // @PageableDefault sets default page size / sort; Spring resolves ?page=&size=&sort= automatically.
    @GetMapping
    public Page<TeamResponse> list(
            @RequestParam(required = false) String city,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return service.list(city, pageable);
    }

    @PutMapping("/{id}")
    public TeamResponse update(@PathVariable Long id, @Valid @RequestBody TeamRequest req) {
        return service.update(id, req);
    }

    // LEARNING: @DeleteMapping maps DELETE. Returning ResponseEntity<Void> with 204 is the REST convention.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH-style: rename a stadium. Wired to the @CachePut demo in TeamServiceImpl —
    // updates the DB AND refreshes the "teams" cache with the returned value in one shot.
    @org.springframework.web.bind.annotation.PatchMapping("/{id}/stadium")
    public TeamResponse renameStadium(
            @PathVariable Long id,
            @RequestParam("name") String stadiumName) {
        return service.updateStadium(id, stadiumName);
    }
}
