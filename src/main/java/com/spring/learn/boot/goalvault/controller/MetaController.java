package com.spring.learn.boot.goalvault.controller;

import com.spring.learn.boot.goalvault.config.AppMetadata;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Exposes the strongly-typed {@link AppMetadata} to demonstrate that @ConfigurationProperties
 * beans are ordinary Spring beans — inject them anywhere.
 *
 * curl example:
 *   curl http://localhost:8083/api/v1/meta
 */
@RestController
@RequestMapping("/api/v1/meta")
public class MetaController {

    private final AppMetadata meta;

    public MetaController(AppMetadata meta) {
        this.meta = meta;
    }

    @GetMapping
    public Map<String, Object> meta() {
        return Map.of(
                "name", meta.getName(),
                "description", meta.getDescription(),
                "defaultPageSize", meta.getDefaultPageSize()
        );
    }
}
