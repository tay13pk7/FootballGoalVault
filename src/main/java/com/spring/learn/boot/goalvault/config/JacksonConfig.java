package com.spring.learn.boot.goalvault.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

// LEARNING: @Configuration marks a class as a source of bean definitions. Methods annotated with
// @Bean here become Spring-managed singletons.
@Configuration
public class JacksonConfig {

    // LEARNING: @Bean tells Spring "call this method and register the returned object as a bean."
    // Jackson2ObjectMapperBuilderCustomizer lets us tweak the auto-configured ObjectMapper
    // WITHOUT having to define our own from scratch — Spring Boot still merges its defaults with ours.
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder
                .indentOutput(true)                       // pretty-print JSON responses
                .failOnUnknownProperties(false)           // ignore unknown JSON fields on inbound requests
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .dateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX"));
    }
}
