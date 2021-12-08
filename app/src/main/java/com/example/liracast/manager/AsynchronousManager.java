package com.example.liracast.manager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class AsynchronousManager {
    private final String TAG = "AsynchronousManager";
    private volatile static AsynchronousManager sInstance;

    private HandlerThread mWorkerThread = null;
    private Handler mWorkerHandler = null;

    public static AsynchronousManager getInstance() {
        if (null == sInstance) {
            synchronized (AsynchronousManager.class) {
                if (null == sInstance) {
                    sInstance = new AsynchronousManager();
                }
            }
        }
        return sInstance;
    }

    private AsynchronousManager() {
        mWorkerThread = new HandlerThread(TAG);
        mWorkerThread.start();
        Looper looper = mWorkerThread.getLooper();
        if (looper != null) {
            mWorkerHandler = new SyncHandler(looper);
        } else {
            Log.e(TAG, "onCreate looper = null");
        }
    }

    public void postRunnabe(Runnable runnable) {
        if ((runnable != null) && (mWorkerHandler != null)) {
            mWorkerHandler.post(runnable);
        }
    }

    public void postRunnabeDelay(Runnable runnable, long delayTime) {
        if ((runnable != null) && (mWorkerHandler != null)) {
            mWorkerHandler.postDelayed(runnable, delayTime);
        }
    }

    public void postRunnable2NewThread(Runnable runnable) {
        new Thread(runnable).start();
    }

    private final class SyncHandler extends Handler {
        public SyncHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }
}
