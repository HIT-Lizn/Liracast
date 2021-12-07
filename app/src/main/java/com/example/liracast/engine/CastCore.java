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
import com.example.liracast.net.TCPSender;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class CastCore {
    private final String TAG = "CastCore";
    private Context mContext;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaCodec mMediaCodecEn;
    private MediaCodec mMediaCodecDe;
    private SurfaceView mSurfaceView;
    private TCPSender mTcpSender;
    private ArrayList<ByteBuffer> byteBuffers = new ArrayList<>();

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
                mMediaCodecEn = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
                mMediaCodecDe = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    public void startMirror() {
        Log.d(TAG, "startMirror");
        if (mVirtualDisplay == null) {
            //mTcpSender = new TCPSender();
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 360,540);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 64000);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 60);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            mMediaCodecEn.configure(mediaFormat,
                    null,
                    null,
                    MediaCodec.CONFIGURE_FLAG_ENCODE);
            Surface surface = mMediaCodecEn.createInputSurface();
            mVirtualDisplay = mMediaProjection.createVirtualDisplay("liracast",
                    1080,
                    1920,
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
                            //mTcpSender.send(byteBuffer);
                            synchronized (byteBuffers) {
                                byteBuffers.add(byteBuffer);
                            }
                            Log.d(TAG, "byteBuffers.add size: " + byteBuffers.size());
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

            MediaFormat mediaFormat1 = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 360,540);
            mMediaCodecDe.configure(mediaFormat1,
                    mSurfaceView.getHolder().getSurface(),
                    null,
                    0);
            mMediaCodecDe.setCallback(new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                    ByteBuffer input = mMediaCodecDe.getInputBuffer(index);
                    input.clear();
                    int size = 0;
                    synchronized (byteBuffers) {
                        if (!byteBuffers.isEmpty()) {
                            size = byteBuffers.get(0).remaining();
                            input.put(byteBuffers.get(0));
                            byteBuffers.remove(0);
                            Log.d(TAG, "put buffer: " + input.toString() + " size: " + size);
                        }
                    }
                    codec.queueInputBuffer(index, 0, size, 0, 0);
                }

                @Override
                public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                    AsynchronousManager.getInstance().postRunnabe(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "feed to surface");
                            codec.releaseOutputBuffer(index, true);
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
            mMediaCodecDe.start();
        }
    }

    public void stopMirror() {
        Log.d(TAG, "stopMirror");
        mMediaCodecEn.stop();
    }
}
