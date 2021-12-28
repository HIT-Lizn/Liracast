package com.example.liracast.net.p2p;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

public class P2pAdapter {
    private String TAG = "P2PAdapter";
    private Context mContext;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pBroadcastRecevier mWifiP2pBroadcastRecevier;
    private WifiP2pManager.Channel mChannel;

    private ArrayList<P2pListener> mP2pListeners = new ArrayList<P2pListener>();

    public P2pAdapter(Context context) {
        Log.d(TAG, "P2PAdapter: init");
        mContext = context;
        mWifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(mContext,
                Looper.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.d(TAG, "onChannelDisconnected: ");
            }
        });
        mWifiP2pBroadcastRecevier = new WifiP2pBroadcastRecevier(mContext, this);
        mWifiP2pBroadcastRecevier.setBroadCastCallBack(new WifiP2pBroadcastRecevier.BroadCastCallBack() {
            @Override
            public void callBack(Context context, Intent intent) {
                Log.d(TAG, "callBack: ");
                /*switch (action) {
                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION: {
                    int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -100);
                    break;
                }
                case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION: {
                    mP2pAdapter.requstPeers();
                    break;
                }
                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION: {
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                    if (networkInfo != null) {
                        Log.d(TAG, "onReceive: " + networkInfo.toString());
                        if (networkInfo.isConnected()) {
                            mP2pAdapter.getConnectionInfo();
                        }
                    }
                    break;
                }
                case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION: {
                    WifiP2pDevice wifiP2pDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                    if (wifiP2pDevice != null) {
                        Log.d(TAG, "onReceive: " + wifiP2pDevice.toString());
                    }
                    break;
                }
            }*/



            }
        });
    }

    public void registerListener(P2pListener p2pListener) {
        Log.d(TAG, "registerSearchListener: ");
        synchronized (mP2pListeners) {
            mP2pListeners.add(p2pListener);
        }
    }

    public void unregisterListener(P2pListener p2pSearchListener) {
        Log.d(TAG, "unregisterSearchListener: ");
        synchronized (mP2pListeners) {
            mP2pListeners.remove(p2pSearchListener);
        }
    }

    public ArrayList<P2pListener> getSearchListener() {
        Log.d(TAG, "getSearchListener: ");
        synchronized (mP2pListeners) {
            return mP2pListeners;
        }
    }

    @SuppressLint("MissingPermission")
    public void startSearch() {
        mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: discover");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "onFailure: discover");
            }
        });
    }

    public void stopSearch() {
        mWifiP2pManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: stop discover");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "onFailure: stop discover");
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void requstPeers() {
        mWifiP2pManager.requestPeers(mChannel, new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                Collection<WifiP2pDevice> list = peers.getDeviceList();
                for (WifiP2pDevice tmp : list) {
                    Log.d(TAG, "onPeersAvailable:" + tmp.toString());
                    synchronized (mP2pListeners) {
                        int count = mP2pListeners.size();
                        for (int i = 0; i < count; i++) {
                            mP2pListeners.get(i).onDeviceSearched(tmp);
                        }
                    }
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void connect(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        mWifiP2pManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: connect");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "onFailure: connect " + reason);
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void createGroup() {
        mWifiP2pManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: create group");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "onFailure: create group" + reason);
            }
        });
    }

    public void removeGroup() {
        mWifiP2pManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: remove group");
                synchronized (mP2pListeners) {
                    int count = mP2pListeners.size();
                    for (int i = 0; i < count; i++) {
                        mP2pListeners.get(i).onDeviceDisconnect();
                    }
                }
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "onFailure: remove group" + reason);
            }
        });
    }

    public void disconnect() {
        mWifiP2pManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: cancel connect");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "onFailure: cancel connect " + reason);
            }
        });
    }

    public void getConnectionInfo() {
        mWifiP2pManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                Log.d(TAG, "onConnectionInfoAvailable: " + info.toString());
                synchronized (mP2pListeners) {
                    int count = mP2pListeners.size();
                    for (int i = 0; i < count; i++) {
                        mP2pListeners.get(i).onDeviceConnect(info);
                    }
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void getGroupInfo() {
        mWifiP2pManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                Log.d(TAG, "getGroupInfo: " + wifiP2pGroup.toString());
                synchronized (mP2pListeners) {
                    int count = mP2pListeners.size();
                    for (int i = 0; i < count; i++) {
                        mP2pListeners.get(i).onGroupFinded(wifiP2pGroup);
                    }
                }
            }
        });
    }
}
