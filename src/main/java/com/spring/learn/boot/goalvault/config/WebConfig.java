package com.spring.learn.boot.goalvault.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;

// LEARNING: @EnableConfigurationProperties activates one or more @ConfigurationProperties-annotated
// classes as Spring beans. Alternative: annotate the properties class itself with @Component.
@Configuration
@EnableConfigurationProperties({RateLimitProperties.class, AppMetadata.class})
public class WebConfig implements WebMvcConfigurer {

    private final ObjectProvider<RateLimitInterceptor> rateLimitInterceptorProvider;

    public WebConfig(ObjectProvider<RateLimitInterceptor> rateLimitInterceptorProvider) {
        this.rateLimitInterceptorProvider = rateLimitInterceptorProvider;
    }

    // LEARNING: @ConditionalOnProperty acts as a compile-time-safe feature flag. When
    // goalvault.rate-limit.enabled=false, this bean is NEVER created. matchIfMissing=true
    // means: if the property is absent, treat it as true (fail-open, rate-limit stays on).
    // Contrast with reading isEnabled() at request time — that keeps the bean allocated
    // even when disabled. @ConditionalOnProperty gives you a cleaner off-switch.
    @Bean
    @ConditionalOnProperty(name = "goalvault.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
    public RateLimitInterceptor rateLimitInterceptor(RateLimitProperties props) {
        return new RateLimitInterceptor(props);
    }

    // LEARNING: @ConditionalOnMissingBean means "only create this bean if the container doesn't
    // already have a bean of the same type". Classic pattern for library defaults: a starter ships
    // a sensible default that any consumer app can override just by declaring their own @Bean.
    // Here, a downstream test or Configuration could inject a fixed Clock for deterministic time.
    @Bean
    @ConditionalOnMissingBean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }

    // LEARNING: WebMvcConfigurer.addInterceptors is the standard hook to attach an interceptor
    // to specific URL patterns. Path matching uses Spring's AntPathMatcher.
    // We resolve the interceptor from an ObjectProvider so the disabled case (bean absent) works.
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        RateLimitInterceptor interceptor = rateLimitInterceptorProvider.getIfAvailable();
        if (interceptor != null) {
            registry.addInterceptor(interceptor).addPathPatterns("/api/v1/**");
        }
    }
}

