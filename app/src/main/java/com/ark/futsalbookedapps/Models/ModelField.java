package com.ark.futsalbookedapps.Models;

public class ModelField {
    private String typeField;
    private String urlField;
    private String minDP;
    private String keyField;

    public ModelField() {
    }

    public ModelField(String typeField, String urlField, String minDP) {
        this.typeField = typeField;
        this.urlField = urlField;
        this.minDP = minDP;
    }

    public String getTypeField() {
        return typeField;
    }

    public void setTypeField(String typeField) {
        this.typeField = typeField;
    }

    public String getUrlField() {
        return urlField;
    }

    public void setUrlField(String urlField) {
        this.urlField = urlField;
    }

    public String getMinDP() {
        return minDP;
    }

    public void setMinDP(String minDP) {
        this.minDP = minDP;
    }

    public String getKeyField() {
        return keyField;
    }

    public void setKeyField(String keyField) {
        this.keyField = keyField;
    }
}
