package com.example.liracast.service;
import com.example.liracast.service.DeviceInfo;
import com.example.liracast.service.ILiracastListener;

interface ILiracastSourceService {
    void setListener(in ILiracastListener listener);
    void startSearch();
    void stopSearch();
    void startMirror(in DeviceInfo deviceInfo);
    void stopMirror(in DeviceInfo deviceInfo);
    void pause(in DeviceInfo deviceInfo);
    void resume(in DeviceInfo deviceInfo);
}