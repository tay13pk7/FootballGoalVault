package com.spring.learn.boot.goalvault.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Top-level GoalVault app metadata bound from application.yml (prefix=goalvault).
 *
 * LEARNING: @ConfigurationProperties gathers a chunk of externalised config into a strongly
 * typed POJO. Beats scattering @Value("${...}") across the code — IDE autocomplete, type
 * checking, and clear ownership. Registered via @EnableConfigurationProperties in WebConfig.
 *
 * Coexists with RateLimitProperties (prefix=goalvault.rate-limit) — both live under the same
 * root but bind to different fields, which is exactly the pattern @ConfigurationProperties
 * was designed for.
 */
@ConfigurationProperties(prefix = "goalvault")
public class AppMetadata {

    private String name = "GoalVault";
    private String description = "";
    private int defaultPageSize = 20;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDefaultPageSize() { return defaultPageSize; }
    public void setDefaultPageSize(int defaultPageSize) { this.defaultPageSize = defaultPageSize; }
}
