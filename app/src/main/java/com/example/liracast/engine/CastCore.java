package com.example.liracast.engine;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pWfdInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.example.liracast.MainActivity;
import com.example.liracast.R;
import com.example.liracast.SourceActivity;
import com.example.liracast.global.Config;
import com.example.liracast.global.ResourceManager;
import com.example.liracast.manager.AsynchronousManager;
import com.example.liracast.net.p2p.P2pListener;
import com.example.liracast.net.tcp.TCPSender;
import com.example.liracast.service.DeviceInfo;
import com.example.liracast.service.ILiracastListener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class CastCore {
    private final String TAG = "CastCore";
    private Context mContext;
    private HandlerThread mWorkerThread = null;
    private Handler mWorkerHandler = null;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private MediaCodec mMediaCodecEn;
    private DeviceInfo mCurrentDevice;
    private RemoteCallbackList<ILiracastListener> mListenerList = new RemoteCallbackList<>();

    private final class CallbackHandler extends Handler {
        public CallbackHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            onMessageReceived(msg);
        }
    }

    private P2pListener mP2pListener = new P2pListener() {
        @Override
        public void onDeviceSearched(WifiP2pDevice wifiP2pDevice) {
            if (!isWFD(wifiP2pDevice.getWfdInfo().getDeviceType())) {
                Log.e(TAG, "not wfd device");
                return;
            }
            synchronized (mListenerList) {
                int count = mListenerList.beginBroadcast();
                for (int i = 0; i < count; i++) {
                    try {
                        DeviceInfo deviceInfo = new DeviceInfo();
                        deviceInfo.setName(wifiP2pDevice.deviceName);
                        deviceInfo.setAddress(wifiP2pDevice.deviceAddress);
                        deviceInfo.setPort(wifiP2pDevice.getWfdInfo().getControlPort());
                        ILiracastListener listener = mListenerList.getBroadcastItem(i);
                        listener.onDeviceSearched(deviceInfo);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
                mListenerList.finishBroadcast();
            }
        }

        @Override
        public void onDeviceConnect(WifiP2pInfo wifiP2pInfo) {
            nativeListen(mCurrentDevice.getPort());
        }

        @Override
        public void onDeviceDisconnect() {

        }

        @Override
        public void onGroupFinded(WifiP2pGroup wifiP2pGroup) {

        }

        private boolean isWFD(int type) {
            if (type == WifiP2pWfdInfo.DEVICE_TYPE_PRIMARY_SINK ||
                    type == WifiP2pWfdInfo.DEVICE_TYPE_SECONDARY_SINK ||
                    type == WifiP2pWfdInfo.DEVICE_TYPE_SOURCE_OR_PRIMARY_SINK) {
                return true;
            }
            return false;
        }
    };

    public CastCore(Context context) {
        Log.d(TAG, "CastCore");
        mContext = context;
        mWorkerThread = new HandlerThread(TAG);
        mWorkerThread.start();
        Looper looper = mWorkerThread.getLooper();
        if (looper != null) {
            mWorkerHandler = new CallbackHandler(looper);
        } else {
            Log.e(TAG, "onCreate looper = null");
        }
    }

    public void init(Intent intent) {
        Log.d(TAG, "init");
        ResourceManager.getInstance().getP2pAdapter().registerListener(mP2pListener);

        Intent data = intent.getParcelableExtra(Config.SOURCE_INTENT_DATA);
        int resultCode = intent.getIntExtra(Config.SOURCE_INTENT_RESULTCODE, -100);
        if (data != null && resultCode != -100) {
            mMediaProjectionManager = (MediaProjectionManager)mContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
            try {
                MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
                MediaCodecInfo[] mediaCodecInfos = mediaCodecList.getCodecInfos();
                for (MediaCodecInfo t: mediaCodecInfos) {
                    Log.d(TAG, t.getName() + t.isEncoder());
                }
                mMediaCodecEn = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);

            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    public void setListener(ILiracastListener listener) {
        Log.d(TAG, "setListener");
        synchronized (mListenerList) {
            mListenerList.register(listener);
        }
    }

    public void startSearch() {
        ResourceManager.getInstance().getThreadPoolManager().postRunnable(new Runnable() {
            @Override
            public void run() {
                ResourceManager.getInstance().getP2pAdapter().startSearch();
            }
        });
    }

    public void stopSearch() {
        ResourceManager.getInstance().getThreadPoolManager().postRunnable(new Runnable() {
            @Override
            public void run() {
                ResourceManager.getInstance().getP2pAdapter().stopSearch();
            }
        });
    }

    public void startMirror(DeviceInfo deviceInfo) {
        Log.d(TAG, "startMirror onDevice: " + deviceInfo);
        mCurrentDevice = deviceInfo;
        stopSearch();
        ResourceManager.getInstance().getThreadPoolManager().postRunnable(new Runnable() {
            @Override
            public void run() {
                ResourceManager.getInstance().getP2pAdapter().connect(deviceInfo);
            }
        });
        /*if (mVirtualDisplay == null) {
            AsynchronousManager.getInstance().postRunnable2NewThread(new Runnable() {
                @Override
                public void run() {
                    mTcpSender = new TCPSender();
                }
            });
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 270,480);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 128000);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 60);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            mMediaCodecEn.configure(mediaFormat,
                    null,
                    null,
                    MediaCodec.CONFIGURE_FLAG_ENCODE);
            Surface surface = mMediaCodecEn.createInputSurface();
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("liracast",
                    270,
                    480,
                    400,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    surface, null, null);
            mMediaCodecEn.setCallback(new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {

                }

                @Override
                public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                    AsynchronousManager.getInstance().postRunnabe(new Runnable() {
                        @Override
                        public void run() {
                            ByteBuffer byteBuffer = codec.getOutputBuffer(index);
                            Log.d(TAG, "getOutputBuffer: " + byteBuffer.toString());
                            if (mTcpSender != null) {
                                mTcpSender.send(byteBuffer);
                            }
                            codec.releaseOutputBuffer(index, 0);
                        }
                    });
                }

                @Override
                public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

                }

                @Override
                public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

                }
            });

            mMediaCodecEn.start();
        }*/
    }

    public void stopMirror(DeviceInfo deviceInfo) {
        Log.d(TAG, "stopMirror");
        mMediaCodecEn.stop();
    }

    private void nativeListen(int port) {
        Log.d(TAG, "nativeListen: " + port);

    }

    private void onMessageReceived(Message msg) {

    }
}
