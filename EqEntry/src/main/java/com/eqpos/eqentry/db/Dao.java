package com.eqpos.eqentry.db;

import android.content.ContentValues;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Created by dursu on 24.02.2018.
 */

public class Dao {
    private static Database Db;
    public static int LanguageId = 4;
    public static int CurrencyId = 1;
    public static String Language = "en";
    public static String Currency = "EUR";
    public static int groupButonSize = 300;
    public static int groupFontSize = 20;

    public static long getRowCount(String tableName) {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getReadableDatabase();
        return DatabaseUtils.queryNumEntries(db, tableName);
    }

    public static void saveFirmInfo(String name, String address, String phone, String taxid, String slogan) {
        if (Db == null) {
            Db = new Database();
        }

        ContentValues value = new ContentValues();
        value.put("firmname", name);
        value.put("firmaddress", address);
        value.put("firmphone", phone);
        value.put("firmtaxid", taxid);
        value.put("slogan", slogan);

        SQLiteDatabase db = Db.getWritableDatabase();

        try {
            db.insert("firm", null, value);
        } catch (SQLException e) {
            Log.e("Unite Save Error 1", e.toString());
        }
    }

    public static void saveFirmInfo(JsonArray value) {
        try {
            for (int i = 0; i < value.size(); i++) {
                JsonObject jData = value.get(i).getAsJsonObject();

                saveFirmInfo(jData.get("firmaadi").getAsString(),
                        jData.get("adres").getAsString(),
                        jData.get("telefon").getAsString(),
                        jData.get("verginumarasi").getAsString(),
                        jData.get("slogan").getAsString()
                        );
            }
            Log.e("saveFirmInfo", "saveFirmInfo: " + value.toString()) ;
        } catch (Exception e) {
            Log.e("Unite Save Error 2", e.toString());
        }
    }
}
