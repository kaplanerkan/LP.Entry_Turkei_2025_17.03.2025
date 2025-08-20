package com.eqpos.eqentry.models;

/**
 * Created by dursu on 09.03.2018.
 */

public class Product {
    private int id = 0;
    private int groupId = 0;
    private String groupName = "";
    private int rowNumber = 1;
    private String barcode = "";
    private String plu = "";
    private String origin = "";
    private String variantcode = "";
    private String productName = "";
    private Double costPrice = 0.0;
    private int taxId = 0;
    private String taxName = "";
    private double taxIn = 0.0;
    private double taxOut = 0.0;
    private int depositId = -1;
    private String depositeName = "";
    private int unitId = 0;
    private String uniteName = "st";
    private int isManuelPrice = 0;
    private Double Stock = 0.0;
    private Double inputStock = 0.0;
    private Double outputStock = 0.0;
    private Double criticalStock = 0.0;
    private Double inputReturn = 0.0;
    private Double productions = 0.0;
    private Double returns = 0.0;
    private Double wastages = 0.0;
    private Double consumption = 0.0;
    private int isStockActive = 1;
    private Double unitAmount = 0.0;
    private String amountUnit = "";
    private int taraId = -1;
    private String description = "";
    private String ingredients = "";
    private int active = 1;
    private int productype = 0;
    private String Suppliers = "";
    private int supplierId = -1;
    private Double price = 0.0;
    private Double newPrice = 0.0;
    private int changed = 0;
    private int printLabel = 0;
    private int priceOrder = 1;
    private String stockCode="";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getBarcode() { return barcode;  }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getPlu() {
        return plu;
    }

    public void setPlu(String plu) {
        this.plu = plu;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getVariantcode(){return variantcode;}

    public void setVariantcode(String variantcode){this.variantcode = variantcode;}

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(Double costPrice) {
        this.costPrice = costPrice;
    }

    public int getTaxId() {
        return taxId;
    }

    public void setTaxId(int taxId) {
        this.taxId = taxId;
    }

    public double getTaxIn() {
        return taxIn;
    }

    public void setTaxIn(double taxIn) {
        this.taxIn = taxIn;
    }

    public double getTaxOut() {
        return taxOut;
    }

    public void setTaxOut(double taxOut) {
        this.taxOut = taxOut;
    }


    public int getDepositId() {
        return depositId;
    }

    public void setDepositId(int depositId) {
        this.depositId = depositId;
    }

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    public int getIsManuelPrice() {
        return isManuelPrice;
    }

    public void setIsManuelPrice(int isManuelPrice) {
        this.isManuelPrice = isManuelPrice;
    }

    public Double getStock() {
        return Stock;
    }

    public void setStock(Double stock) {
        Stock = stock;
    }

    public Double getInputStock() {
        return inputStock;
    }

    public void setInputStock(Double inputStock) {
        this.inputStock = inputStock;
    }

    public Double getOutputStock() {
        return outputStock;
    }

    public void setOutputStock(Double outputStock) {
        this.outputStock = outputStock;
    }

    public Double getCriticalStock() {
        return criticalStock;
    }

    public void setCriticalStock(Double criticalStock) {
        this.criticalStock = criticalStock;
    }

    public Double getInputReturn() {
        return inputReturn;
    }

    public void setInputReturn(Double inputReturn) {
        this.inputReturn = inputReturn;
    }

    public Double getProductions() {
        return productions;
    }

    public void setProductions(Double productions) {
        this.productions = productions;
    }

    public Double getReturns() {
        return returns;
    }

    public void setReturns(Double returns) {
        this.returns = returns;
    }

    public Double getWastages() {
        return wastages;
    }

    public void setWastages(Double wastages) {
        this.wastages = wastages;
    }

    public Double getConsumption() {
        return consumption;
    }

    public void setConsumption(Double consumption) {
        this.consumption = consumption;
    }

    public int getIsStockActive() {
        return isStockActive;
    }

    public void setIsStockActive(int isStockActive) {
        this.isStockActive = isStockActive;
    }

    public Double getUnitAmount() {
        return unitAmount;
    }

    public void setUnitAmount(Double unitAmount) {
        this.unitAmount = unitAmount;
    }

    public String getAmountUnit() {
        return amountUnit;
    }

    public void setAmountUnit(String amountUnit) {
        this.amountUnit = amountUnit;
    }

    public int getTaraId() {
        return taraId;
    }

    public void setTaraId(int taraId) {
        this.taraId = taraId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public int getProductype() {
        return productype;
    }

    public void setProductype(int productype) {
        this.productype = productype;
    }

    public String getSuppliers() {
        return Suppliers;
    }

    public void setSuppliers(String suppliers) {
        Suppliers = suppliers;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public int getPrintLabel() {
        return printLabel;
    }

    public void setPrintLabel(int printLabel) {
        this.printLabel = printLabel;
    }

    public int getPriceOrder() {
        return priceOrder;
    }
    public void setPriceOrder(int priceOrder) {
        this.priceOrder = priceOrder;
    }


    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getTaxName() {
        return taxName;
    }

    public void setTaxName(String taxName) {
        this.taxName = taxName;
    }

    public String getDepositeName() {
        return depositeName;
    }

    public void setDepositeName(String depositeName) {
        this.depositeName = depositeName;
    }

    public String getUniteName() {
        return uniteName;
    }

    public void setUniteName(String uniteName) {
        this.uniteName = uniteName;
    }

    public int getChanged() {
        return changed;
    }

    public void setChanged(int changed) {
        this.changed = changed;
    }


    public Double getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(Double newPrice) {
        this.newPrice = newPrice;
    }


    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

}
