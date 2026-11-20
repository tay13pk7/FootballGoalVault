package com.spring.learn.boot.goalvault.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// LEARNING: @ConfigurationProperties binds a chunk of application.yml under a common prefix to a POJO.
// Beats scattering @Value("${...}") calls, and gives IDE autocompletion + type safety.
// Enabled via @EnableConfigurationProperties (see RateLimitConfig) OR @ConfigurationPropertiesScan.
@ConfigurationProperties(prefix = "goalvault.rate-limit")
public class RateLimitProperties {

    /** Max requests allowed per client IP per minute. Applied to /api/v1/**. */
    private int requestsPerMinute = 120;

    /** Master switch — set to false to disable the interceptor entirely (e.g., for local dev). */
    private boolean enabled = true;

    public int getRequestsPerMinute() { return requestsPerMinute; }
    public void setRequestsPerMinute(int requestsPerMinute) { this.requestsPerMinute = requestsPerMinute; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
