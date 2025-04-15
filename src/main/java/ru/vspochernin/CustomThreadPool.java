package ru.vspochernin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadPool implements CustomExecutor {

    private final int corePoolSize;
    private final int maxPoolSize;
    private final int minSpareThreads;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private final BlockingQueue<Runnable> taskQueue;
    private final ThreadFactory threadFactory;
    private final List<Worker> workers;
    private final AtomicInteger activeWorkers;
    private volatile boolean isShutdown;

    public CustomThreadPool(
            int corePoolSize,
            int maxPoolSize,
            int minSpareThreads,
            long keepAliveTime,
            TimeUnit timeUnit,
            int queueSize)
    {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.minSpareThreads = minSpareThreads;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.taskQueue = new LinkedBlockingQueue<>(queueSize);
        this.threadFactory = new CustomThreadFactory("CustomThreadPool");
        this.workers = new ArrayList<>();
        this.activeWorkers = new AtomicInteger(0);
        this.isShutdown = false;

        // Инициализация базовых потоков.
        for (int i = 0; i < this.corePoolSize; i++) {
            addWorker();
        }
    }

    private void addWorker() {
        if (activeWorkers.get() >= maxPoolSize) {
            return;
        }

        Worker worker = new Worker(taskQueue, keepAliveTime, timeUnit);
        workers.add(worker);
        Thread thread = threadFactory.newThread(worker);
        thread.start();
        activeWorkers.incrementAndGet();
    }

    @Override
    public void execute(Runnable command) {
        if (isShutdown) {
            throw new RejectedExecutionException("[Rejected] ThreadPool is shutdown");
        }

        if (!taskQueue.offer(command)) {
            if (activeWorkers.get() < maxPoolSize) {
                addWorker();
                if (!taskQueue.offer(command)) {
                    throw new RejectedExecutionException("[Rejected] Queue is full and cannot create new worker");
                }
            } else {
                throw new RejectedExecutionException("[Rejected] Queue is full and max pool size reached");
            }
        }

        System.out.println("[Pool] Task accepted into queue");
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        FutureTask<T> futureTask = new FutureTask<>(task);
        execute(futureTask);
        return futureTask;
    }

    @Override
    public void shutdown() {
        isShutdown = true;
        for (Worker worker : workers) {
            worker.stop();
        }
    }

    @Override
    public void shutdownNow() {
        isShutdown = true;
        taskQueue.clear();
        for (Worker worker : workers) {
            worker.stop();
        }
    }
} 