package com.eqpos.eqentry.models;

public class StockTransfer {
    private long id=0;
    private String number="";
    private int targetWarehouseId=0;
    private String targetWarehouseName="";

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getTargetWarehouseId() {
        return targetWarehouseId;
    }

    public void setTargetWarehouseId(int targetWarehouseId) {
        this.targetWarehouseId = targetWarehouseId;
    }

    public String getTargetWarehouseName() {
        return targetWarehouseName;
    }

    public void setTargetWarehouseName(String targetWarehouseName) {
        this.targetWarehouseName = targetWarehouseName;
    }
}
