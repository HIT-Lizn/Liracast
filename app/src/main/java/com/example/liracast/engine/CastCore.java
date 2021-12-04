package com.example.liracast.engine;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.Log;
import android.view.SurfaceView;

import com.example.liracast.MainActivity;

public class CastCore {
    private final String TAG = "CastCore";
    private Context mContext;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private SurfaceView mSurfaceView;

    public CastCore(Context context) {
        Log.d(TAG, "CastCore");
        mContext = context;
        mSurfaceView = MainActivity.mSurfaceView;
    }

    public void init(Intent intent) {
        Log.d(TAG, "init");
        Intent data = intent.getParcelableExtra("data");
        int resultCode = intent.getIntExtra("resultCode", -100);
        if (data != null && resultCode != -100) {
            mMediaProjectionManager = (MediaProjectionManager)mContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
        }
    }

    public void startMirror() {
        Log.d(TAG, "startMirror");
        if (mVirtualDisplay == null) {
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("liracast",
                    1080,
                    1920,
                    400,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    mSurfaceView.getHolder().getSurface(), null, null);
        }
    }

    public void stopMirror() {
        Log.d(TAG, "stopMirror");

    }
}
