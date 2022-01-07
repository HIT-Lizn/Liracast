package com.example.liracast.service;
import com.example.liracast.service.DeviceInfo;
interface ILiracastSinkService {
    void start(in DeviceInfo deviceInfo);
    void stop(in DeviceInfo deviceInfo);
}