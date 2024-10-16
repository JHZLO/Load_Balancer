package org.loadBalancer.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolManager {
    private final ExecutorService executor;

    public ThreadPoolManager(int poolSize) {
        this.executor = Executors.newFixedThreadPool(poolSize);
    }

    public void submitTask(Runnable task) {
        executor.submit(task);
    }

    public void shutdown() {
        executor.shutdown();
    }
}
