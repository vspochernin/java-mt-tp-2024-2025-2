package ru.vspochernin;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Worker implements Runnable {

    private final BlockingQueue<Runnable> taskQueue;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private final CustomThreadPool pool;
    private volatile boolean isRunning = true;
    private final Thread workerThread;

    public Worker(BlockingQueue<Runnable> taskQueue, long keepAliveTime, TimeUnit timeUnit, CustomThreadPool pool) {
        this.taskQueue = taskQueue;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.pool = pool;
        this.workerThread = Thread.currentThread();
    }

    public void stop() {
        isRunning = false;
        workerThread.interrupt();
    }

    public void awaitTermination() throws InterruptedException {
        workerThread.join();
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
                    if (pool.canTerminateWorker()) {
                        System.out.println("[Worker] " + Thread.currentThread().getName() + " idle timeout, stopping");
                        break;
                    }
                }
            } catch (InterruptedException e) {
                if (!isRunning) {
                    break;
                }
            }
        }
        pool.workerTerminated(this);
        System.out.println("[Worker] " + Thread.currentThread().getName() + " terminated");
    }

    public BlockingQueue<Runnable> getQueue() {
        return taskQueue;
    }
} 