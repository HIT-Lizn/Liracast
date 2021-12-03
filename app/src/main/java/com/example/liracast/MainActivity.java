package com.example.liracast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;

import com.example.liracast.manager.AsynchronousManager;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private final int MEDIAPROJECTION_REQUEST_CODE = 1234;

    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaCodec mMediaCodec;

    private int mWidth = 1080;
    private int mHeight = 1920;
    private int mDpi = 800;
    private Surface mSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        
    }

    private void getMediaProjection() {
        Log.d(TAG, "getMediaProjectionPrivacy: ");
        if (mMediaProjectionManager == null) {
            mMediaProjectionManager = (MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        }
        if (mMediaProjectionManager == null) {
            Log.e(TAG, "Can't get media projection manager");
            return;
        }
        Intent screenCaptureIntent = mMediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(screenCaptureIntent, MEDIAPROJECTION_REQUEST_CODE);
    }

    private void initMediaCodec() {

    }

    private void initVirtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("Liracast",
                mWidth,
                mHeight,
                mDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                mSurface,
                null,
                null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MEDIAPROJECTION_REQUEST_CODE && requestCode == RESULT_OK) {
            mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
            if (mMediaProjection == null) {
                Log.e(TAG, "Make media projection failed");
                return;
            }
            AsynchronousManager.getInstance().postRunnabe(new Runnable() {
                @Override
                public void run() {
                    initMediaCodec();
                    initVirtualDisplay();
                }
            });
        }
    }
}