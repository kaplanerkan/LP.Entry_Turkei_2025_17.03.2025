package com.eqpos.eqentry.models;
public class DepoModel {
    private int id;
    private String ismi; // veya depoAdi

    public DepoModel(int id, String ismi) {
        this.id = id;
        this.ismi = ismi;
    }

    public int getId() {
        return id;
    }

    public String getIsmi() { // veya getDepoAdi()
        return ismi;
    }
}