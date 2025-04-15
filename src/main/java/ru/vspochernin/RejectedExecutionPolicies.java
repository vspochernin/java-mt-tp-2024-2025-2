package ru.vspochernin;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class RejectedExecutionPolicies {
    // Просто отклоняет задачу.
    public static final CustomRejectedExecutionHandler ABORT = (r, executor) -> {
        throw new RejectedExecutionException("[Rejected] Task was rejected due to overload");
    };

    // Выполняет задачу в текущем потоке.
    public static final CustomRejectedExecutionHandler CALLER_RUNS = (r, executor) -> {
        System.out.println("[Rejected] Task will be executed in caller thread");
        r.run();
    };

    // Пытается добавить задачу в очередь снова через некоторое время.
    public static CustomRejectedExecutionHandler RETRY_AFTER_DELAY(long delay, TimeUnit unit) {
        return (r, executor) -> {
            try {
                System.out.println("[Rejected] Task will be retried after delay");
                Thread.sleep(unit.toMillis(delay));
                executor.execute(r);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RejectedExecutionException("[Rejected] Retry was interrupted", e);
            }
        };
    }
} 