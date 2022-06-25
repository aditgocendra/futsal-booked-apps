package com.ark.futsalbookedapps.Models;

public class ModelProviderField {
    private String name;
    private String urlPhotoField;
    private String numberPhone;
    private String nameBank;
    private String bankAccountNumber;
    private String location;
    private double latitude;
    private double longitude;
    private double rating;
    private String openTime;
    private String closeTime;
    private String description;
    private int priceField;
    private String keyUserProviderField;

    public ModelProviderField() {
    }

    public ModelProviderField(String name, String urlPhotoField, String numberPhone, String nameBank, String bankAccountNumber, String location, double latitude, double longitude, double rating, String openTime, String closeTime, String description, int priceField) {
        this.name = name;
        this.urlPhotoField = urlPhotoField;
        this.numberPhone = numberPhone;
        this.nameBank = nameBank;
        this.bankAccountNumber = bankAccountNumber;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.description = description;
        this.priceField = priceField;
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

    public String getNumberPhone() {
        return numberPhone;
    }

    public void setNumberPhone(String numberPhone) {
        this.numberPhone = numberPhone;
    }

    public String getNameBank() {
        return nameBank;
    }

    public void setNameBank(String nameBank) {
        this.nameBank = nameBank;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(String closeTime) {
        this.closeTime = closeTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPriceField() {
        return priceField;
    }

    public void setPriceField(int priceField) {
        this.priceField = priceField;
    }

    public String getKeyUserProviderField() {
        return keyUserProviderField;
    }

    public void setKeyUserProviderField(String keyUserProviderField) {
        this.keyUserProviderField = keyUserProviderField;
    }
}
