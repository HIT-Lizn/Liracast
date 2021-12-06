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

import androidx.annotation.NonNull;

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
            mMediaCodec.setCallback(new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {

                }

                @Override
                public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                    Log.d(TAG, "onOutputBufferAvailable: ");
                    ByteBuffer byteBuffer = codec.getOutputBuffer(index);
                    MediaFormat mediaFormat1 = codec.getOutputFormat(index);
                    Log.d(TAG, "getOutputBuffer: " + byteBuffer.toString());
                    Log.d(TAG, "getOutputFormat: " + mediaFormat1.toString());
                    Log.d(TAG, "BufferInfo: " + info.toString());
                    codec.releaseOutputBuffer(index, 0);
                }

                @Override
                public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

                }

                @Override
                public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

                }
            });
            mMediaCodec.start();
        }
    }

    public void stopMirror() {
        Log.d(TAG, "stopMirror");

    }
}
