package com.spring.learn.boot.goalvault.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Simulates fan notifications after a match is recorded.
 *
 * LEARNING notes on @Async:
 *   - The annotated method MUST BE PUBLIC. Spring can't proxy package-private/private methods.
 *   - It must be called from a DIFFERENT bean. Self-invocation (this.sendMatchNotification(...))
 *     skips the proxy → runs synchronously. The call in MatchController is safe because
 *     MatchController is a separate bean and Spring hands it the proxied NotificationService.
 *   - Return type is void (fire-and-forget) or Future/CompletableFuture (results back later).
 *   - Without a dedicated Executor bean, Spring uses SimpleAsyncTaskExecutor which creates a
 *     new thread per task. Fine for demos; in production configure a ThreadPoolTaskExecutor.
 */
@Service
public class NotificationService {

    private static final Logger logNotifciation = LoggerFactory.getLogger(NotificationService.class);

    @Async
    public void sendMatchNotification(Long matchId) {
        logNotifciation.info("[async] Fan notification job started for match {} on thread {}",
                matchId, Thread.currentThread().getName());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logNotifciation.warn("[async] Notification interrupted for match {}", matchId);
            return;
        }
        logNotifciation.info("[async] Fan notification job finished for match {}", matchId);
    }
}
