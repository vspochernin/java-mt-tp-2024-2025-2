package ru.vspochernin;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadFactory implements ThreadFactory {

    private final String poolName;
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    public CustomThreadFactory(String poolName) {
        this.poolName = poolName;
    }

    @Override
    public Thread newThread(Runnable r) {
        String threadName = poolName + "-worker-" + threadNumber.getAndIncrement();
        System.out.println("[ThreadFactory] Creating new thread: " + threadName);

        Thread thread = new Thread(r, threadName);
        thread.setDaemon(false);

        return thread;
    }
} 