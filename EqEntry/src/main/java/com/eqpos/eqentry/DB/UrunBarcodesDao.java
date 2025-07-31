package com.eqpos.eqentry.DB;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class UrunBarcodesDao {

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
            db.delete("urunbarcodes", null, null); // Tablo adını kontrol edin
        } catch (SQLException e) {
            Log.e("UrunBarcodesDao", "deleteAll error: " + e.toString());
        }
        // Not: db.close() çağrısı, Database sınıfında uygun şekilde yönetiliyorsa gerekli olmayabilir
    }


    public static boolean saveUrunBarcodes(JsonArray value) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.beginTransaction();

            for (int i = 0; i < value.size(); i++) {
                JsonObject jData = value.get(i).getAsJsonObject();

                // Null ve tür kontrolleri
                if (jData.has("urunid") && jData.has("barkod") && jData.has("miktar") && jData.has("birimid")) {
                    saveUrunBarcodes(
                            db,
                            jData.get("urunid").getAsInt(),
                            jData.get("barkod").getAsString(),
                            jData.get("miktar").getAsString(),
                            jData.get("birimid").getAsInt()
                    );
                } else {
                    Log.e("UrunBarcodesDao", "Missing fields in JSON at index " + i);
                }
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e("UrunBarcodesDao", "saveUrunBarcodes error: " + e.toString());
            return false;
        } finally {
            if (db != null && db.inTransaction()) {
                db.endTransaction();
            }
        }
    }

    private static void saveUrunBarcodes(SQLiteDatabase db, int urunid, String barkod, String miktar, int birimid) {
        try {
            ContentValues value = new ContentValues();
            value.put("urunid", urunid);
            value.put("barcod", barkod);
            value.put("miktar", miktar);
            value.put("birimid", birimid);

            db.insert("urunbarcodes", null, value); // Tablo adını doğrulayın: urunbarcodes mu, productprices mi?
        } catch (SQLException e) {
            Log.e("UrunBarcodesDao", "saveUrunBarcodes error: " + e.toString());
            throw e; // Hata çağrıcıya iletilsin
        }
    }

}
