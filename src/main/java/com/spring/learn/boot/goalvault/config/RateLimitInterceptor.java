package com.spring.learn.boot.goalvault.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory TOKEN-BUCKET rate limiter keyed by client IP.
 *
 * Algorithm per bucket:
 *   capacity   = requestsPerMinute
 *   refillRate = requestsPerMinute tokens per 60 seconds (i.e., capacity / 60_000 ms per ms)
 *   Each request tries to consume 1 token. If none available -> 429 with Retry-After.
 *
 * LEARNING: A HandlerInterceptor sits in the Spring MVC pipeline AFTER the DispatcherServlet routes
 * to a handler but BEFORE the handler executes (preHandle). Returning false short-circuits the request.
 * A Filter would run earlier (before DispatcherServlet) — either works here; interceptors give us
 * Spring integration (autowired properties, path-pattern registration in WebMvcConfigurer).
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitProperties props;
    // LEARNING: ConcurrentHashMap is safe for concurrent get/put across Tomcat worker threads.
    // Each bucket's internal state is guarded by its own `synchronized` block (see Bucket.tryConsume).
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitInterceptor(RateLimitProperties props) {
        this.props = props;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!props.isEnabled()) return true;

        String clientIp = resolveClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(clientIp, ip -> new Bucket(props.getRequestsPerMinute()));

        long waitMillis = bucket.tryConsume();
        if (waitMillis <= 0) {
            return true;
        }
        // Bucket empty — reject.
        long retryAfterSec = Math.max(1, (waitMillis + 999) / 1000);
        response.setStatus(429); // Too Many Requests
        response.setHeader("Retry-After", String.valueOf(retryAfterSec));
        response.setHeader("X-RateLimit-Limit", String.valueOf(props.getRequestsPerMinute()));
        response.setHeader("X-RateLimit-Remaining", "0");
        return false;
    }

    // Prefer X-Forwarded-For (first hop) when present — typical when running behind a proxy/load balancer.
    private String resolveClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }
        return req.getRemoteAddr();
    }

    // ---------- Bucket ----------
    static final class Bucket {
        private final double capacity;
        private final double refillPerMillis; // tokens added per ms
        private double tokens;
        private long lastRefillNanos;

        Bucket(int requestsPerMinute) {
            this.capacity = requestsPerMinute;
            this.refillPerMillis = requestsPerMinute / 60_000.0;
            this.tokens = capacity;
            this.lastRefillNanos = System.nanoTime();
        }

        /**
         * Attempts to consume 1 token. Returns 0 if consumed; otherwise returns the estimated
         * millis the caller must wait before 1 full token becomes available.
         */
        synchronized long tryConsume() {
            refill();
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return 0L;
            }
            double missing = 1.0 - tokens;
            return (long) Math.ceil(missing / refillPerMillis);
        }

        private void refill() {
            long now = System.nanoTime();
            long elapsedMillis = (now - lastRefillNanos) / 1_000_000L;
            if (elapsedMillis <= 0) return;
            tokens = Math.min(capacity, tokens + elapsedMillis * refillPerMillis);
            lastRefillNanos = now;
        }
    }
}
