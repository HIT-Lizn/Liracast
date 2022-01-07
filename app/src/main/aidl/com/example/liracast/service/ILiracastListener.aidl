// ILiracastListener.aidl
package com.example.liracast.service;
import com.example.liracast.service.DeviceInfo;
// Declare any non-default types here with import statements

interface ILiracastListener {
    void onDeviceSearched(in DeviceInfo deviceInfo);
    void onDeviceConnected(in DeviceInfo deviceInfo);
    void onDeviceDisconnected(in DeviceInfo deviceInfo);
    void onStart(in DeviceInfo deviceInfo);
    void onPause(in DeviceInfo deviceInfo);
    void onResume(in DeviceInfo deviceInfo);
    void onStop(in DeviceInfo deviceInfo);
}