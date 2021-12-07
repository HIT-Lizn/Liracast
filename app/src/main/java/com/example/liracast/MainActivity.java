package com.example.liracast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.liracast.manager.AsynchronousManager;
import com.example.liracast.net.TCPServier;
import com.example.liracast.service.ILiracastService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private final String SERVICE_ACTION = "liracast_service";
    private final int MEDIAPROJECTION_REQUEST_CODE = 1234;
    public static SurfaceView mSurfaceView;
    private ILiracastService mILiracastService;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mILiracastService = ILiracastService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");
        mSurfaceView = findViewById(R.id.surfaceView);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsynchronousManager.getInstance().postRunnabe(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "source", Toast.LENGTH_SHORT).show();
                        try {
                            mILiracastService.startMirror();
                        } catch (RemoteException e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }
        });
        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "sink", Toast.LENGTH_SHORT).show();
                /*AsynchronousManager.getInstance().postRunnabe(new Runnable() {
                    @Override
                    public void run() {*/
                        TCPServier tcpServier =  new TCPServier();
                        try {
                            MediaCodec mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
                            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 360,540);
                            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
                            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 64000);
                            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 60);
                            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
                            mediaCodec.configure(mediaFormat, mSurfaceView.getHolder().getSurface(), null,0);
                            mediaCodec.setCallback(new MediaCodec.Callback() {
                                @Override
                                public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {

                                }

                                @Override
                                public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                                    Log.d(TAG, "send to surface");
                                    codec.releaseOutputBuffer(index, true);
                                }

                                @Override
                                public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

                                }

                                @Override
                                public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

                                }
                            });
                            mediaCodec.start();
                            Log.d(TAG, "init codec success");
                            InputStream inputStream = tcpServier.receive();
                            Log.d(TAG, "tcp connect success");
                            while (true) {
                                byte[] b = new byte[1000];
                                int size = inputStream.read(b, 0, 1000);
                                Log.d(TAG, b.toString());
                                int index = mediaCodec.dequeueInputBuffer(100);
                                if (index != -1) {
                                    ByteBuffer byteBuffer = mediaCodec.getInputBuffer(index);
                                    byteBuffer.put(b);
                                    Log.d(TAG, byteBuffer.toString());
                                    mediaCodec.queueInputBuffer(index, 0, size, 0, 0);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    /*}
                });*/

            }
        });
        AsynchronousManager.getInstance().postRunnabe(new Runnable() {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
        if (requestCode == MEDIAPROJECTION_REQUEST_CODE && resultCode == RESULT_OK) {
            AsynchronousManager.getInstance().postRunnabe(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent serviceIntent = new Intent();
                        serviceIntent.setAction(SERVICE_ACTION);
                        serviceIntent.setPackage(getPackageName());
                        serviceIntent.putExtra("data", data);
                        serviceIntent.putExtra("resultCode", resultCode);
                        bindService(serviceIntent, conn, Service.BIND_AUTO_CREATE);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
            });
        }
    }
}