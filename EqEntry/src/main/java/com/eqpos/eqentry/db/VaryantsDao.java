package com.eqpos.eqentry.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.eqpos.eqentry.models.VaryantModelWithBadget;

import java.util.ArrayList;
import java.util.List;

public class VaryantsDao {

    private static Database Db;

    private static SQLiteDatabase getWritableDatabase() {
        if (Db == null) {
            Db = new Database();
        }
        return Db.getWritableDatabase();
    }

    public static void deleteAll() {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.delete("varyants", null, null); // Tablo adını kontrol edin
        } catch (SQLException e) {
            Log.e("Varyants", "deleteAll error: " + e.toString());
        }
        // Not: db.close() çağrısı, Database sınıfında uygun şekilde yönetiliyorsa gerekli olmayabilir
    }


    public static void saveVaryantsToDatabase(List<VaryantModelWithBadget> varyantList) {
        // SQLite veya Room gibi bir veritabanı yöneticisi kullanarak verileri kaydet
        // Örnek olarak SQLite kullanımı:
        SQLiteDatabase db = null;
        db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (VaryantModelWithBadget varyant : varyantList) {
                ContentValues values = new ContentValues();
                values.put("id", varyant.getId());
                values.put("sira", varyant.getSira());
                values.put("tanim", varyant.getTanim());
                values.put("rowcell", varyant.getRowcell());
                values.put("aciklama", varyant.getAciklama());
                values.put("parentid", varyant.getParentid());

                // Veriyi tabloya ekle (örneğin tablo adı: "varyants")
                db.insertWithOnConflict("varyants", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("saveToDatabase", "Veritabanına yazma hatası: " + e.toString());
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // Hem Gruplari hem de alt grupları çeken fonksiyon
    public static List<String> getAllGrupTurleri(int rowcell, int parentid) {
        List<String> grupTuruList = new ArrayList<>();
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            String query = "SELECT DISTINCT tanim FROM varyants WHERE parentid =" + parentid + " AND rowcell = " + rowcell + " ORDER BY id ASC";

            Cursor cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                do {
                    String grupTuru = cursor.getString(cursor.getColumnIndexOrThrow("tanim"));
                    grupTuruList.add(grupTuru);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (SQLException e) {
            Log.e("Varyants", "getAllGrupTurleri error: " + e.toString());
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return grupTuruList;
    }

    // parentid = 0 olan tüm varyantları çeken fonksiyon
    public List<VaryantModelWithBadget> getAllWithParentIdZero() {
        List<VaryantModelWithBadget> varyantList = new ArrayList<>();

        Database Db = new Database();
        SQLiteDatabase db = Db.getReadableDatabase();

        // Sorgu: parentid = 0 olan kayıtları seç
        String query = "SELECT * FROM varyants WHERE parentid = 0";
        Cursor cursor = db.rawQuery(query, null);

        try {
            // Cursor üzerinde döngü ile verileri oku
            if (cursor.moveToFirst()) {
                do {
                    // Her bir satırdan verileri al
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    int sira = cursor.getInt(cursor.getColumnIndexOrThrow("sira"));
                    String tanim = cursor.getString(cursor.getColumnIndexOrThrow("tanim"));
                    int rowcell = cursor.getInt(cursor.getColumnIndexOrThrow("rowcell"));
                    String aciklama = cursor.getString(cursor.getColumnIndexOrThrow("aciklama"));
                    int parentid = cursor.getInt(cursor.getColumnIndexOrThrow("parentid"));

                    // VaryantModelWithBadget nesnesi oluştur

                    //int id, int sira, String tanim, int rowcell, String aciklama, int parentid
                    VaryantModelWithBadget varyant = new VaryantModelWithBadget(id, sira, tanim, rowcell, aciklama, parentid, 0);
                    varyantList.add(varyant);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Veri çekme hatası: " + e.toString());
        } finally {
            cursor.close();
            db.close();
        }

        return varyantList;
    }

}
