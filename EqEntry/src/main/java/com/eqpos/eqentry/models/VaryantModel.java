package com.eqpos.eqentry.models;

public class VaryantModel {

    private int id;
    private int sira;
    private String tanim;
    private int rowcell;
    private String aciklama;
    private int parentid;



    public VaryantModel() {
    }

    public VaryantModel(int id, int sira, String tanim, int rowcell, String aciklama, int parentid) {
        this.id = id;
        this.sira = sira;
        this.tanim = tanim;
        this.rowcell = rowcell;
        this.aciklama = aciklama;
        this.parentid = parentid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSira() {
        return sira;
    }

    public void setSira(int sira) {
        this.sira = sira;
    }

    public String getTanim() {
        return tanim;
    }

    public void setTanim(String tanim) {
        this.tanim = tanim;
    }

    public int getRowcell() {
        return rowcell;
    }

    public void setRowcell(int rowcell) {
        this.rowcell = rowcell;
    }

    public String getAciklama() {
        return aciklama;
    }

    public void setAciklama(String aciklama) {
        this.aciklama = aciklama;
    }

    public int getParentid() {
        return parentid;
    }

    public void setParentid(int parentid) {
        this.parentid = parentid;
    }
}
