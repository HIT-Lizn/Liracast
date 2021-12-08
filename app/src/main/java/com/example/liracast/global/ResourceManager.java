package com.example.liracast.global;

import android.content.Context;
import android.view.SurfaceView;

public class ResourceManager {
    private final String TAG = "ResourceManager";
    private volatile static ResourceManager sInstance = null;
    private Context mContext;
    private SurfaceView mSurfaceView;

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
}
