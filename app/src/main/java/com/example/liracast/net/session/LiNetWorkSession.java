package com.example.liracast.net.session;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.liracast.net.tcp.TCPSender;
import com.example.liracast.net.tcp.TCPServier;
import com.example.liracast.net.ucp.UDPRecevier;
import com.example.liracast.net.ucp.UDPSender;

import java.net.InetAddress;

public class LiNetWorkSession {
    public static final int UDP_REC_DATA = 100;
    private final String TAG = "LiNetWorkSession";
    private Handler mNotify;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private TCPSender mTcpSender;
    private TCPServier mTcpServier;
    private UDPSender mUdpSender;
    private UDPRecevier mUdpRecevier;

    public LiNetWorkSession(Handler notify) {
        mNotify = notify;
        mHandlerThread = new HandlerThread("LiNetWorkSession");
        mHandlerThread.start();
        Looper looper = mHandlerThread.getLooper();
        if (looper != null) {
            mHandler = new NetWorkSessionHandler(looper);
        } else {
            Log.e(TAG, "looper == null");
        }
    }

    public void UDPStartListen(int port) {
        if (mUdpRecevier == null) {
            mUdpRecevier = new UDPRecevier(mHandler);
        }
        mUdpRecevier.startListen(port);
    }

    public void UDPStopListen() {
        if (mUdpRecevier == null) {
            mUdpRecevier = new UDPRecevier(mHandler);
        }
        mUdpRecevier.stopListen();
    }

    public void UDPSetRemote(InetAddress inetAddress, int port) {
        if (mUdpSender == null) {
            mUdpSender = new UDPSender(mHandler);
        }
        mUdpSender.setRemote(inetAddress, port);
    }

    public void UDPSendData(byte[] bytes, int length) {
        if (mUdpSender == null) {
            mUdpSender = new UDPSender(mHandler);
        }
        mUdpSender.sendData(bytes, length);
    }

    private class NetWorkSessionHandler extends Handler {
        public NetWorkSessionHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UDP_REC_DATA:
                    Message notify = new Message();
                    notify.obj = msg.obj;
                    notify.what = -1;
                    mNotify.sendMessage(notify);
                    break;
                default:
                    ;
            }
        }
    }
}
