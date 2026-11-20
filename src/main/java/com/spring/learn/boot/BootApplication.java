package com.spring.learn.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

// LEARNING: @SpringBootApplication is a meta-annotation = @Configuration + @EnableAutoConfiguration + @ComponentScan.
// It bootstraps the app, enables auto-config based on classpath, and scans this package (and children) for beans.
// Its implicit @ComponentScan already covers "com.spring.learn.boot" and everything under it, so we don't declare
// a redundant explicit @ComponentScan here (doing so also breaks Spring Boot test slices like @DataJpaTest /
// @WebMvcTest that rely on the SpringBootApplication's TypeExcludeFilter machinery).
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class BootApplication {

	public static void main(String[] args) {
		SpringApplication.run(BootApplication.class, args);
	}

}
