package com.spring.learn.boot.goalvault.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * GoalVault security. Deliberately simple — HTTP Basic + in-memory users. No JWT/OAuth.
 *
 * LEARNING:
 *   @EnableWebSecurity     — bootstraps Spring Security's web filter chain.
 *   @EnableMethodSecurity  — turns on annotation-driven method security. In Spring Security 6
 *                            @PreAuthorize / @PostAuthorize are on by default; securedEnabled=true
 *                            additionally activates @Secured (which we use on one admin endpoint
 *                            to demonstrate both styles side by side).
 *
 *   @PreAuthorize vs @Secured — practical difference:
 *     @Secured("ROLE_ADMIN")            — plain string, must be a role name. Simple, fast, limited.
 *     @PreAuthorize("hasRole('ADMIN')") — full Spring EL. Can reference method args (#id),
 *                                         principal (authentication), combine expressions
 *                                         (hasRole('X') and #id == principal.id), etc.
 *   Rule of thumb: use @PreAuthorize by default; @Secured is legacy but concise for role-only checks.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    // LEARNING: BCryptPasswordEncoder produces a salted, adaptive one-way hash. Even
    // if the users table leaks, plaintext passwords can't be recovered cheaply.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // LEARNING: InMemoryUserDetailsManager — Spring's canned UserDetailsService for tests/demos.
    // In production, swap in a JdbcUserDetailsManager or a custom UserDetailsService.
    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder encoder) {
        UserDetails user = User.builder()
                .username("user")
                .password(encoder.encode("password"))
                .roles("USER")
                .build();
        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("admin"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user, admin);
    }

    // LEARNING: SecurityFilterChain is the modern replacement for WebSecurityConfigurerAdapter
    // (removed in Spring Security 6). Declarative and easier to compose across profiles.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF disabled — this is a stateless JSON API. For browser-form apps, leave it ON.
                .csrf(AbstractHttpConfigurer::disable)
                // Session policy STATELESS: no HttpSession is created; every request re-authenticates.
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // H2 console renders in a frame — allow same-origin framing so it stays usable in dev.
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        // Admin routes require authentication + ADMIN role (method-level checks
                        // then apply @PreAuthorize / @Secured for finer control).
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/admin/**")).hasRole("ADMIN")
                        // Actuator: expose health + info openly; lock the rest behind ADMIN.
                        .requestMatchers(new AntPathRequestMatcher("/actuator/health"),
                                         new AntPathRequestMatcher("/actuator/info")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/actuator/**")).hasRole("ADMIN")
                        // H2 console open in dev — the profile toggle in application.yml disables it in prod.
                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                        // Everything else is open — the API is the demo surface for this project.
                        .anyRequest().permitAll()
                )
                .httpBasic(org.springframework.security.config.Customizer.withDefaults());
        return http.build();
    }
}
