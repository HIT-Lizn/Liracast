package com.example.liracast.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {
    private final String TAG = "ThreadPoolManager";
    private volatile static ThreadPoolManager sInstance = null;
    private ExecutorService mExecutorService;

    public ThreadPoolManager() {
        mExecutorService = new ThreadPoolExecutor(
                10, 20, 1000, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1024),
                new ThreadPoolExecutor.AbortPolicy());
    }

    public void postRunnable(Runnable runnable) {
        mExecutorService.execute(runnable);
    }

    public Object submitCallableSynchronize(Callable<?> callable) {
        Future<?> res = mExecutorService.submit(callable);
        try {
            return res.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void submitCallableAsynchronous(Callable<?> callable) {
        Future<?> res = mExecutorService.submit(callable);
    }
}
