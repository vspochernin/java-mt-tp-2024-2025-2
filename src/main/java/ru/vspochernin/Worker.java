package ru.vspochernin;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Worker implements Runnable {

    private final BlockingQueue<Runnable> taskQueue;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private volatile boolean isRunning = true;

    public Worker(BlockingQueue<Runnable> taskQueue, long keepAliveTime, TimeUnit timeUnit) {
        this.taskQueue = taskQueue;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
    }

    public void stop() {
        isRunning = false;
        Thread.currentThread().interrupt();
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                Runnable task = taskQueue.poll(keepAliveTime, timeUnit);
                if (task != null) {
                    System.out.println("[Worker] " + Thread.currentThread().getName() + " executes task");
                    task.run();
                } else {
                    System.out.println("[Worker] " + Thread.currentThread().getName() + " idle timeout, stopping");
                    break;
                }
            } catch (InterruptedException e) {
                if (!isRunning) {
                    break;
                }
            }
        }
        System.out.println("[Worker] " + Thread.currentThread().getName() + " terminated");
    }
} 