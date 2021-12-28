package com.example.liracast.net.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

public class WifiP2pBroadcastRecevier extends BroadcastReceiver {
    public static interface BroadCastCallBack {
        void callBack(Context context, Intent intent);
    }

    private String TAG = "WifiP2pBroadcastRecevier";
    private Context mContext;
    private BroadCastCallBack mBroadCastCallBack;

    public WifiP2pBroadcastRecevier(Context context, P2pAdapter p2PAdapter) {
        Log.d(TAG, "WifiP2PBroadcastRecevier: init");
        mContext = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mContext.registerReceiver(this, intentFilter);
    }

    public void setBroadCastCallBack(BroadCastCallBack broadCastCallBack) {
        synchronized (mBroadCastCallBack) {
            mBroadCastCallBack = broadCastCallBack;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            Log.d(TAG, "onReceive: " + action.toString());
            synchronized (mBroadCastCallBack) {
                mBroadCastCallBack.callBack(context, intent);
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.d(TAG, "finalize: uninit");
        mContext.unregisterReceiver(this);
    }
}
