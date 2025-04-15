package ru.vspochernin;

import java.util.concurrent.RejectedExecutionException;

@FunctionalInterface
public interface CustomRejectedExecutionHandler {

    void rejectedExecution(Runnable r, CustomThreadPool executor) throws RejectedExecutionException;
} 