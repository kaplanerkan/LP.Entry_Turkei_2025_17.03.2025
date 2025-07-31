package com.eqpos.eqentry.Models;

public class UrunBarcode {

    private int urunid;
    private String barcod;
    private String miktar;
    private int birimid;

    public UrunBarcode() {

    }

    public UrunBarcode(int urunid, String barcod, String miktar, int birimid) {
        this.urunid = urunid;
        this.barcod = barcod;
        this.miktar = miktar;
        this.birimid = birimid;
    }

    public int getUrunid() {
        return urunid;
    }

    public void setUrunid(int urunid) {
        this.urunid = urunid;
    }

    public String getBarcod() {
        return barcod;
    }

    public void setBarcod(String barcod) {
        this.barcod = barcod;
    }

    public String getMiktar() {
        return miktar;
    }

    public void setMiktar(String miktar) {
        this.miktar = miktar;
    }

    public int getBirimid() {
        return birimid;
    }

    public void setBirimid(int birimid) {
        this.birimid = birimid;
    }
}
