package com.spring.learn.boot.goalvault.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Admin-only endpoints. Locked down by both:
 *   1) the URL matcher in SecurityConfig  (/api/v1/admin/** requires ROLE_ADMIN), AND
 *   2) method-level annotations below (belt-and-braces).
 *
 * curl examples:
 *   curl -u admin:admin -X DELETE http://localhost:8083/api/v1/admin/cache/teams
 *   curl -u admin:admin -X POST   http://localhost:8083/api/v1/admin/ping
 *   curl -u user:password -X DELETE http://localhost:8083/api/v1/admin/cache/teams   # -> 403
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final CacheManager cacheManager;

    public AdminController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * @PreAuthorize style — SpEL expression. Preferred for anything richer than a role check.
     */
    @DeleteMapping("/cache/teams")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> clearTeamCache() {
        var cache = cacheManager.getCache("teams");
        if (cache != null) {
            cache.clear();
        }
        log.info("Admin cleared 'teams' cache.");
        return ResponseEntity.ok(Map.of("cache", "teams", "cleared", true));
    }

    /**
     * @Secured style — simple string role name. Kept alongside @PreAuthorize so you can compare.
     * Practical difference: @Secured cannot reference method args or the principal object; it's
     * strictly role-based. @PreAuthorize takes a SpEL expression and is far more expressive.
     */
    @PostMapping("/ping")
    @Secured("ROLE_ADMIN")
    public Map<String, String> adminPing() {
        return Map.of("status", "ok", "message", "admin ping");
    }
}
