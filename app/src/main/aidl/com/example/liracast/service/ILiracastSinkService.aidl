package com.example.liracast.service;
import com.example.liracast.service.Test;
interface ILiracastSinkService {
    void start(in Test t);
    void stop();
}