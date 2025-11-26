package com.eqpos.eqentry.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.eqpos.eqentry.models.SayimModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dursu on 24.10.2018.
 */

public class InventurDao {
    private static Database Db;

    public static void deleteAll() {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getWritableDatabase();
        db.delete("inventur", "", null);
    }

    public static void addNewProductsToInventur() {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getWritableDatabase();
        db.execSQL("insert into inventur (productid, currentquantity, newquantity) " +
                "select p.id, p.stock, p.stock from products p " +
                "left join inventur i on i.productid=p.id " +
                "where i.productid is null");

    }

    public static ArrayList<HashMap<String, String>> getInventurList(String find) {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getReadableDatabase();
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        Cursor cursor = db.rawQuery("select i.productid, p.plu, p.productname, i.currentquantity, i.newquantity, i.difference from inventur i " +
                        "inner join products p on p.id=i.productid " +
                        "where p.productname like '%" + find + "%' collate nocase or p.barcode like '%" + find + "%' or p.plu like '%" + find + "%' order by p.productname",
                null);

        if (cursor.getCount() > 0) {
            HashMap<String, String> map;

            while (cursor.moveToNext()) {
                map = new HashMap<>();
                map.put("productid", String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("productid"))));
                map.put("plu", cursor.getString(cursor.getColumnIndexOrThrow("plu")));
                map.put("productname", cursor.getString(cursor.getColumnIndexOrThrow("productname")));
                map.put("currentquantity", String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("currentquantity"))));
                map.put("newquantity", String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("newquantity"))));
                map.put("difference", String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("difference"))));

                list.add(map);
            }
        }
        cursor.close();
        return list;
    }

    public static void changeNewStock_orj(int productId, Double value, int warehouseid) {
        if (Db == null) {
            Db = new Database();
        }
        ContentValues values = new ContentValues();
        values.put("newquantity", value);
        values.put("warehouseid", warehouseid);
        SQLiteDatabase db = Db.getWritableDatabase();
        int i = db.update("inventur", values, "productid=?", new String[]{String.valueOf(productId)});
    }
    public static void changeNewStock(int productId, Double value, int warehouseId) {
        if (Db == null) {
            Db = new Database();
        }

        ContentValues values = new ContentValues();
        values.put("newquantity", value);
        values.put("warehouseid", warehouseId);

        SQLiteDatabase db = null;
        try {
            db = Db.getWritableDatabase();
            int rowsAffected = db.update("inventur", values, "productid =?",
                    new String[]{String.valueOf(productId)});

            if (rowsAffected == 0) {
                // Eğer update yapılamazsa, yeni kayıt ekle
                values.put("productid", productId);
                db.insert("inventur", null, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public static void changeNewStock_Erkan(int productId, Double value, int warehouseId, int difference) {
        if (Db == null) {
            Db = new Database();
        }

        ContentValues values = new ContentValues();
        values.put("newquantity", value);
        values.put("difference", difference);
        values.put("warehouseid", warehouseId);

        SQLiteDatabase db = null;
        try {
            db = Db.getWritableDatabase();
            int rowsAffected = db.update("inventur", values, "productid =?",
                    new String[]{String.valueOf(productId)});

            if (rowsAffected == 0) {
                // Eğer update yapılamazsa, yeni kayıt ekle
                values.put("productid", productId);
                db.insert("inventur", null, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }



    public static List<SayimModel> getSayimListem(int depoId) {

        List<SayimModel> sayimListesi = new ArrayList<>();

        if (Db == null) {
            Db = new Database();
        }

        try (SQLiteDatabase db = Db.getReadableDatabase()) {
            String query = "";
            if (depoId == 0) {
                query = "SELECT p.productname, i.productid, i.newquantity, i.warehouseid " +
                        "FROM inventur i " +
                        " INNER JOIN  products p ON p.id = i.productid " +
                        " WHERE i.difference<>0";
            } else {
                query = "SELECT p.productname, i.productid, i.newquantity, i.warehouseid " +
                        "FROM inventur i " +
                        " INNER JOIN  products p ON p.id = i.productid " +
                        " WHERE  i.warehouseid<> 0 ";
            // " WHERE i.difference<>0 AND i.warehouseid =" + depoId;
            }


            Cursor cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    SayimModel sayimModel = new SayimModel();
                    sayimModel.setProductId(cursor.getString(cursor.getColumnIndexOrThrow("productid")));
                    sayimModel.setUrunAdi(cursor.getString(cursor.getColumnIndexOrThrow("productname")));
                    sayimModel.setNewQuantity(cursor.getString(cursor.getColumnIndexOrThrow("newquantity")));
                    sayimModel.setWarehouseId(cursor.getString(cursor.getColumnIndexOrThrow("warehouseid")));

                    sayimListesi.add(sayimModel);
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (SQLException e) {
            Log.e("getSayimListem", "getSayimListem error: " + e.toString());
        }
        return sayimListesi;
    }


    public static void deleteSayim(int productid) {
        if (Db == null)
            Db = new Database();

        SQLiteDatabase db = Db.getWritableDatabase();

        try {
            db.beginTransaction();
            db.delete("inventur", "productid=?", new String[]{String.valueOf(productid)});
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (SQLException e) {
            Log.e("remove delivery", "deleteSayim :: " + e.getMessage());
        }
    }

    public static boolean sayimMiktariGuncelle(String productId, String warehouseId, String yeniMiktar) {
        try {
            if (Db == null)
                Db = new Database();

            SQLiteDatabase db = Db.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("newquantity", yeniMiktar);

            int result = db.update("inventur", values,
                    "productid = ? AND warehouseid = ?",
                    new String[]{productId, warehouseId});

            return result > 0;
        } catch (Exception e) {
            Log.e("miktarGuncelle", "Güncelleme hatası: " + e.toString());
            return false;
        }
    }


    /**
     * Tabloyu tamamen temizler (TRUNCATE benzeri)
     */
    public static boolean sayimTabloyuTamamenSil() {
        try {
            SQLiteDatabase db = Db.getWritableDatabase();
            db.execSQL("DELETE FROM inventur");
            db.execSQL("DELETE FROM sqlite_sequence WHERE name='inventur'"); // AUTOINCREMENT sıfırlama
            Log.d("tabloyuTemizle", "Tablo temizlendi");
            return true;
        } catch (Exception e) {
            Log.e("tabloyuTemizle", "Tablo temizleme hatası: " + e.toString());
            return false;
        }
    }


}
