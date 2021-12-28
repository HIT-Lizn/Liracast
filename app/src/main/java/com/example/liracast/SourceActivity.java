package com.example.liracast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.liracast.global.Config;
import com.example.liracast.global.ResourceManager;
import com.example.liracast.manager.AsynchronousManager;
import com.example.liracast.service.ILiracastSourceService;

public class SourceActivity extends AppCompatActivity {
    private final String TAG = "SourceActivity";
    private final int MEDIAPROJECTION_REQUEST_CODE = 1234;
    private ILiracastSourceService mILiracastSourceService;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mILiracastSourceService = ILiracastSourceService.Stub.asInterface(service);
            ResourceManager.getInstance().getAsynchronousManager().postRunnabe(new Runnable() {
                @Override
                public void run() {
                    try {
                        mILiracastSourceService.startMirror();
                    } catch (RemoteException e) {
                        Log.e(TAG, e.toString());
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source);

        ResourceManager.getInstance().getAsynchronousManager().postRunnabe(new Runnable() {
            @Override
            public void run() {
                getMediaProjection();
            }
        });
    }

    private void getMediaProjection() {
        Log.d(TAG, "getMediaProjectionPrivacy");
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mediaProjectionManager == null) {
            Log.e(TAG, "Can't get media projection manager");
            return;
        }
        Intent screenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(screenCaptureIntent, MEDIAPROJECTION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
        if (requestCode == MEDIAPROJECTION_REQUEST_CODE && resultCode == RESULT_OK) {
            ResourceManager.getInstance().getAsynchronousManager().postRunnabe(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent serviceIntent = new Intent();
                        serviceIntent.setAction(Config.SOURCE_SERVICE_ACTION);
                        serviceIntent.setPackage(getPackageName());
                        serviceIntent.putExtra(Config.SOURCE_INTENT_DATA, data);
                        serviceIntent.putExtra(Config.SOURCE_INTENT_RESULTCODE, resultCode);
                        bindService(serviceIntent, conn, Service.BIND_AUTO_CREATE);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
            });
        }
    }
}