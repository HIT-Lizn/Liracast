package com.example.liracast.net.tcp;

import android.util.Log;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServier {
    private final String TAG = "TCPServier";
    public ServerSocket mSocket;

    public InputStream receive() {
        try {
            mSocket = new ServerSocket(9529);
            Socket s = mSocket.accept();
            s.setSoTimeout(5000);
            Log.d(TAG, s.getInetAddress().toString());
            return s.getInputStream();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }
}
