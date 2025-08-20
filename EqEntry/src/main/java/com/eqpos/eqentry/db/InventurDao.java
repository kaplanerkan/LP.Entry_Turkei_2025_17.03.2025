package com.eqpos.eqentry.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

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
                "where p.productname like '%"+ find +"%' collate nocase or p.barcode like '%"+ find +"%' or p.plu like '%"+ find +"%' order by p.productname",
                null);

        if (cursor.getCount() > 0) {
            HashMap<String, String> map;

            while (cursor.moveToNext()) {
                map = new HashMap<String, String>();
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

    public static void changeNewStock(int productId, Double value) {
        if (Db == null) {
            Db = new Database();
        }
        ContentValues values = new ContentValues();
        values.put("newquantity", value);
        SQLiteDatabase db = Db.getWritableDatabase();
        int i = db.update("inventur", values, "productid=?", new String[]{ String.valueOf(productId)});
    }
}
