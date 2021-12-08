package com.example.liracast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import com.example.liracast.manager.AsynchronousManager;
import com.example.liracast.net.tcp.TCPServier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class SinkActivity extends AppCompatActivity {
    private final String TAG = "SinkActivity";
    private MediaCodec mMediaCodecDe;
    private SurfaceView mSurfaceView;
    private InputStream mInputStream;
    private ArrayList<byte[]> byteBuffers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sink);
        mSurfaceView = findViewById(R.id.sink_surfaceview);

        AsynchronousManager.getInstance().postRunnabe(new Runnable() {
            @Override
            public void run() {
                try {
                    mMediaCodecDe = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
                    AsynchronousManager.getInstance().postRunnable2NewThread(new Runnable() {
                        @Override
                        public void run() {
                            TCPServier tcpServier = new TCPServier();
                            Log.d(TAG, "new tcpServier success");
                            mInputStream = tcpServier.receive();
                            Log.d(TAG, "get BufferedReader success");
                            while (true) {
                                try {
                                    byte[] t = new byte[1572864];
                                    int size = mInputStream.read(t, 0, 1572864);
                                    Log.d(TAG, "read from net size: " + size);
                                    if (size > 0) {
                                        byte[] t2 = new byte[size];
                                        for (int i = 0; i < size; i++) {
                                            t2[i] = t[i];
                                        }
                                        synchronized (byteBuffers) {
                                            byteBuffers.add(t2);
                                            Log.d(TAG, "buffers size: " + byteBuffers.size());
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.d(TAG, e.toString());
                                }
                            }
                        }
                    });
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }

                MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 270,480);
                mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);

                mMediaCodecDe.configure(mediaFormat,
                        mSurfaceView.getHolder().getSurface(),
                        null,
                        0);
                mMediaCodecDe.setCallback(new MediaCodec.Callback() {
                    @Override
                    public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                        ByteBuffer input = codec.getInputBuffer(index);
                        input.clear();
                        int size = 0;
                        synchronized (byteBuffers) {
                            if (!byteBuffers.isEmpty()) {
                                size = byteBuffers.get(0).length;
                                input.put(byteBuffers.get(0));
                                byteBuffers.remove(0);
                                Log.d(TAG, "put buffer: " + input.toString() + " size: " + size);
                            }
                        }
                        codec.queueInputBuffer(index, 0, size, 0, 0);
                    }

                    @Override
                    public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                        AsynchronousManager.getInstance().postRunnable2NewThread(new Runnable() {
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
        });
    }
}