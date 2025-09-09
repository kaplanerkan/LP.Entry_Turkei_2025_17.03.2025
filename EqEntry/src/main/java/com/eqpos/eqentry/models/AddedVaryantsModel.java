package com.eqpos.eqentry.models;

public class AddedVaryantsModel {

    private int id;
    private String barcode;
    private int plu;
    private String urunadi;
    private double price;
    private int anagrupid;
    private int altgrupid;

    public AddedVaryantsModel(int id, String barcode, int plu, String urunadi, double price, int anagrupid, int altgrupid) {
        this.id = id;
        this.barcode = barcode;
        this.plu = plu;
        this.urunadi = urunadi;
        this.price = price;
        this.anagrupid = anagrupid;
        this.altgrupid = altgrupid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public int getPlu() {
        return plu;
    }

    public void setPlu(int plu) {
        this.plu = plu;
    }

    public String getUrunadi() {
        return urunadi;
    }

    public void setUrunadi(String urunadi) {
        this.urunadi = urunadi;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getAnagrupid() {
        return anagrupid;
    }

    public void setAnagrupid(int anagrupid) {
        this.anagrupid = anagrupid;
    }

    public int getAltgrupid() {
        return altgrupid;
    }

    public void setAltgrupid(int altgrupid) {
        this.altgrupid = altgrupid;
    }
}
