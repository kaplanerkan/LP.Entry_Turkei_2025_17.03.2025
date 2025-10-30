package com.eqpos.eqentry.models;

public class SayimModel {

    private String productId;
    private String urunAdi;
    private String newQuantity;
    private String warehouseId;

    public SayimModel() {
    }

    public SayimModel(String productId, String urunAdi, String newQuantity, String warehouseId) {
        this.productId = productId;
        this.urunAdi = urunAdi;
        this.newQuantity = newQuantity;
        this.warehouseId = warehouseId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getUrunAdi() {
        return urunAdi;
    }

    public void setUrunAdi(String urunAdi) {
        this.urunAdi = urunAdi;
    }

    public String getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(String newQuantity) {
        this.newQuantity = newQuantity;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }
}
