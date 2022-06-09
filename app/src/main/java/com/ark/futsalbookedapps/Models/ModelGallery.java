package com.ark.futsalbookedapps.Models;

public class ModelGallery {
    private String imageGalleryUrl;
    private String keyGallery;

    public ModelGallery() {
    }

    public ModelGallery(String imageGalleryUrl) {
        this.imageGalleryUrl = imageGalleryUrl;
    }

    public String getImageGalleryUrl() {
        return imageGalleryUrl;
    }

    public void setImageGalleryUrl(String imageGalleryUrl) {
        this.imageGalleryUrl = imageGalleryUrl;
    }

    public String getKeyGallery() {
        return keyGallery;
    }

    public void setKeyGallery(String keyGallery) {
        this.keyGallery = keyGallery;
    }
}
