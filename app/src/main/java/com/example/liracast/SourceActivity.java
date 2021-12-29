package com.example.liracast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pWfdInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.liracast.global.Config;
import com.example.liracast.global.ResourceManager;
import com.example.liracast.manager.AsynchronousManager;
import com.example.liracast.net.p2p.P2pListener;
import com.example.liracast.service.ILiracastSourceService;

import java.util.ArrayList;
import java.util.List;

public class SourceActivity extends AppCompatActivity {

    private class Device {
        public String name;
        public String address;

        public Device(String n, String a) {
            name = n;
            address = a;
        }
    }

    private class DeviceAdapter extends ArrayAdapter<Device> {
        class ViewHolder {
            TextView name;
            TextView status;
        }

        public DeviceAdapter(Context context, int resource, List<Device> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Device device = getItem(position);
            View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_device, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.name = view.findViewById(R.id.device_name);
            viewHolder.status = view.findViewById(R.id.device_status);
            viewHolder.name.setText(device.name);
            viewHolder.status.setText(device.address);
            view.setTag(viewHolder);
            return view;
        }
    };

    private final String TAG = "SourceActivity";
    private final int MEDIAPROJECTION_REQUEST_CODE = 1234;
    private ILiracastSourceService mILiracastSourceService;
    private List<Device> deviceList = new ArrayList<Device>();
    private DeviceAdapter deviceAdapter;

    private P2pListener mP2pListener = new P2pListener() {
        @Override
        public void onDeviceSearched(WifiP2pDevice wifiP2pDevice) {
            if (!isWFD(wifiP2pDevice.getWfdInfo().getDeviceType())) {
                Log.e(TAG, "not wfd device");
                return;
            }
            for (Device d: deviceList) {
                if (d.address.equals(wifiP2pDevice.deviceAddress)) {
                    return;
                }
            }
            deviceList.add(new Device(wifiP2pDevice.deviceName, wifiP2pDevice.deviceAddress));
            deviceAdapter.notifyDataSetChanged();
        }

        @Override
        public void onDeviceConnect(WifiP2pInfo wifiP2pInfo) {

        }

        @Override
        public void onDeviceDisconnect() {

        }

        @Override
        public void onGroupFinded(WifiP2pGroup wifiP2pGroup) {

        }

        private boolean isWFD(int type) {
            if (type == WifiP2pWfdInfo.DEVICE_TYPE_PRIMARY_SINK ||
                    type == WifiP2pWfdInfo.DEVICE_TYPE_SECONDARY_SINK ||
                    type == WifiP2pWfdInfo.DEVICE_TYPE_SOURCE_OR_PRIMARY_SINK) {
                return true;
            }
            return false;
        }
    };

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mILiracastSourceService = ILiracastSourceService.Stub.asInterface(service);
            /*ResourceManager.getInstance().getAsynchronousManager().postRunnabe(new Runnable() {
                @Override
                public void run() {
                    try {
                        mILiracastSourceService.startMirror();
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
            });*/
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_source);
        deviceAdapter = new DeviceAdapter(this, R.layout.layout_device, deviceList);
        ListView listView = findViewById(R.id.source_listview);
        listView.setAdapter(deviceAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Device device = deviceList.get(position);
                Toast.makeText(SourceActivity.this, "startMirror on " + device.name, Toast.LENGTH_SHORT).show();
            }
        });

        ResourceManager.getInstance().getP2pAdapter().registerListener(mP2pListener);
        ResourceManager.getInstance().getThreadPoolManager().postRunnable(new Runnable() {
            @Override
            public void run() {
                ResourceManager.getInstance().getP2pAdapter().startSearch();
            }
        });

        /*ResourceManager.getInstance().getAsynchronousManager().postRunnabe(new Runnable() {
            @Override
            public void run() {
                getMediaProjection();
            }
        });*/
    }

    private void getMediaProjection() {
        Log.d(TAG, "getMediaProjectionPrivacy");
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mediaProjectionManager == null) {
            Log.e(TAG, "Can't get media projection manager");
            return;
        }
        Intent screenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(screenCaptureIntent, MEDIAPROJECTION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
        if (requestCode == MEDIAPROJECTION_REQUEST_CODE && resultCode == RESULT_OK) {
            ResourceManager.getInstance().getAsynchronousManager().postRunnabe(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent serviceIntent = new Intent();
                        serviceIntent.setAction(Config.SOURCE_SERVICE_ACTION);
                        serviceIntent.setPackage(getPackageName());
                        serviceIntent.putExtra(Config.SOURCE_INTENT_DATA, data);
                        serviceIntent.putExtra(Config.SOURCE_INTENT_RESULTCODE, resultCode);
                        bindService(serviceIntent, conn, Service.BIND_AUTO_CREATE);
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
            });
        }
    }
}