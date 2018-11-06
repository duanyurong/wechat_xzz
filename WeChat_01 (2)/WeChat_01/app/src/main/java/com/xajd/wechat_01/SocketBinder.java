package com.xajd.wechat_01;

import android.os.Binder;

import java.net.Socket;

public class SocketBinder extends Binder {
    private Socket skt;
    private MyDataBaseHelper dhHelper;

    public MyDataBaseHelper getDhHelper() {
        return dhHelper;
    }

    public void setDhHelper(MyDataBaseHelper dhHelper) {
        this.dhHelper = dhHelper;
    }
    public Socket getSkt() {
        return skt;
    }

    public void setSkt(Socket skt) {
        this.skt = skt;
    }
}
