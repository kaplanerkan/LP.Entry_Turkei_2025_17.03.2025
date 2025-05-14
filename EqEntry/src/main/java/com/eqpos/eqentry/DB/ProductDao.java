package com.eqpos.eqentry.DB;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.eqpos.eqentry.Models.Product;
import com.eqpos.eqentry.R;
import com.eqpos.eqentry.tools.Variables;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dursu on 24.02.2018.
 */

public class ProductDao {
    private static Database Db;

    public static void deleteAll() {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getWritableDatabase();
        db.delete("unites", "", null);
        db.delete("suppliers", "", null);
        db.delete("taxes", "", null);
        db.delete("productgroups", "", null);
        db.delete("deposites", "", null);
        db.delete("products", "", null);
        db.delete("productprices", "", null);
        db.delete("collects", "", null);
        db.delete("invoice", "", null);
        db.delete("customers", "", null);
        db.delete("warehouses", "", null);

    }





    /* bu fonksiyon ürünün veritabanına kaydedilmesi için kullanılacak */
    public static int saveProduct(int id, int groupid, int rownumber, String stockCode, String barcode, String plu, String variantcode,
                                  String productname, double costprice, int taxid, int depositeid, int uniteid,
                                  int ismanualprice, double stock, int isstockactive, double unitamount,
                                  String amountunite, int taraid, String description, String ingredients,
                                  int active, double inputstock, double outputstock, int producttype,
                                  double criticalstock, double inputreturn, double production, double returns,
                                  double wastages, double consumption, String suppliers, int suppliersid,
                                  String origin, int changed, boolean isSync) {
        if (Db == null) {
            Db = new Database();
        }

        int productId = id;

        ContentValues value = new ContentValues();
        if (id > 0) {
            value.put("id", productId);
        } else {
            value.put("isnew", 1);
        }
        value.put("groupid", groupid);
        value.put("rownumber", rownumber);
        value.put("barcode", barcode.trim());
        value.put("plu", plu);
        value.put("variantcode", variantcode);
        value.put("productname", productname);
        value.put("costprice", costprice);
        value.put("taxid", taxid);
        value.put("depositeid", depositeid);
        value.put("uniteid", uniteid);
        value.put("ismanualprice", ismanualprice);
        value.put("stock", stock);
        value.put("isstockactive", isstockactive);
        value.put("unitamount", unitamount);
        value.put("amountunite", amountunite);
        value.put("taraid", taraid);
        value.put("description", description);
        value.put("ingredients", ingredients);
        value.put("active", active);
        value.put("inputstock", inputstock);
        value.put("outputstock", outputstock);
        value.put("producttype", producttype);
        value.put("criticalstock", criticalstock);
        value.put("inputreturn", inputreturn);
        value.put("production", production);
        value.put("returns", returns);
        value.put("wastages", wastages);
        value.put("consumption", consumption);
        value.put("suppliers", suppliers);
        value.put("suppliersid", suppliersid);
        value.put("origin", origin);
        value.put("changed", changed);
        value.put("stockcode", stockCode);

        SQLiteDatabase db = Db.getWritableDatabase();

        try {
            if (id > 0 && !isSync) {
                db.update("products", value, "id=?", new String[]{String.valueOf(id)});
            } else {

                db.insert("products", null, value);
                Cursor cursor = db.rawQuery("select last_insert_rowid()", null);
                cursor.moveToNext();
                productId = cursor.getInt(0);
                cursor.close();
            }
        } catch (SQLException e) {
            Log.e("Product Save Error 1", e.toString());
            return 0;
        }

        return productId;
    }



