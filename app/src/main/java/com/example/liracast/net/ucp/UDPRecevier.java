package com.example.liracast.net.ucp;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.liracast.global.ResourceManager;
import com.example.liracast.manager.AsynchronousManager;
import com.example.liracast.net.session.LiNetWorkSession;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPRecevier {
    private final String TAG = "UDPRecevier";
    private DatagramSocket mDatagramSocket;
    private Handler mNotify;
    private boolean state;

    public UDPRecevier(Handler handler) {
        mNotify = handler;
    }

    public void startListen(int port) {
        if (mDatagramSocket != null) {
            mDatagramSocket.close();
            mDatagramSocket = null;
        }
        try {
            mDatagramSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            Log.e(TAG, "new socket failed");
            return;
        }
        byte[] bytes = new byte[2048];
        DatagramPacket datagramPacket = new DatagramPacket(bytes, 2048);
        ResourceManager.getInstance().getThreadPoolManager().postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    state = true;
                    while (state) {
                        mDatagramSocket.receive(datagramPacket);
                        String info = new String(bytes, 0, datagramPacket.getLength());
                        Message msg = new Message();
                        msg.what = LiNetWorkSession.UDP_REC_DATA;
                        msg.obj = info;
                        mNotify.sendMessage(msg);
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }
        });
    }

    public void stopListen() {
        state = false;
        mDatagramSocket.close();
        mDatagramSocket = null;
    }
}
