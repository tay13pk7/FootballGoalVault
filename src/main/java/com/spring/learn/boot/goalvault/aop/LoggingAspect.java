package com.spring.learn.boot.goalvault.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Cross-cutting method-level logging for the service layer.
 *
 * LEARNING: @Aspect marks this as an AspectJ aspect. Combined with @Component it becomes a
 * Spring bean that Spring's AOP proxy machinery weaves into matching beans. Because Spring
 * AOP is PROXY-based (JDK dynamic proxies for interfaces, CGLIB otherwise), advice only fires
 * on calls that go THROUGH the proxy — self-invocation inside the same class is not woven.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    // LEARNING: @Pointcut factors out an expression so multiple advice can reuse it.
    // "execution(* com.spring.learn.boot.goalvault.service..*(..))" matches every method
    // (any return type, any args) in the service package and its sub-packages.
    @Pointcut("execution(* com.spring.learn.boot.goalvault.service..*(..))")
    public void serviceLayer() {}

    // Narrower pointcut: only *ServiceImpl classes — used by @Before below so its output stays
    // distinct from the @Around advice that already covers the whole service layer.
    @Pointcut("execution(* com.spring.learn.boot.goalvault.service.impl.*ServiceImpl.*(..))")
    public void serviceImpls() {}

    // Narrower still: only mutating methods, used by @AfterThrowing so we specifically flag
    // exceptions from create/update/delete/scoreGoal etc.
    @Pointcut("execution(* com.spring.learn.boot.goalvault.service..*.create(..)) || " +
              "execution(* com.spring.learn.boot.goalvault.service..*.update(..)) || " +
              "execution(* com.spring.learn.boot.goalvault.service..*.delete(..)) || " +
              "execution(* com.spring.learn.boot.goalvault.service..*.scoreGoal(..))")
    public void mutatingServiceMethods() {}

    // LEARNING: @Around gives the fullest control — you get the ProceedingJoinPoint and decide
    // when (or if) to call proceed(). Great for timing, retry, caching, transaction-like patterns.
    @Around("serviceLayer()")
    public Object timeServiceMethods(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();
        try {
            return pjp.proceed();
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
            log.debug("[AOP @Around] {} took {} ms",
                    pjp.getSignature().toShortString(), elapsedMs);
        }
    }

    // LEARNING: @Before runs BEFORE the join point method executes. Cannot alter args or short-circuit;
    // typical uses are auditing, argument logging, or lightweight authorization checks.
    @Before("serviceImpls()")
    public void logEntry(org.aspectj.lang.JoinPoint jp) {
        if (log.isDebugEnabled()) {
            log.debug("[AOP @Before ] entering {} with args={}",
                    jp.getSignature().toShortString(),
                    Arrays.toString(jp.getArgs()));
        }
    }

    // LEARNING: @AfterThrowing runs when the target method exits by throwing. Great for structured
    // error logging. The `throwing` attribute names the parameter that receives the exception.
    @AfterThrowing(pointcut = "mutatingServiceMethods()", throwing = "ex")
    public void logException(org.aspectj.lang.JoinPoint jp, Throwable ex) {
        log.warn("[AOP @AfterThrowing] {} threw {}: {}",
                jp.getSignature().toShortString(),
                ex.getClass().getSimpleName(),
                ex.getMessage());
    }
}
