package com.example.liracast.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.SurfaceView;

import androidx.core.app.NotificationCompat;

import com.example.liracast.R;
import com.example.liracast.engine.CastCore;

public class LiracastSourceService extends Service {
    private final String TAG = "LiracastSourceService";
    private IBinder mBinder;
    private CastCore mCastCore;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        if (mCastCore == null) {
            mCastCore = new CastCore(this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        notifyAndStartForeGround();
        if (mCastCore != null) {
            mCastCore.init(intent);
        }
        if (mBinder == null) {
            mBinder = new LiracastSourceServiceStub();
        }
        return mBinder;
    }

    private class LiracastSourceServiceStub extends ILiracastSourceService.Stub {

        @Override
        public void startMirror() throws RemoteException {
            mCastCore.startMirror();
        }

        @Override
        public void stopMirror() throws RemoteException {
            mCastCore.stopMirror();
        }

    }

    private void notifyAndStartForeGround() {
        Log.d(TAG, "notifyAndStartForeGround");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("channelID", "name", importance);
            channel.setDescription("description");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channelID")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("title")
                    .setContentText("content")
                    .setPriority(NotificationCompat.PRIORITY_MAX);

            //notificationManager.notify(1234, builder.build());
            startForeground(1314, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        }
    }
}
