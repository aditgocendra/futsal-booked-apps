package com.ark.futsalbookedapps.Models;

public class ModelBooked {
    private String keyUserBooked;
    private String keyProviderField;
    private String playtime;
    private String dateBooked;
    private String timeBooked;
    private String urlProof;
    private int status;
    private String keyBooked;

    public ModelBooked() {
    }

    public ModelBooked(String keyUserBooked, String keyProviderField, String playtime, String dateBooked, String timeBooked, String urlProof, int status) {
        this.keyUserBooked = keyUserBooked;
        this.keyProviderField = keyProviderField;
        this.playtime = playtime;
        this.dateBooked = dateBooked;
        this.timeBooked = timeBooked;
        this.urlProof = urlProof;
        this.status = status;
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

    public String getUrlProof() {
        return urlProof;
    }

    public void setUrlProof(String urlProof) {
        this.urlProof = urlProof;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getKeyBooked() {
        return keyBooked;
    }

    public void setKeyBooked(String keyBooked) {
        this.keyBooked = keyBooked;
    }
}
