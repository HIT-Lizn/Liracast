package com.example.liracast.net.tcp;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.ByteBuffer;

public class TCPSender {
    private final String TAG = "TCPSender";
    public Socket mSocket;
    public OutputStream mOutputStream;
    public TCPSender() {
        try {
            mSocket = new Socket("192.168.236.11", 9529);
            mOutputStream = mSocket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    public void send(ByteBuffer byteBuffer) {
        try {
            int len = byteBuffer.remaining();
            Log.d(TAG, "bf info: " + byteBuffer.toString());
            byte[] b = new byte[len];
            for (int i = 0; i < len; i++) {
                b[i] = byteBuffer.get();
            }
            mOutputStream.write(b);
            mOutputStream.flush();
            Log.d(TAG, "send bytes: " + b.length);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }


}
