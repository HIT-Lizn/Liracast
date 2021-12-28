package com.example.liracast.net.p2p;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;

public interface P2pListener {
    void onDeviceSearched(WifiP2pDevice wifiP2pDevice);
    void onDeviceConnect(WifiP2pInfo wifiP2pInfo);
    void onDeviceDisconnect();
    void onGroupFinded(WifiP2pGroup wifiP2pGroup);
}
