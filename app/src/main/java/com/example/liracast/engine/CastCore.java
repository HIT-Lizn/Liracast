package com.example.liracast.engine;

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
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import com.example.liracast.MainActivity;
import com.example.liracast.R;
import com.example.liracast.manager.AsynchronousManager;

import java.io.IOException;
import java.nio.ByteBuffer;

public class CastCore {
    private final String TAG = "CastCore";
    private Context mContext;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaCodec mMediaCodec;
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
            try {
                MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
                MediaCodecInfo[] mediaCodecInfos = mediaCodecList.getCodecInfos();
                for (MediaCodecInfo t: mediaCodecInfos) {
                    Log.d(TAG, t.getName() + t.isEncoder());
                }
                //mMediaCodec = MediaCodec.createByCodecName("c2.android.avc.encoder");
                mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    public void startMirror() {
        Log.d(TAG, "startMirror");
        if (mVirtualDisplay == null) {
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 720,1080);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 64000);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 60);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            mMediaCodec.configure(mediaFormat,
                    mSurfaceView.getHolder().getSurface(),
                    null,
                    MediaCodec.CONFIGURE_FLAG_ENCODE);
            Surface surface = mMediaCodec.createInputSurface();
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("liracast",
                    1080,
                    1920,
                    400,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    surface, null, null);
            //mMediaCodec.setInputSurface(surface);
            mMediaCodec.setOutputSurface(mSurfaceView.getHolder().getSurface());
            mMediaCodec.start();
            AsynchronousManager.getInstance().postRunnabe(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            ByteBuffer byteBuffer = mMediaCodec.getOutputBuffer(0);
                            Log.d(TAG, byteBuffer.toString());
                        } catch (Exception e) {
                            Log.d(TAG, e.toString());
                        }
                    }
                }
            });
        }
    }

    public void stopMirror() {
        Log.d(TAG, "stopMirror");

    }
}
