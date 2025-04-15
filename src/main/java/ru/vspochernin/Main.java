package ru.vspochernin;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.RejectedExecutionException;

public class Main {
    public static void main(String[] args) {

        // Создаем пул с параметрами:
        // corePoolSize = 2.
        // maxPoolSize = 4.
        // minSpareThreads = 1.
        // keepAliveTime = 5 секунд.
        // queueSize = 5.
        CustomThreadPool pool = new CustomThreadPool(2, 4, 1, 5, TimeUnit.SECONDS, 5);

        // Создаем и отправляем задачи.
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            try {
                pool.execute(() -> {
                    System.out.println("Task " + taskId + " started");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    System.out.println("Task " + taskId + " completed");
                });
            } catch (RejectedExecutionException e) {
                System.out.println("Task " + taskId + " was rejected: " + e.getMessage());
            }
        }

        // Ждем некоторое время.
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Завершаем работу пула.
        pool.shutdown();
    }
}