package com.ark.futsalbookedapps.Models;

public class ModelProviderField {
    private String name;
    private String urlPhotoField;
    private String numberPhone;
    private String bankAccountNumber;
    private String location;
    private double latitude;
    private double longitude;
    private double rating;
    private String keyUserProviderField;

    public ModelProviderField() {
    }

    public ModelProviderField(String name, String urlPhotoField, String numberPhone, String bankAccountNumber, String location, double latitude, double longitude, double rating) {
        this.name = name;
        this.urlPhotoField = urlPhotoField;
        this.numberPhone = numberPhone;
        this.bankAccountNumber = bankAccountNumber;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrlPhotoField() {
        return urlPhotoField;
    }

    public void setUrlPhotoField(String urlPhotoField) {
        this.urlPhotoField = urlPhotoField;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNumberPhone() {
        return numberPhone;
    }

    public void setNumberPhone(String numberPhone) {
        this.numberPhone = numberPhone;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getKeyUserProviderField() {
        return keyUserProviderField;
    }

    public void setKeyUserProviderField(String keyUserProviderField) {
        this.keyUserProviderField = keyUserProviderField;
    }
}
