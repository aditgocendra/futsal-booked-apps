package com.ark.futsalbookedapps.Models;

public class ModelAccount {
    private String username;
    private String email;
    private int role;
    private String number_phone;
    private String keyAccount;

    public ModelAccount() {
    }

    public ModelAccount(String username, String email, int role, String number_phone) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.number_phone = number_phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getNumber_phone() {
        return number_phone;
    }

    public void setNumber_phone(String number_phone) {
        this.number_phone = number_phone;
    }

    public String getKeyAccount() {
        return keyAccount;
    }

    public void setKeyAccount(String keyAccount) {
        this.keyAccount = keyAccount;
    }
}
