package com.lingdeqin.secrets.task;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ZeroTask {

    private static final String TAG = "ZeroTask";

    private static final int TASK_COMPLETE = 6;
    private static final int TASK_ERROR = 7;
    private static final int TASK_FINISH = 8;

    private TaskCallBack taskCallBack;
    private Future future;

    private final Handler mainHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case TASK_COMPLETE:
                    onComplete();
                    onCompleteCallback();
                    break;
                case TASK_ERROR:
                    onError((Exception)msg.obj);
                    break;
                case TASK_FINISH:
                    onFinish();
                    break;
            }
        }
    };

    public static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 20, 300, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(20),
            new ThreadFactory() {
                private final AtomicInteger mCount = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r,"secrets-task-" + mCount.getAndIncrement());
                }
            },
            new ThreadPoolExecutor.DiscardOldestPolicy());

    protected void onStart(){ }

    protected abstract void onRun();

    protected void onComplete(){ }

    protected void onFinish(){ }

    private void onCompleteCallback(){
        if (taskCallBack != null){
            taskCallBack.onComplete();
        }
    }

    protected void onError(Exception e){ }

    public void start() {
        if (future != null) {
            return;
        }
        onStart();
        future = threadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try{
                    onRun();
                    mainHandler.obtainMessage(TASK_COMPLETE).sendToTarget();
                }catch (Exception e){
                    mainHandler.obtainMessage(TASK_ERROR,e).sendToTarget();
                }finally {
                    mainHandler.obtainMessage(TASK_FINISH).sendToTarget();
                }
            }
        });
    }

    private void sendMessage(int what, Exception e){
        mainHandler.obtainMessage(what,e).sendToTarget();
    }
    private void sendMessage(int what){
        mainHandler.obtainMessage(what).sendToTarget();
    }

    public Boolean cancel(Boolean mayInterruptIfRunning){
        this.taskCallBack = null;
        if (future != null){
            return future.cancel(mayInterruptIfRunning);
        }
        return true;
    }

    public Boolean isCancelled(){
        if (future != null){
            return future.isCancelled();
        }
        return false;
    }

    public ZeroTask addTaskListener(TaskCallBack taskCallBack){
        this.taskCallBack = taskCallBack;
        return this;
    }

    public interface TaskCallBack{
        void onComplete();
    }

}
