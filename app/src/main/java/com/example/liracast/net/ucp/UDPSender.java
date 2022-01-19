package com.example.liracast.net.ucp;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPSender {
    private final String TAG = "UDPSender";
    private DatagramSocket mDatagramSocket;
    private InetAddress mInetAddress;
    private int mPort;
    private Handler mNotify;

    public UDPSender(Handler handler) {
        mNotify = handler;
    }

    public void setRemote(InetAddress inetAddress,int port) {
        mInetAddress = inetAddress;
        mPort = port;
    }

    public void sendData(byte[] bytes, int length) {
        if (mDatagramSocket == null) {
            try {
                mDatagramSocket = new DatagramSocket();
            } catch (SocketException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
        DatagramPacket packet = new DatagramPacket(bytes, length, mInetAddress, mPort);
        try {
            mDatagramSocket.send(packet);
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }
}
