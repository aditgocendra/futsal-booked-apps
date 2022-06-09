package com.ark.futsalbookedapps.Models;

public class ModelNotification {
    private String header;
    private String msg;
    private String keyNotification;

    public ModelNotification() {
    }

    public ModelNotification(String header, String msg) {
        this.header = header;
        this.msg = msg;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getKeyNotification() {
        return keyNotification;
    }

    public void setKeyNotification(String keyNotification) {
        this.keyNotification = keyNotification;
    }
}
