package com.ark.futsalbookedapps.Models;

public class ModelBooked {
    private String keyUserBooked;
    private String keyProviderField;
    private String keyFieldBooked;
    private String playtime;
    private String dateBooked;
    private String timeBooked;
    private int statusDP;
    private String keyBooked;

    public ModelBooked(String keyUserBooked, String keyProviderField, String keyFieldBooked, String playtime, String dateBooked, String timeBooked, int statusDP) {
        this.keyUserBooked = keyUserBooked;
        this.keyProviderField = keyProviderField;
        this.keyFieldBooked = keyFieldBooked;
        this.playtime = playtime;
        this.dateBooked = dateBooked;
        this.timeBooked = timeBooked;
        this.statusDP = statusDP;
    }

    public String getKeyUserBooked() {
        return keyUserBooked;
    }

    public void setKeyUserBooked(String keyUserBooked) {
        this.keyUserBooked = keyUserBooked;
    }

    public String getKeyProviderField() {
        return keyProviderField;
    }

    public void setKeyProviderField(String keyProviderField) {
        this.keyProviderField = keyProviderField;
    }

    public String getKeyFieldBooked() {
        return keyFieldBooked;
    }

    public void setKeyFieldBooked(String keyFieldBooked) {
        this.keyFieldBooked = keyFieldBooked;
    }

    public String getPlaytime() {
        return playtime;
    }

    public void setPlaytime(String playtime) {
        this.playtime = playtime;
    }

    public String getDateBooked() {
        return dateBooked;
    }

    public void setDateBooked(String dateBooked) {
        this.dateBooked = dateBooked;
    }

    public String getTimeBooked() {
        return timeBooked;
    }

    public void setTimeBooked(String timeBooked) {
        this.timeBooked = timeBooked;
    }

    public int getStatusDP() {
        return statusDP;
    }

    public void setStatusDP(int statusDP) {
        this.statusDP = statusDP;
    }

    public String getKeyBooked() {
        return keyBooked;
    }

    public void setKeyBooked(String keyBooked) {
        this.keyBooked = keyBooked;
    }
}