    /* Bu method serverdan gelen verileri kaydetmek için kullanılacaktır */
    public static void saveProduct(JsonArray value) {
        SQLiteDatabase db = getWritableDatabase();
        try {

            db.beginTransaction();

            for (int i = 0; i < value.size(); i++) {
                JsonObject jData = value.get(i).getAsJsonObject();
                String lStockCode = "";
                if (jData.has("stockcode")) {
                    lStockCode = jData.get("stockcode").getAsString();
                }
                int activmi = jData.get("active").getAsInt() ;
                if (activmi == 0) {
                    continue;
                }
                saveProduct(jData.get("id").getAsInt(), jData.get("grupid").getAsInt(),
                        jData.get("urunsira").getAsInt(), lStockCode, jData.get("barkod").getAsString(),
                        jData.get("urunkodu").getAsString(), jData.get("variantcode").getAsString(),
                        jData.get("urunadi").getAsString(),
                        jData.get("fiyat").getAsDouble(), jData.get("urunturid").getAsInt(),
                        jData.get("depozitoid").getAsInt(), jData.get("birimid").getAsInt(),
                        jData.get("fiyatsor").getAsInt(), jData.get("stok").getAsDouble(),
                        jData.get("stoktut").getAsInt(), jData.get("birimmiktari").getAsDouble(),
                        jData.get("miktarbirimi").getAsString(), jData.get("daraid").getAsInt(),
                        jData.get("description").getAsString(), jData.get("icindekiler").getAsString(),
                        jData.get("active").getAsInt(), jData.get("girdi").getAsDouble(),
                        jData.get("cikti").getAsDouble(), jData.get("uruntip").getAsInt(),
                        jData.get("kritikstok").getAsDouble(), jData.get("alisiadeler").getAsDouble(),
                        jData.get("uretim").getAsDouble(), jData.get("iadeler").getAsDouble(),
                        jData.get("fireler").getAsDouble(), jData.get("sarf").getAsDouble(),
                        jData.get("tedarikci").getAsString(), jData.get("tedarikciid").getAsInt(),
                        jData.get("uretimyeri").getAsString(),0, true);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("Product Save Error 2", e.toString());
        }finally {
            db.endTransaction();
        }
    }

    /* Bu method Ürün bilgilerinin değişimi veya yeni ürün oluşturmak için kullanılacak */
    public static void saveProduct(Product value) {
        try {
            if (value != null) {
                int lId = saveProduct(value.getId(), value.getGroupId(), value.getRowNumber(),
                        value.getStockCode(), value.getBarcode(), value.getPlu(), value.getVariantcode(),
                        value.getProductName(), value.getCostPrice(), value.getTaxId(), value.getDepositId(),
                        value.getUnitId(), value.getIsManuelPrice(), value.getStock(),
                        value.getIsStockActive(), value.getUnitAmount(), value.getAmountUnit(),
                        value.getTaraId(), value.getDescription(), value.getIngredients(),
                        value.getActive(), value.getInputStock(), value.getOutputStock(),
                        value.getProductype(), value.getCriticalStock(), value.getInputReturn(),
                        value.getProductions(), value.getReturns(), value.getWastages(),
                        value.getConsumption(), value.getSuppliers(), value.getSupplierId(),
                        value.getOrigin(), value.getChanged(), false);
                if (lId > 0) {
                    value.setId(lId);
                    changePrice(lId, 1, value.getPrice());
                }
            }
        } catch (Exception e) {
            Log.e("Product Save Error 2", e.toString());
        }
    }

    public static void updateProduct(int id, String groupname, String barcode, String plu, String origin,
                                     String productname, String taxname, String unitname, String depositname) {
        if (Db == null) {
            Db = new Database();
        }

        int lGroupId = getGroupId(groupname);
        int lTaxid = getTaxId(taxname);
        int lUnitId = getUnitId(unitname);
        int lDepositId = getDepositId(depositname);

        ContentValues value = new ContentValues();
        value.put("groupid", lGroupId);
        value.put("barcode", barcode);
        value.put("plu", plu);
        value.put("origin",origin);
        value.put("productname", productname);
        value.put("taxid", lTaxid);
        value.put("depositeid", lDepositId);
        value.put("changed", 1);
        //value.put("printlabel", 1);

        SQLiteDatabase db = Db.getWritableDatabase();

        try {
            db.update("products", value, "id=?", new String[]{String.valueOf(id)});

        } catch (SQLException e) {
            Log.e("Product Save Error 1", e.toString());
        }
    }

//    public static void setProductAsNew(int id) {
//        if (Db == null) {
//            Db = new Database();
//        }
//
//        ContentValues value = new ContentValues();
//        value.put("isnew", 1);
//
//        SQLiteDatabase db = Db.getWritableDatabase();
//
//        try {
//            db.update("products", value, "id=?", new String[]{String.valueOf(id)});
//        } catch (SQLException e) {
//            Log.e("Product Save Error 1", e.toString());
//        }
//    }

    public static void saveUnite(int id, String unitename) {
        if (Db == null) {
            Db = new Database();
        }

        ContentValues value = new ContentValues();
        value.put("id", id);
        value.put("unitename", unitename);

        SQLiteDatabase db = Db.getWritableDatabase();

        try {
            db.insert("unites", null, value);
        } catch (SQLException e) {
            Log.e("Unite Save Error 1", e.toString());
        }
    }

    public static void saveUnite(JsonArray value) {
        try {
            for (int i = 0; i < value.size(); i++) {
                JsonObject jData = value.get(i).getAsJsonObject();

                saveUnite(jData.get("id").getAsInt(), jData.get("birimadi").getAsString());
            }
        } catch (Exception e) {
            Log.e("Unite Save Error 2", e.toString());
        }
    }

    public static void saveTax(int id, String taxname, double taxin, double taxout) {
        if (Db == null) {
            Db = new Database();
        }

        ContentValues value = new ContentValues();
        value.put("id", id);
        value.put("taxname", taxname);
        value.put("taxin", taxin);
        value.put("taxout", taxout);

        SQLiteDatabase db = Db.getWritableDatabase();

        try {
            db.insert("taxes", null, value);
        } catch (SQLException e) {
            Log.e("Tax Save Error 1", e.toString());
        }
    }

    public static void saveTax(JsonArray value) {
        try {
            for (int i = 0; i < value.size(); i++) {
                JsonObject jData = value.get(i).getAsJsonObject();

                saveTax(jData.get("id").getAsInt(), jData.get("turadi").getAsString(),
                        jData.get("icvergi").getAsDouble(), jData.get("disvergi").getAsDouble());
            }
        } catch (Exception e) {
            Log.e("Tax Save Error 2", e.toString());
        }
    }

    public static void saveGroup(int id, String groupname) {
        if (Db == null) {
            Db = new Database();
        }

        ContentValues value = new ContentValues();
        value.put("id", id);
        value.put("groupname", groupname);

        SQLiteDatabase db = Db.getWritableDatabase();

        try {
            db.insert("productgroups", null, value);
        } catch (SQLException e) {
            Log.e("Group Save Error 1", e.toString());
        }
    }

    public static void saveGroup(JsonArray value) {
        try {
            for (int i = 0; i < value.size(); i++) {
                JsonObject jData = value.get(i).getAsJsonObject();

                saveGroup(jData.get("id").getAsInt(), jData.get("grupadi").getAsString());
            }
        } catch (Exception e) {
            Log.e("Group Save Error 2", e.toString());
        }
    }

    public static void saveDeposit(int id, String depositename, double price, double taxpercent, int uniteid) {
        if (Db == null) {
            Db = new Database();
        }

        ContentValues value = new ContentValues();
        value.put("id", id);
        value.put("depositename", depositename);
        value.put("price", price);
        value.put("taxpercent", taxpercent);
        value.put("uniteid", uniteid);

        SQLiteDatabase db = Db.getWritableDatabase();

        try {
            db.insert("deposites", null, value);
        } catch (SQLException e) {
            Log.e("Deposit Save Error 1", e.toString());
        }
    }

    public static void saveDeposit(JsonArray value) {
        try {
            for (int i = 0; i < value.size(); i++) {
                JsonObject jData = value.get(i).getAsJsonObject();

                saveDeposit(jData.get("id").getAsInt(), jData.get("aciklama").getAsString(),
                        jData.get("fiyat").getAsDouble(), jData.get("vergioran").getAsDouble(),
                        jData.get("birimid").getAsInt());
            }
        } catch (Exception e) {
            Log.e("Deposit Save Error 2", e.toString());
        }
    }

    public static void savePrice(int productid, int priceorder, String description, double price) {
        if (Db == null) {
            Db = new Database();
        }

        ContentValues value = new ContentValues();
        value.put("productid", productid);
        value.put("priceorder", priceorder);
        value.put("description", description);
        value.put("price", price);
        value.put("newprice", price);

        SQLiteDatabase db = Db.getWritableDatabase();

        try {


            db.insert("productprices", null, value);



        } catch (SQLException e) {
            Log.e("Price Save Error 1", e.toString());
        }
    }

    private static SQLiteDatabase getWritableDatabase() {
        if (Db == null) {
            Db = new Database();
        }
        return Db.getWritableDatabase();
    }

    public static void savePrice(JsonArray value) {

        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            for (int i = 0; i < value.size(); i++) {
                JsonObject jData = value.get(i).getAsJsonObject();

                savePrice(jData.get("urunid").getAsInt(), jData.get("fiyatsira").getAsInt(),
                        jData.get("aciklama").getAsString(), jData.get("fiyat").getAsDouble());
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("Price Save Error 2", e.toString());
        }finally {
            db.endTransaction();
        }
    }

    public static ArrayList<HashMap<String, String>> getProductList(String filterSearch, int filterType, String filterGroupname, String filterTaxName, String sortField, String sortType,
                                                                    String whereClause) {
        if (Db == null)
            Db = new Database();

        String[] args = null;

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        SQLiteDatabase db = Db.getReadableDatabase();

        String lSql = "select p.id, p.plu, p.barcode, p.variantcode, p.productname, taxname, taxout, groupname, " +
                "stock, d.price as depositprice, costprice, pr.price as sellprice, pr.newprice, amountunite, unitamount, u.unitename," +
                "coalesce(pr.printlabel,0) as printlabel, coalesce(pr.priceorder,0) as priceorder, "+
                "(pr.price<>pr.newprice) as ischangedprice from products p " +
                "left join taxes t on t.id=p.taxid ";
        lSql += "left join productgroups g on g.id=p.groupid ";
        lSql += "left join deposites d on d.id=p.depositeid " +
                "left join unites u on u.id=p.uniteid " +
                "left join productprices pr on pr.productid=p.id "; // and pr.priceorder=1 ";

        String lWhere = "where 1=1 "; //cursor = database.rawQuery(query, new String[]{"%" + searchTerm + "%"});//db.rawQuery(sql, selectionArgs)
        if (filterSearch.length() > 0) {
            if (filterType == 0) {
                lWhere += " and (lower(productname) like '%" + filterSearch.toLowerCase() +
                          "%' or barcode like ? or plu like ? or variantcode like ?)";
                args = new String[] {"%" + filterSearch + "%",
                                     "%" + filterSearch + "%",
                                     "%" + filterSearch + "%"}; //FKILIC
            } else if (filterType == 1) {
                lWhere += " and (barcode='" + filterSearch + "')";
            } else if (filterType == 2) {
                lWhere += " and (plu='" + filterSearch + "')";
            } else if (filterType == 3) {
                lWhere += " and (variantcode='" + filterSearch + "')";
            } else if (filterType == 4) {
                lWhere += " and (origin='" + filterSearch + "')";
            }
        }

        if (!filterTaxName.equals("")) {
            lWhere += " and taxname='" + filterTaxName + "' ";
        }

        if (!filterGroupname.equals("")) {
            lWhere += " and groupname='" + filterGroupname + "' ";
        }

        if (whereClause.length() > 0) {
            lWhere += " and (" + whereClause + ")";
        }


        if (!sortField.equals("")) {
            lSql += lWhere + " COLLATE NOCASE order by " + sortField + " " + sortType;
        }

        Cursor cursor = db.rawQuery(lSql, args);//FKILIC

        if (cursor.getCount() > 0) {
            HashMap<String, String> map;

            Double sellPrice = 0.0, newPrice = 0.0;
            String price = "";
            while (cursor.moveToNext()) {
                map = new HashMap<String, String>();
                sellPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("sellprice"));
                newPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("newprice"));
                price = Variables.doubleToStr(sellPrice, 2);
                if (!sellPrice.equals(newPrice)) {
                    price += "    " + Database.vtContext.getString(R.string.newprice) + ": " + Variables.doubleToStr(newPrice, 2);
                }
                map.put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")));
                map.put("plu", cursor.getString(cursor.getColumnIndexOrThrow("plu")));
                map.put("barcode", cursor.getString(cursor.getColumnIndexOrThrow("barcode")));
                map.put("variantcode", cursor.getString(cursor.getColumnIndexOrThrow("variantcode")));
                map.put("productname", cursor.getString(cursor.getColumnIndexOrThrow("productname")));
                map.put("taxname", cursor.getString(cursor.getColumnIndexOrThrow("taxname")));
                map.put("taxout", cursor.getString(cursor.getColumnIndexOrThrow("taxout")));
                map.put("groupname", cursor.getString(cursor.getColumnIndexOrThrow("groupname")));
                map.put("stock", cursor.getString(cursor.getColumnIndexOrThrow("stock")));
                map.put("depositprice", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("depositprice")), 2));
                map.put("costprice", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("costprice")), 2));
                map.put("sellprice", price);//nf.format(cursor.getDouble(cursor.getColumnIndexOrThrow("sellprice"))));
                map.put("newprice", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("newprice")), 2));
                map.put("unitename", cursor.getString(cursor.getColumnIndexOrThrow("unitename")));
                map.put("unitamount", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("unitamount")), 2));
                map.put("amountunite", cursor.getString(cursor.getColumnIndexOrThrow("amountunite")));
                map.put("printlabel", cursor.getString(cursor.getColumnIndexOrThrow("printlabel")));
                map.put("priceorder", cursor.getString(cursor.getColumnIndexOrThrow("priceorder")));
                if (cursor.getString(cursor.getColumnIndexOrThrow("ischangedprice")) == null)
                    map.put("ischangedprice", "0");
                else
                    map.put("ischangedprice", cursor.getString(cursor.getColumnIndexOrThrow("ischangedprice")));

                list.add(map);
            }
            cursor.close();
        }


        return list;
    }


    public static ArrayList<HashMap<String, String>> getProductList_Erkan(String filterSearch, int filterType, String filterGroupname, String filterTaxName, String sortField, String sortType,
                                                                    String whereClause) {
        if (Db == null)
            Db = new Database();

        String[] args = null;

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        SQLiteDatabase db = Db.getReadableDatabase();

        String lSql = "select p.id, p.plu, p.barcode, p.variantcode, p.productname, taxname, taxout, groupname, " +
                "stock, d.price as depositprice, costprice, pr.price as sellprice, pr.newprice, amountunite, unitamount, u.unitename," +
                "coalesce(pr.printlabel,0) as printlabel, coalesce(pr.priceorder,0) as priceorder, "+
                "(pr.price<>pr.newprice) as ischangedprice from products p " +
                "left join taxes t on t.id=p.taxid ";
        lSql += "left join productgroups g on g.id=p.groupid ";
        lSql += "left join deposites d on d.id=p.depositeid " +
                "left join unites u on u.id=p.uniteid " +
                "left join productprices pr on pr.productid=p.id "; // and pr.priceorder=1 ";

        String lWhere = "where 1=1 "; //cursor = database.rawQuery(query, new String[]{"%" + searchTerm + "%"});//db.rawQuery(sql, selectionArgs)
        if (filterSearch.length() > 0) {
            if (filterType == 0) {
                lWhere += " and (lower(productname) like '%" + filterSearch.toLowerCase() +
                        "%' or barcode like ? or plu like ? or variantcode like ?)";
                args = new String[] {"%" + filterSearch + "%",
                        "%" + filterSearch + "%",
                        "%" + filterSearch + "%"}; //FKILIC
            } else if (filterType == 1) {
                lWhere += " and (barcode='" + filterSearch + "')";
            } else if (filterType == 2) {
                lWhere += " and (plu='" + filterSearch + "')";
            } else if (filterType == 3) {
                lWhere += " and (variantcode='" + filterSearch + "')";
            } else if (filterType == 4) {
                lWhere += " and (origin='" + filterSearch + "')";
            }
        }

        if (!filterTaxName.equals("")) {
            lWhere += " and taxname='" + filterTaxName + "' ";
        }

        if (!filterGroupname.equals("")) {
            // 22.08.2024: Erkan Gruplarak gore arama yapmiyacak
         //   lWhere += " and groupname='" + filterGroupname + "' ";
        }

        if (whereClause.length() > 0) {
            lWhere += " and (" + whereClause + ")";
        }


        if (!sortField.equals("")) {
            lSql += lWhere + " COLLATE NOCASE order by " + sortField + " " + sortType;
        }

        Cursor cursor = db.rawQuery(lSql, args);//FKILIC

        if (cursor.getCount() > 0) {
            HashMap<String, String> map;

            Double sellPrice = 0.0, newPrice = 0.0;
            String price = "";
            while (cursor.moveToNext()) {
                map = new HashMap<String, String>();
                sellPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("sellprice"));
                newPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("newprice"));
                price = Variables.doubleToStr(sellPrice, 2);
                if (!sellPrice.equals(newPrice)) {
                    price += "    " + Database.vtContext.getString(R.string.newprice) + ": " + Variables.doubleToStr(newPrice, 2);
                }
                map.put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")));
                map.put("plu", cursor.getString(cursor.getColumnIndexOrThrow("plu")));
                map.put("barcode", cursor.getString(cursor.getColumnIndexOrThrow("barcode")));
                map.put("variantcode", cursor.getString(cursor.getColumnIndexOrThrow("variantcode")));
                map.put("productname", cursor.getString(cursor.getColumnIndexOrThrow("productname")));
                map.put("taxname", cursor.getString(cursor.getColumnIndexOrThrow("taxname")));
                map.put("taxout", cursor.getString(cursor.getColumnIndexOrThrow("taxout")));
                map.put("groupname", cursor.getString(cursor.getColumnIndexOrThrow("groupname")));
                map.put("stock", cursor.getString(cursor.getColumnIndexOrThrow("stock")));
                map.put("depositprice", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("depositprice")), 2));
                map.put("costprice", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("costprice")), 2));
                map.put("sellprice", price);//nf.format(cursor.getDouble(cursor.getColumnIndexOrThrow("sellprice"))));
                map.put("newprice", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("newprice")), 2));
                map.put("unitename", cursor.getString(cursor.getColumnIndexOrThrow("unitename")));
                map.put("unitamount", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("unitamount")), 2));
                map.put("amountunite", cursor.getString(cursor.getColumnIndexOrThrow("amountunite")));
                map.put("printlabel", cursor.getString(cursor.getColumnIndexOrThrow("printlabel")));
                map.put("priceorder", cursor.getString(cursor.getColumnIndexOrThrow("priceorder")));
                if (cursor.getString(cursor.getColumnIndexOrThrow("ischangedprice")) == null)
                    map.put("ischangedprice", "0");
                else
                    map.put("ischangedprice", cursor.getString(cursor.getColumnIndexOrThrow("ischangedprice")));

                list.add(map);
            }
            cursor.close();
        }


        return list;
    }




    public static ArrayList<HashMap<String, String>> getGroupList() {
        if (Db == null)
            Db = new Database();

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        SQLiteDatabase db = Db.getReadableDatabase();

        Cursor cursor = db.rawQuery("select id, groupname from productgroups order by groupname",
                null);

        if (cursor.getCount() > 0) {
            HashMap<String, String> map;

            while (cursor.moveToNext()) {
                map = new HashMap<String, String>();
                map.put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")));
                map.put("groupname", cursor.getString(cursor.getColumnIndexOrThrow("groupname")));

                list.add(map);
            }
            cursor.close();
        }

        return list;
    }

    public static List<String> getGroupListForSpinner(String firstItem) {
        if (Db == null)
            Db = new Database();

        ArrayList<String> list = new ArrayList<String>();
        list.add(firstItem);
        SQLiteDatabase db = Db.getReadableDatabase();

        Cursor cursor = db.rawQuery("select groupname from productgroups order by groupname",
                null);
        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                list.add(cursor.getString(cursor.getColumnIndexOrThrow("groupname")));
            }
            cursor.close();
        }
        return list;
    }

    public static ArrayList<HashMap<String, String>> getTaxList() {
        if (Db == null)
            Db = new Database();

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        SQLiteDatabase db = Db.getReadableDatabase();

        Cursor cursor = db.rawQuery("select id, taxname from taxes order by taxname",
                null);

        if (cursor.getCount() > 0) {
            HashMap<String, String> map;

            while (cursor.moveToNext()) {
                map = new HashMap<String, String>();
                map.put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")));
                map.put("taxname", cursor.getString(cursor.getColumnIndexOrThrow("taxname")));

                list.add(map);
            }
            cursor.close();
        }


        return list;
    }

    public static List<String> getTaxListForSpinner(String firstItem) {
        if (Db == null)
            Db = new Database();

        ArrayList<String> list = new ArrayList<String>();
        list.add(firstItem);
        SQLiteDatabase db = Db.getReadableDatabase();

        Cursor cursor = db.rawQuery("select taxname from taxes order by taxname",
                null);
        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                list.add(cursor.getString(cursor.getColumnIndexOrThrow("taxname")));
            }
            cursor.close();
        }
        return list;
    }

    public static List<String> getUnitListForSpinner(String firstItem) {
        if (Db == null)
            Db = new Database();

        ArrayList<String> list = new ArrayList<String>();
        list.add(firstItem);
        SQLiteDatabase db = Db.getReadableDatabase();

        Cursor cursor = db.rawQuery("select unitename from unites order by unitename",
                null);
        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                list.add(cursor.getString(cursor.getColumnIndexOrThrow("unitename")));
            }
            cursor.close();
        }
        return list;
    }

    public static List<String> getDepositListForSpinner(String firstItem) {
        if (Db == null)
            Db = new Database();

        ArrayList<String> list = new ArrayList<String>();
        list.add(firstItem);
        SQLiteDatabase db = Db.getReadableDatabase();

        Cursor cursor = db.rawQuery("select depositename from deposites order by depositename",
                null);
        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                list.add(cursor.getString(cursor.getColumnIndexOrThrow("depositename")));
            }
            cursor.close();
        }
        return list;
    }

    public static void changePrice(int productid, int priceorder, double price) {
        if (Db == null) {
            Db = new Database();
        }

        ContentValues value = new ContentValues();
        value.put("productid", productid);
        value.put("priceorder", priceorder);
        value.put("newprice", price);
        value.put("changed", 1);
        value.put("printlabel", 1);

        boolean isNewProduct = false;
        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor = db.query("products", new String[]{"isnew"}, "id=?", new String[]{String.valueOf(productid)}, "", "", "");
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            if (cursor.getInt(0) == 1) {
                isNewProduct = true;
            }
        }


        db = Db.getWritableDatabase();

        try {
            int lCount = db.update("productprices", value, "productid=? and priceorder=?",
                    new String[]{String.valueOf(productid), String.valueOf(priceorder)});

            if (lCount == 0) {
                if (isNewProduct)
                    value.put("isnew", 1);
                db.insert("productprices", null, value);

            }
            //value.clear();
            //value.put("printlabel", 1);
            //db.update("products", value, "id=?",new String[]{String.valueOf(productid)});
        } catch (SQLException e) {
            Log.e("Price update Error 1", e.toString());
        }
    }

    public static void cancelChangedPrice(int productid) {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getWritableDatabase();

        try {
            db.execSQL("update productprices set newprice=price, printlabel=0, changed=0 where productid=" + String.valueOf(productid) + " and priceorder=1");

            //db.execSQL("update products set printlabel=0 where id=" + String.valueOf(productid) + " and changed=0");
        } catch (SQLException e) {
            Log.e("Cancelprice Error 1", e.toString());
        }
    }

    public static void addToLabelList(int productid, int priceorder) {
        if (Db == null) {
            Db = new Database();
        }

        ContentValues value = new ContentValues();
        value.put("productid", productid);
        value.put("printlabel", 1);
        value.put("priceorder",priceorder);

        SQLiteDatabase db = Db.getWritableDatabase();

        try {
            //db.update("products", value, "id=?",new String[]{String.valueOf(productid)});
            db.update("productprices", value, "productid=? and priceorder=?",
                    new String[]{String.valueOf(productid),String.valueOf(priceorder)});

        } catch (SQLException e) {
            Log.e("AddtoLabelList Error 1", e.toString());
        }
    }

    public static void removeFromLabelList(int productid, int priceorder) {
        if (Db == null) {
            Db = new Database();
        }

        ContentValues value = new ContentValues();
        value.put("productid", productid);
        value.put("printlabel", 0);
        value.put("changed", 1);
        value.put("priceorder", priceorder);

        SQLiteDatabase db = Db.getWritableDatabase();

        try {
            db.update("productprices", value, "productid=? and priceorder=?",
                        new String[]{String.valueOf(productid), String.valueOf(priceorder)});

        } catch (SQLException e) {
            Log.e("AddtoLabelList Error 1", e.toString());
        }
    }

    public static Product getProduct(int productId, String barcode) {
        if (Db == null) {
            Db = new Database();
        }

        Product product = new Product();

        SQLiteDatabase db = Db.getReadableDatabase();

        String lSql = "select p.*, pr.price, pr.newprice, u.unitename, t.taxname, t.taxin, "+
                "t.taxout, g.groupname, d.depositename from products p " +
                "left join productprices pr on pr.priceorder=1 and pr.productid=p.id " +
                "left join unites u on u.id=p.uniteid " +
                "left join taxes t on t.id=p.taxid " +
                "left join productgroups g on g.id=p.groupid " +
                "left join deposites d on d.id=p.depositeid ";
        if (barcode.length() > 0) {
            lSql += "where barcode='" + barcode + "'";
        } else {
            lSql += "where p.id=" + String.valueOf(productId);
        }

        Cursor cursor = db.rawQuery(lSql, null);

        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            product.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            product.setGroupId(cursor.getInt(cursor.getColumnIndexOrThrow("groupid")));
            product.setRowNumber(cursor.getInt(cursor.getColumnIndexOrThrow("rownumber")));
            product.setBarcode(cursor.getString(cursor.getColumnIndexOrThrow("barcode")));
            product.setPlu(cursor.getString(cursor.getColumnIndexOrThrow("plu")));
            product.setOrigin(cursor.getString(cursor.getColumnIndexOrThrow("origin")));
            product.setVariantcode(cursor.getString(cursor.getColumnIndexOrThrow("variantcode")));
            product.setProductName(cursor.getString(cursor.getColumnIndexOrThrow("productname")));
            product.setCostPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("costprice")));
            product.setTaxId(cursor.getInt(cursor.getColumnIndexOrThrow("taxid")));
            product.setDepositId(cursor.getInt(cursor.getColumnIndexOrThrow("depositeid")));
            product.setUnitId(cursor.getInt(cursor.getColumnIndexOrThrow("uniteid")));
            product.setIsManuelPrice(cursor.getInt(cursor.getColumnIndexOrThrow("ismanualprice")));
            product.setStock(cursor.getDouble(cursor.getColumnIndexOrThrow("stock")));
            product.setIsStockActive(cursor.getInt(cursor.getColumnIndexOrThrow("isstockactive")));
            product.setUnitAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("unitamount")));
            product.setAmountUnit(cursor.getString(cursor.getColumnIndexOrThrow("amountunite")));
            product.setTaraId(cursor.getInt(cursor.getColumnIndexOrThrow("taraid")));
            product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
            product.setIngredients(cursor.getString(cursor.getColumnIndexOrThrow("ingredients")));
            product.setActive(cursor.getInt(cursor.getColumnIndexOrThrow("active")));
            product.setInputStock(cursor.getDouble(cursor.getColumnIndexOrThrow("inputstock")));
            product.setOutputStock(cursor.getDouble(cursor.getColumnIndexOrThrow("outputstock")));
            product.setProductype(cursor.getInt(cursor.getColumnIndexOrThrow("producttype")));
            product.setCriticalStock(cursor.getDouble(cursor.getColumnIndexOrThrow("criticalstock")));
            product.setInputReturn(cursor.getDouble(cursor.getColumnIndexOrThrow("inputreturn")));
            product.setProductions(cursor.getDouble(cursor.getColumnIndexOrThrow("production")));
            product.setReturns(cursor.getDouble(cursor.getColumnIndexOrThrow("returns")));
            product.setWastages(cursor.getDouble(cursor.getColumnIndexOrThrow("wastages")));
            product.setConsumption(cursor.getDouble(cursor.getColumnIndexOrThrow("consumption")));
            product.setSuppliers(cursor.getString(cursor.getColumnIndexOrThrow("suppliers")));
            product.setSupplierId(cursor.getInt(cursor.getColumnIndexOrThrow("suppliersid")));
            product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
            product.setNewPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("newprice")));
            product.setTaxName(cursor.getString(cursor.getColumnIndexOrThrow("taxname")));
            product.setTaxIn(cursor.getDouble(cursor.getColumnIndexOrThrow("taxin")));
            product.setTaxOut(cursor.getDouble(cursor.getColumnIndexOrThrow("taxout")));
            product.setGroupName(cursor.getString(cursor.getColumnIndexOrThrow("groupname")));
            product.setDepositeName(cursor.getString(cursor.getColumnIndexOrThrow("depositename")));
            product.setUniteName(cursor.getString(cursor.getColumnIndexOrThrow("unitename")));


        }

        return product;
    }


    public static Product getProductErkan(int productId, String barcode, int priceOrder) {
        if (Db == null) {
            Db = new Database();
        }

        Product product = new Product();

        SQLiteDatabase db = Db.getReadableDatabase();

        String lSql = "select p.*, pr.price, pr.newprice, u.unitename, t.taxname, t.taxin, "+
                "t.taxout, g.groupname, d.depositename from products p " +

                // 30.07.2024 Erkan
                //"left join productprices pr on pr.priceorder=1 and pr.productid=p.id " +
                "left join productprices pr on pr.priceorder="+priceOrder+" and pr.productid=p.id " +


                "left join unites u on u.id=p.uniteid " +
                "left join taxes t on t.id=p.taxid " +
                "left join productgroups g on g.id=p.groupid " +
                "left join deposites d on d.id=p.depositeid ";
        if (barcode.length() > 0) {
            lSql += "where barcode='" + barcode + "'";
        } else {
            lSql += "where p.id=" + String.valueOf(productId);
        }

        Cursor cursor = db.rawQuery(lSql, null);

        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            product.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            product.setGroupId(cursor.getInt(cursor.getColumnIndexOrThrow("groupid")));
            product.setRowNumber(cursor.getInt(cursor.getColumnIndexOrThrow("rownumber")));
            product.setBarcode(cursor.getString(cursor.getColumnIndexOrThrow("barcode")));
            product.setPlu(cursor.getString(cursor.getColumnIndexOrThrow("plu")));
            product.setOrigin(cursor.getString(cursor.getColumnIndexOrThrow("origin")));
            product.setVariantcode(cursor.getString(cursor.getColumnIndexOrThrow("variantcode")));
            product.setProductName(cursor.getString(cursor.getColumnIndexOrThrow("productname")));
            product.setCostPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("costprice")));
            product.setTaxId(cursor.getInt(cursor.getColumnIndexOrThrow("taxid")));
            product.setDepositId(cursor.getInt(cursor.getColumnIndexOrThrow("depositeid")));
            product.setUnitId(cursor.getInt(cursor.getColumnIndexOrThrow("uniteid")));
            product.setIsManuelPrice(cursor.getInt(cursor.getColumnIndexOrThrow("ismanualprice")));
            product.setStock(cursor.getDouble(cursor.getColumnIndexOrThrow("stock")));
            product.setIsStockActive(cursor.getInt(cursor.getColumnIndexOrThrow("isstockactive")));
            product.setUnitAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("unitamount")));
            product.setAmountUnit(cursor.getString(cursor.getColumnIndexOrThrow("amountunite")));
            product.setTaraId(cursor.getInt(cursor.getColumnIndexOrThrow("taraid")));
            product.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
            product.setIngredients(cursor.getString(cursor.getColumnIndexOrThrow("ingredients")));
            product.setActive(cursor.getInt(cursor.getColumnIndexOrThrow("active")));
            product.setInputStock(cursor.getDouble(cursor.getColumnIndexOrThrow("inputstock")));
            product.setOutputStock(cursor.getDouble(cursor.getColumnIndexOrThrow("outputstock")));
            product.setProductype(cursor.getInt(cursor.getColumnIndexOrThrow("producttype")));
            product.setCriticalStock(cursor.getDouble(cursor.getColumnIndexOrThrow("criticalstock")));
            product.setInputReturn(cursor.getDouble(cursor.getColumnIndexOrThrow("inputreturn")));
            product.setProductions(cursor.getDouble(cursor.getColumnIndexOrThrow("production")));
            product.setReturns(cursor.getDouble(cursor.getColumnIndexOrThrow("returns")));
            product.setWastages(cursor.getDouble(cursor.getColumnIndexOrThrow("wastages")));
            product.setConsumption(cursor.getDouble(cursor.getColumnIndexOrThrow("consumption")));
            product.setSuppliers(cursor.getString(cursor.getColumnIndexOrThrow("suppliers")));
            product.setSupplierId(cursor.getInt(cursor.getColumnIndexOrThrow("suppliersid")));
            product.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
            product.setNewPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("newprice")));
            product.setTaxName(cursor.getString(cursor.getColumnIndexOrThrow("taxname")));
            product.setTaxIn(cursor.getDouble(cursor.getColumnIndexOrThrow("taxin")));
            product.setTaxOut(cursor.getDouble(cursor.getColumnIndexOrThrow("taxout")));
            product.setGroupName(cursor.getString(cursor.getColumnIndexOrThrow("groupname")));
            product.setDepositeName(cursor.getString(cursor.getColumnIndexOrThrow("depositename")));
            product.setUniteName(cursor.getString(cursor.getColumnIndexOrThrow("unitename")));


        }

        return product;
    }




    public static int getGroupId(String value) {
        if (Db == null) {
            Db = new Database();
        }

        Product product = new Product();

        SQLiteDatabase db = Db.getReadableDatabase();

        String lSql = "select id from productgroups where groupname='" + value + "'";

        Cursor cursor = db.rawQuery(lSql, null);
        int lId = 0;
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            lId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        }

        return lId;
    }

    public static int getUnitId(String value) {
        if (Db == null) {
            Db = new Database();
        }

        Product product = new Product();

        SQLiteDatabase db = Db.getReadableDatabase();

        String lSql = "select id from unites where unitename='" + value + "'";

        Cursor cursor = db.rawQuery(lSql, null);
        int lId = 0;
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            lId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        }

        return lId;
    }

    public static int getTaxId(String value) {
        if (Db == null) {
            Db = new Database();
        }

        Product product = new Product();

        SQLiteDatabase db = Db.getReadableDatabase();

        String lSql = "select id from taxes where taxname='" + value + "'";

        Cursor cursor = db.rawQuery(lSql, null);
        int lId = 0;
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            lId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        }

        return lId;
    }

    public static int getDepositId(String value) {
        if (Db == null) {
            Db = new Database();
        }

        Product product = new Product();

        SQLiteDatabase db = Db.getReadableDatabase();

        String lSql = "select id from deposites where depositename='" + value + "'";

        Cursor cursor = db.rawQuery(lSql, null);
        int lId = 0;
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            lId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
        }

        return lId;
    }

    public static boolean isThereNewProduct() {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getReadableDatabase();

        String lSql = "select id from products where isnew=1";

        Cursor cursor = db.rawQuery(lSql, null);

        return (cursor.getCount() > 0);
    }

    public static boolean isThereNewOrUpdatedProduct() {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getReadableDatabase();

        String lSql = "select id from products where isnew=1 or changed=1";

        Cursor cursor = db.rawQuery(lSql, null);

        return (cursor.getCount() > 0);
    }

    public static void changeProducts(String sonEk, double fiyat, int depozitoid) {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getWritableDatabase();

        try {
            db.execSQL("update products set changed=1,  productname=productname||'" + sonEk + "', depositeid=" + depozitoid);

            ContentValues value = new ContentValues();
            value.put("newprice", fiyat);
            value.put("printlabel",1);
            db.update("productprices", value, "", null);
        } catch (SQLException e) {
            Log.e("debug Error 1", e.toString());
        }
    }

    public static double changeProductStock(long productId, double amount) {
        if (Db == null) {
            Db = new Database();
        }


        SQLiteDatabase db = Db.getReadableDatabase();

        double lStock = 0.0;

        Cursor cursor = db.rawQuery("select stock from products where id=?", new String[]{String.valueOf(productId)});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            lStock = cursor.getDouble(0);
        }

        db = Db.getWritableDatabase();
        ContentValues values = new ContentValues();
        values = new ContentValues();
        values.put("stock", lStock + amount);
        db.update("products", values, "id=?", new String[]{String.valueOf(productId)});

        return lStock + amount;
    }

    public static double getProductStock(long productId) {
        if (Db == null) {
            Db = new Database();
        }


        SQLiteDatabase db = Db.getReadableDatabase();

        double lStock = 0.0;

        Cursor cursor = db.rawQuery("select stock from products where id=?", new String[]{String.valueOf(productId)});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            lStock = cursor.getDouble(0);
        }

        return lStock;
    }
}
