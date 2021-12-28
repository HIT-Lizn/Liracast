package com.example.liracast.global;

import android.content.Context;
import android.view.SurfaceView;

import com.example.liracast.thread.ThreadPoolManager;

public class ResourceManager {
    private final String TAG = "ResourceManager";
    private volatile static ResourceManager sInstance = null;
    private Context mContext;
    private SurfaceView mSurfaceView;
    private ThreadPoolManager mThreadPoolManager;

    public static ResourceManager getInstance() {
        if (sInstance == null) {
            synchronized (ResourceManager.class) {
                if (sInstance == null) {
                    sInstance = new ResourceManager();
                }
            }
        }
        return sInstance;
    }

    public void setContext(Context Context) {
        mContext = Context;
    }

    public Context getContext() {
        return mContext;
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        mSurfaceView = surfaceView;
    }

    public SurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    public ThreadPoolManager getThreadPoolManager() {
        synchronized (mThreadPoolManager) {
            if (mThreadPoolManager != null) {
                mThreadPoolManager = new ThreadPoolManager();
            }
        }
        return mThreadPoolManager;
    }
}
