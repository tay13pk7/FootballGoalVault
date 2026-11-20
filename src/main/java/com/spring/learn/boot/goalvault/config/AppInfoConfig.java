package com.spring.learn.boot.goalvault.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

@Configuration
public class AppInfoConfig {

    // LEARNING: @Value injects a property (from application.yml, env vars, or system props).
    // ":default" after the key provides a fallback if the property is missing.
    @Value("${spring.application.name:goalvault}")
    private String appName;

    // A trivial value bean showing @Bean returning a POJO. Any @Component/@Service can @Autowire AppInfo.
    @Bean
    public AppInfo appInfo() {
        return new AppInfo(appName, Instant.now());
    }

    public record AppInfo(String name, Instant startedAt) {}
}
