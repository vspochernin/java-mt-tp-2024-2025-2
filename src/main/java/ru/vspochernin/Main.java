package ru.vspochernin;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.RejectedExecutionException;

public class Main {
    public static void main(String[] args) {
        // Демонстрация разных политик отказа
        demonstrateRejectionPolicy(RejectedExecutionPolicies.ABORT, "ABORT");
        demonstrateRejectionPolicy(RejectedExecutionPolicies.CALLER_RUNS, "CALLER_RUNS");
        demonstrateRejectionPolicy(RejectedExecutionPolicies.RETRY_AFTER_DELAY(1, TimeUnit.SECONDS), "RETRY");
    }

    private static void demonstrateRejectionPolicy(CustomRejectedExecutionHandler policy, String policyName) {
        System.out.println("\n=== Demonstrating " + policyName + " policy ===");
        
        // Создаем пул с маленькими параметрами для демонстрации отклонений:
        // corePoolSize = 2
        // maxPoolSize = 3
        // minSpareThreads = 1
        // keepAliveTime = 1 секунда
        // queueSize = 2
        CustomThreadPool pool = new CustomThreadPool(
            2, 3, 1,
            1, TimeUnit.SECONDS,
            2, policy
        );

        // Создаем и отправляем задачи быстрее, чем они могут быть обработаны
        for (int i = 0; i < 20; i++) {
            final int taskId = i;
            try {
                pool.execute(() -> {
                    System.out.println("Task " + taskId + " started");
                    try {
                        Thread.sleep(2000); // Увеличиваем время выполнения задачи
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.println("Task " + taskId + " completed");
                });
                System.out.println("Task " + taskId + " submitted");
            } catch (RejectedExecutionException e) {
                System.out.println("Task " + taskId + " was rejected: " + e.getMessage());
            }
        }

        // Ждем некоторое время
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Завершаем работу пула
        pool.shutdown();
    }
}