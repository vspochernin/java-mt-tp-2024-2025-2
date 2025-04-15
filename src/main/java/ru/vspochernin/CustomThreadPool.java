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
    private final int queueSize;
    private final List<BlockingQueue<Runnable>> taskQueues;
    private final ThreadFactory threadFactory;
    private final List<Worker> workers;
    private final AtomicInteger activeWorkers;
    private final AtomicInteger nextQueueIndex;
    private final CustomRejectedExecutionHandler rejectedExecutionHandler;
    private volatile boolean isShutdown;

    public CustomThreadPool(
            int corePoolSize,
            int maxPoolSize,
            int minSpareThreads,
            long keepAliveTime,
            TimeUnit timeUnit,
            int queueSize)
    {
        this(
                corePoolSize,
                maxPoolSize,
                minSpareThreads,
                keepAliveTime,
                timeUnit,
                queueSize,
                RejectedExecutionPolicies.ABORT);
    }

    public CustomThreadPool(
            int corePoolSize,
            int maxPoolSize,
            int minSpareThreads,
            long keepAliveTime,
            TimeUnit timeUnit,
            int queueSize,
            CustomRejectedExecutionHandler rejectedExecutionHandler)
    {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.minSpareThreads = minSpareThreads;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.queueSize = queueSize;
        this.taskQueues = new ArrayList<>();
        this.threadFactory = new CustomThreadFactory("CustomThreadPool");
        this.workers = new ArrayList<>();
        this.activeWorkers = new AtomicInteger(0);
        this.nextQueueIndex = new AtomicInteger(0);
        this.rejectedExecutionHandler = rejectedExecutionHandler;
        this.isShutdown = false;

        // Инициализация базовых потоков и очередей.
        for (int i = 0; i < corePoolSize; i++) {
            BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(queueSize);
            taskQueues.add(queue);
            addWorker(queue);
        }
    }

    private void addWorker(BlockingQueue<Runnable> queue) {
        if (activeWorkers.get() >= maxPoolSize) {
            return;
        }

        Worker worker = new Worker(queue, keepAliveTime, timeUnit, this);
        workers.add(worker);
        Thread thread = threadFactory.newThread(worker);
        thread.start();
        activeWorkers.incrementAndGet();
    }

    private BlockingQueue<Runnable> getNextQueue() {
        int index = nextQueueIndex.getAndIncrement() % taskQueues.size();
        return taskQueues.get(index);
    }

    @Override
    public void execute(Runnable command) {
        if (isShutdown) {
            throw new RejectedExecutionException("[Rejected] ThreadPool is shutdown");
        }

        // Сначала проверяем, можем ли создать новый воркер.
        if (activeWorkers.get() < maxPoolSize) {
            BlockingQueue<Runnable> newQueue = new LinkedBlockingQueue<>(queueSize);
            taskQueues.add(newQueue);
            addWorker(newQueue);
            if (newQueue.offer(command)) {
                System.out.println("[Pool] Task accepted into new queue #" + (taskQueues.size() - 1));
                return;
            }
        }

        // Если не смогли создать новый воркер, пробуем добавить в существующую очередь.
        BlockingQueue<Runnable> queue = getNextQueue();
        if (!queue.offer(command)) {
            rejectedExecutionHandler.rejectedExecution(command, this);
            return;
        }

        System.out.println("[Pool] Task accepted into queue #" + taskQueues.indexOf(queue));
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
        for (BlockingQueue<Runnable> queue : taskQueues) {
            queue.clear();
        }
        for (Worker worker : workers) {
            worker.stop();
        }
    }

    synchronized boolean canTerminateWorker() {
        return activeWorkers.get() > corePoolSize &&
                activeWorkers.get() > minSpareThreads;
    }

    synchronized void workerTerminated(Worker worker) {
        workers.remove(worker);
        activeWorkers.decrementAndGet();
        taskQueues.remove(worker.getQueue());
    }
} 