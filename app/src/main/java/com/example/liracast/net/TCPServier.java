package com.example.liracast.net;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class TCPServier {
    private final String TAG = "TCPServier";
    public ServerSocket mSocket;

    public InputStream receive() {
        try {
            mSocket = new ServerSocket(9528);
            Socket s = mSocket.accept();
            Log.d(TAG, s.getInetAddress().toString());
            return s.getInputStream();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }
}
