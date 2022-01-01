package com.lingdeqin.secrets.task;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ZeroTask {

    private static final String TAG = "ZeroTask";

    public static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 20, 300, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(20),
            new ThreadFactory() {
                private final AtomicInteger mCount = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r,"secrets-task-pool-" + mCount.getAndIncrement());
                }
            },
            new ThreadPoolExecutor.DiscardOldestPolicy());

    protected void onStart(){ }

    protected abstract void onRun();

    protected void onComplete(){ }

    public void start() {
        onStart();
        threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                onRun();
                onComplete();
            }
        });
    }



    public interface TaskCallBack{
        void callback();
    }

}
