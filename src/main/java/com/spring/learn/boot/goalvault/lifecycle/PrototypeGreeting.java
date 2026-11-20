package com.spring.learn.boot.goalvault.lifecycle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Prototype-scoped bean used purely to illustrate scope behaviour.
 *
 * LEARNING — @Scope:
 *   singleton (default) — one instance per ApplicationContext for the whole JVM lifetime.
 *   prototype           — a fresh instance every time you ASK the container for the bean
 *                         (getBean / ObjectProvider.getObject / injected Provider).
 *
 * The classic trap: injecting a prototype bean into a singleton with plain constructor injection
 * gives the singleton ONE prototype instance forever — because the singleton is wired once at
 * startup. To get a fresh prototype per call you need one of:
 *   - ObjectProvider<PrototypeGreeting> injected, then .getObject() per use
 *   - jakarta.inject.Provider<PrototypeGreeting>
 *   - a method-level lookup annotated with @Lookup
 *   - @Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
 * All four teach the same lesson: the container needs a chance to hand you a new instance each time.
 *
 * PrototypeGreetingUser below shows the ObjectProvider approach.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PrototypeGreeting {

    private final String instanceId = UUID.randomUUID().toString();

    public String greet() {
        return "Hello from prototype instance " + instanceId;
    }

    /**
     * Companion singleton demonstrating the "prototype into singleton" pitfall + fix.
     * Injects an ApplicationContext (or ObjectProvider) so it can request a fresh instance
     * each call, instead of caching the first one it received.
     */
    @Component
    public static class PrototypeGreetingUser {

        private final ApplicationContext ctx;

        @Autowired
        public PrototypeGreetingUser(ApplicationContext ctx) {
            this.ctx = ctx;
        }

        public String freshGreeting() {
            return ctx.getBean(PrototypeGreeting.class).greet();
        }
    }
}
