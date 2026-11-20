package com.spring.learn.boot.goalvault.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * In-memory idempotency store keyed by Idempotency-Key header.
 *
 * WHY ConcurrentHashMap is safe here across concurrent Tomcat request threads:
 *  - `computeIfAbsent` is atomic per key: if two threads race with the same key, only ONE of
 *    them runs the mapping function; the other reuses the resulting value. That guarantee is
 *    exactly what we want — the actual create-match work runs at most once per key.
 *  - Different keys map to different hash bins and don't contend on the same lock, so we get
 *    concurrent throughput for the common case (distinct keys per client).
 *  - Reads during writes never throw ConcurrentModificationException; the map is designed for
 *    concurrent readers and writers.
 *
 * TTL: entries older than 5 minutes are evicted lazily on read (no background thread — this keeps
 * the app aligned with the "no async/threading" hard constraint).
 */
@Service
public class IdempotencyService {

    private static final Duration TTL = Duration.ofMinutes(5);

    private final ConcurrentHashMap<String, CachedResponse> cache = new ConcurrentHashMap<>();

    /**
     * Executes {@code supplier} exactly once for the given {@code key} (until TTL expiry).
     * On subsequent calls with the same key inside the TTL window, returns the cached ResponseEntity.
     *
     * If {@code key} is null/blank, no caching is performed — the supplier just runs.
     */
    public ResponseEntity<?> executeIdempotent(String key, Supplier<ResponseEntity<?>> supplier) {
        if (key == null || key.isBlank()) {
            return supplier.get();
        }
        // Evict on read if the existing entry is stale — cheap, no scheduler needed.
        CachedResponse existing = cache.get(key);
        if (existing != null && existing.isExpired()) {
            cache.remove(key, existing);
            existing = null;
        }
        if (existing != null) {
            return existing.response();
        }
        // computeIfAbsent guarantees at-most-once execution per key across concurrent threads.
        CachedResponse fresh = cache.computeIfAbsent(key, k -> new CachedResponse(Instant.now(), supplier.get()));
        return fresh.response();
    }

    private record CachedResponse(Instant createdAt, ResponseEntity<?> response) {
        boolean isExpired() {
            return Instant.now().isAfter(createdAt.plus(TTL));
        }
    }
}
