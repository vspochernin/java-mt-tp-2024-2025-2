package ru.vspochernin;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.RejectedExecutionException;

public class Main {
    public static void main(String[] args) {
        // Демонстрация базовой работы пула
        demonstrateBasicPool();
        
        // Демонстрация разных политик отказа
        demonstrateRejectionPolicy(RejectedExecutionPolicies.ABORT, "ABORT");
        demonstrateRejectionPolicy(RejectedExecutionPolicies.CALLER_RUNS, "CALLER_RUNS");
        demonstrateRejectionPolicy(RejectedExecutionPolicies.RETRY_AFTER_DELAY(1, TimeUnit.SECONDS), "RETRY");
    }

    private static void demonstrateBasicPool() {
        System.out.println("\n=== Demonstrating basic pool operation ===");
        
        // Создаем пул с параметрами:
        // corePoolSize = 2
        // maxPoolSize = 4
        // minSpareThreads = 1
        // keepAliveTime = 5 секунд
        // queueSize = 5
        CustomThreadPool pool = new CustomThreadPool(
            2, 4, 1,
            5, TimeUnit.SECONDS,
            5
        );

        // Создаем и отправляем задачи разного типа
        for (int i = 0; i < 8; i++) {
            final int taskId = i;
            try {
                if (i % 2 == 0) {
                    // Короткие задачи
                    pool.execute(() -> {
                        System.out.println("Short task " + taskId + " started");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        System.out.println("Short task " + taskId + " completed");
                    });
                } else {
                    // Длинные задачи
                    pool.execute(() -> {
                        System.out.println("Long task " + taskId + " started");
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        System.out.println("Long task " + taskId + " completed");
                    });
                }
                System.out.println("Task " + taskId + " submitted");
            } catch (RejectedExecutionException e) {
                System.out.println("Task " + taskId + " was rejected: " + e.getMessage());
            }
        }

        // Ждем некоторое время
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Завершаем работу пула
        System.out.println("Initiating shutdown...");
        pool.shutdown();
        System.out.println("Shutdown completed");
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
        System.out.println("Initiating shutdown...");
        pool.shutdown();
        System.out.println("Shutdown completed");
    }
}