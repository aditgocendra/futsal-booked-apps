package com.ark.futsalbookedapps.Models;

public class ModelReviewProvider {
    private double rating;
    private String comment;
    private String keyUser;
    private String keyBooked;
    private String keyReview;

    public ModelReviewProvider() {
    }

    public ModelReviewProvider(double rating, String comment, String keyUser, String keyBooked) {
        this.rating = rating;
        this.comment = comment;
        this.keyUser = keyUser;
        this.keyBooked = keyBooked;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getKeyUser() {
        return keyUser;
    }

    public void setKeyUser(String keyUser) {
        this.keyUser = keyUser;
    }

    public String getKeyBooked() {
        return keyBooked;
    }

    public void setKeyBooked(String keyBooked) {
        this.keyBooked = keyBooked;
    }

    public String getKeyReview() {
        return keyReview;
    }

    public void setKeyReview(String keyReview) {
        this.keyReview = keyReview;
    }
}
