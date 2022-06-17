package com.ark.futsalbookedapps.Models;

public class ModelFacility {
    private String nameFacility;
    private String keyFacility;

    public ModelFacility() {
    }

    public ModelFacility(String nameFacility) {
        this.nameFacility = nameFacility;
    }

    public String getNameFacility() {
        return nameFacility;
    }

    public void setNameFacility(String nameFacility) {
        this.nameFacility = nameFacility;
    }

    public String getKeyFacility() {
        return keyFacility;
    }

    public void setKeyFacility(String keyFacility) {
        this.keyFacility = keyFacility;
    }
}
