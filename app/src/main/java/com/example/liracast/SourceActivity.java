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
import com.example.liracast.service.DeviceInfo;
import com.example.liracast.service.ILiracastListener;
import com.example.liracast.service.ILiracastSourceService;

import java.util.ArrayList;
import java.util.List;

public class SourceActivity extends AppCompatActivity {

    private class DeviceAdapter extends ArrayAdapter<DeviceInfo> {
        class ViewHolder {
            TextView name;
            TextView status;
        }

        public DeviceAdapter(Context context, int resource, List<DeviceInfo> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DeviceInfo device = getItem(position);
            View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_device, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.name = view.findViewById(R.id.device_name);
            viewHolder.status = view.findViewById(R.id.device_status);
            viewHolder.name.setText(device.getName());
            viewHolder.status.setText(device.getAddress());
            view.setTag(viewHolder);
            return view;
        }
    };

    private final String TAG = "SourceActivity";
    private final int MEDIAPROJECTION_REQUEST_CODE = 1234;
    private ILiracastSourceService mILiracastSourceService;
    private List<DeviceInfo> deviceList = new ArrayList<DeviceInfo>();
    private DeviceAdapter deviceAdapter;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mILiracastSourceService = ILiracastSourceService.Stub.asInterface(service);
            try {
                mILiracastSourceService.setListener(new ILiracastListener.Stub() {
                    @Override
                    public void onDeviceSearched(DeviceInfo deviceInfo) throws RemoteException {
                        if (!deviceList.contains(deviceInfo)) {
                            deviceList.add(deviceInfo);
                        }
                        deviceAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onDeviceConnected(DeviceInfo deviceInfo) throws RemoteException {

                    }

                    @Override
                    public void onDeviceDisconnected(DeviceInfo deviceInfo) throws RemoteException {

                    }

                    @Override
                    public void onStart(DeviceInfo deviceInfo) throws RemoteException {

                    }

                    @Override
                    public void onPause(DeviceInfo deviceInfo) throws RemoteException {

                    }

                    @Override
                    public void onResume(DeviceInfo deviceInfo) throws RemoteException {

                    }

                    @Override
                    public void onStop(DeviceInfo deviceInfo) throws RemoteException {

                    }
                });

                mILiracastSourceService.startSearch();
            } catch (RemoteException e) {
                Log.e(TAG, e.toString());
            }
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
                DeviceInfo device = deviceList.get(position);
                Toast.makeText(SourceActivity.this, "startMirror on " + device.getName(), Toast.LENGTH_SHORT).show();
                try {
                    mILiracastSourceService.startMirror(device);
                } catch (RemoteException e) {
                    Log.e(TAG, e.toString());
                }
            }
        });

        ResourceManager.getInstance().getAsynchronousManager().postRunnabe(new Runnable() {
            @Override
            public void run() {
                getMediaProjection();
            }
        });
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