package com.eqpos.eqentry.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.eqpos.eqentry.models.DepoModel;
import com.eqpos.eqentry.models.VaryantModelWithBadget;

import java.util.ArrayList;
import java.util.List;

public class WarehouseDao {

    private static Database Db;

    private static SQLiteDatabase getWritableDatabase() {
        if (Db == null) {
            Db = new Database();
        }
        return Db.getWritableDatabase();
    }


    public static List<DepoModel> getAllWarehouses() {
        List<DepoModel> depoModelList = new ArrayList<>();

        Database Db = new Database();
        SQLiteDatabase db = Db.getReadableDatabase();

        // Sorgu: parentid = 0 olan kayıtları seç
        String query = "SELECT * FROM warehouses";
        Cursor cursor = db.rawQuery(query, null);

        try {
            // Cursor üzerinde döngü ile verileri oku
            if (cursor.moveToFirst()) {
                do {
                    // Her bir satırdan verileri al
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String warehousename = cursor.getString(cursor.getColumnIndexOrThrow("warehousename"));
                    DepoModel depoModel = new DepoModel(id,warehousename);
                    depoModelList.add(depoModel);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Veri çekme hatası: " + e.toString());
        } finally {
            cursor.close();
            db.close();
        }
        return depoModelList;
    }

}
