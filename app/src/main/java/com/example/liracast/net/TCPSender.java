package com.example.liracast.net;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class TCPSender {
    private final String TAG = "TCPSender";
    public Socket mSocket;
    public OutputStream mOutputStream;
    public TCPSender() {
        try {
            mSocket = new Socket("192.168.236.247", 9528);
            mOutputStream = mSocket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    public void send(ByteBuffer byteBuffer) {
        try {
            byte[] b = new byte[byteBuffer.remaining()];
            int i = 0;
            while (byteBuffer.remaining() > 0) {
                b[i] = byteBuffer.get();
                i++;
            }
            mOutputStream.write(b);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }


}
