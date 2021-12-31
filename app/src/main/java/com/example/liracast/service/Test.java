package com.example.liracast.service;

import android.os.Parcel;
import android.os.Parcelable;

public class Test implements Parcelable{
    private int t;

    protected Test(Parcel in) {
        t = in.readInt();
    }

    public static final Creator<Test> CREATOR = new Creator<Test>() {
        @Override
        public Test createFromParcel(Parcel in) {
            return new Test(in);
        }

        @Override
        public Test[] newArray(int size) {
            return new Test[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(t);
    }
}
