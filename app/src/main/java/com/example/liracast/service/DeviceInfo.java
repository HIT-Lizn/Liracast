package com.example.liracast.service;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class DeviceInfo implements Parcelable {
    private String name;
    private String address;
    private int port;

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public DeviceInfo() {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceInfo that = (DeviceInfo) o;
        return port == that.port &&
                Objects.equals(name, that.name) &&
                Objects.equals(address, that.address);
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address, port);
    }

    protected DeviceInfo(Parcel in) {
        name = in.readString();
        address = in.readString();
        port = in.readInt();
    }

    public static final Creator<DeviceInfo> CREATOR = new Creator<DeviceInfo>() {
        @Override
        public DeviceInfo createFromParcel(Parcel in) {
            return new DeviceInfo(in);
        }

        @Override
        public DeviceInfo[] newArray(int size) {
            return new DeviceInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeInt(port);
    }
}
