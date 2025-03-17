package com.eqpos.eqentry.DB;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.eqpos.eqentry.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by dursu on 20.03.2018.
 */

public class OrderDao {
    private static Database Db;

    public static void addProductToOrder(int supplierId, int productId, Double amount, String unit) {
        if (Db == null) {
            Db = new Database();
        }

        if (productId <= 0)
            return;

        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor = db.query("purchaseorder", new String[]{"id, amount"}, "supplierid=? and productid=?", new String[]{String.valueOf(supplierId), String.valueOf(productId)}, "", "", "");
        int lId = 0;
        Double lAmount = amount;
        if (cursor.getCount() > 0) {
            cursor.moveToNext();

            lId = cursor.getInt(0);
            lAmount = cursor.getDouble(1) + amount;
        }

        db = Db.getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put("productid", productId);
        value.put("amount", amount);
        value.put("unit", unit);
        value.put("supplierid", supplierId);

        try {
            if (lId > 0)
                db.update("purchaseorder", value, "id=?", new String[]{String.valueOf(lId)});
            else
                db.insert("purchaseorder", null, value);
        } catch (SQLException e) {
            Log.e("purchaseorder", e.getMessage());
        }
    }

    public static void addProductToOrderRandom(int sayi) {
        if (Db == null) {
            Db = new Database();
        }

        int miktar = 0;

        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor;
        cursor = db.rawQuery("select min(id) from products", null);
        cursor.moveToNext();
        int minId = cursor.getInt(0);
        int start = 0;
        int lsayi = 0;
        int lId = 0;
        do {
            start = (int) (Math.random() * 2499) + minId;
            miktar = (int) (Math.random() * 20) + 1;

            cursor = db.query("products", new String[]{"id"}, "id>?", new String[] {String.valueOf(start)}, "", "", "id", "1");
            if (cursor.getCount() > 0) {
                cursor.moveToNext();

                lId = cursor.getInt(0);
                addProductToOrder(0, lId, (double)miktar,"st.");
            }
            lsayi += 1;
        } while (lsayi < sayi);
    }

    public static void removeOrder() {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getWritableDatabase();

        try {
            db.delete("purchaseorder", null, null);
        } catch (SQLException e) {
            Log.e("purchaseorder", e.getMessage());
        }
    }

    public static void removeFromOrder(int id) {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getWritableDatabase();

        try {
            db.delete("purchaseorder", "id=?", new String[]{String.valueOf(id)});
        } catch (SQLException e) {
            Log.e("purchaseorder", e.getMessage());
        }
    }

    public static ArrayList<HashMap<String, String>> getOrderList(int supplierId) {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getReadableDatabase();

        ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();

        Cursor cursor = db.rawQuery("select o.*, p.productname, p.barcode from purchaseorder o " +
                "inner join products p on p.id=o.productid where o.supplierid="+ String.valueOf(supplierId) +" order by id", null);

        if (cursor.getCount() > 0) {
            HashMap<String, String> map;

            Double costPrice = 0.0, amount = 0.0;
            String price = "";
            while (cursor.moveToNext()) {
                map = new HashMap<String, String>();
                amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                map.put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")));
                map.put("productid", cursor.getString(cursor.getColumnIndexOrThrow("productid")));
                map.put("productname", cursor.getString(cursor.getColumnIndexOrThrow("productname")));
                map.put("amount", cursor.getString(cursor.getColumnIndexOrThrow("amount")));
                map.put("unit", cursor.getString(cursor.getColumnIndexOrThrow("unit")));
                map.put("barcode", cursor.getString(cursor.getColumnIndexOrThrow("barcode")));

                result.add(map);
            }
            cursor.close();
        }

        return result;
    }


    public static void createRandomOrder(int prSupplierId) {
        if (Db == null) {
            Db = new Database();
        }

        Random r = new Random();
        SQLiteDatabase db = Db.getReadableDatabase();
        double lAmount = 1.0;
        int lCount = r.nextInt(100)+1;
        Cursor cursor = db.rawQuery("select p.id, u.unitename from products p " +
                "inner join unites u on u.id=p.uniteid ORDER BY RANDOM() limit "+String.valueOf(lCount), null);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                lAmount = r.nextInt(10)+1.0;
                addProductToOrder(prSupplierId, cursor.getInt(0), lAmount, cursor.getString(1));
            }
        }

        }
}
