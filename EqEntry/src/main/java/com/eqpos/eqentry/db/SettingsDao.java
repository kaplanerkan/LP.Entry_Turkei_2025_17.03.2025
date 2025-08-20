package com.eqpos.eqentry.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.eqpos.eqentry.models.Ayar;

/**
 * Created by dursu on 23.02.2018.
 * Bu classda ayarlar tablosundan ayar okuma ve yazma methodları olacak
 */

public class SettingsDao {
    public static Database vt;

    private static boolean getAyarVarmi(String ayarAdi) {
        if (vt==null)
            vt = new Database();

        boolean value = false;

        SQLiteDatabase db = vt.getReadableDatabase();

        Cursor sonuc = db.query("ayarlar",new String[]{"ayaradi"}, "ayaradi=?", new String[]{ayarAdi}, null, null, null);


        if (sonuc.getCount() > 0) {
            value = true;
        }
        sonuc.close();
        return value;
    }

    private static void ayarKaydet(String ayarAdi, ContentValues values) {
        if (vt==null)
            vt = new Database();

        SQLiteDatabase db = vt.getWritableDatabase();

        db.delete("ayarlar", "ayaradi=?", new String[] {ayarAdi});
        db.insert("ayarlar", null, values);

        db.close();
    }

    public static void setVeritabani(Database veritabani) {
        SettingsDao.vt = veritabani;
    }

    public static int getIntValue(String ayarAdi) {
        if (vt==null)
            vt = new Database();

        int value = -1;
        SQLiteDatabase db = vt.getReadableDatabase();

        //tabloadi, geri donecek fieldler, where alanindaki sartlar, ? yerine gelecek parametreler, group, having, order
        Cursor sonuc = db.query("ayarlar", new String[]{"intvalue"}, "ayaradi=?", new String[]{ayarAdi}, null, null, null);

        if (sonuc.getCount() > 0) {
            sonuc.moveToFirst();
            value = sonuc.getInt(sonuc.getColumnIndexOrThrow("intvalue"));
        }

        sonuc.close();
        return value;
    }

    public static int getIntValue(String ayarAdi, int defalutValue) {
        if (vt==null)
            vt = new Database();

        int value = defalutValue;
        SQLiteDatabase db = vt.getReadableDatabase();

        //tabloadi, geri donecek fieldler, where alanindaki sartlar, ? yerine gelecek parametreler, group, having, order
        Cursor sonuc = db.query("ayarlar", new String[]{"intvalue"}, "ayaradi=?", new String[]{ayarAdi}, null, null, null);

        if (sonuc.getCount() > 0) {
            sonuc.moveToFirst();
            value = sonuc.getInt(sonuc.getColumnIndexOrThrow("intvalue"));
        }

        sonuc.close();
        return value;
    }

    public static String getStrValue(String ayarAdi) {
        if (vt==null)
            vt = new Database();

        String value = "";

        SQLiteDatabase db = vt.getReadableDatabase();

        //tabloadi, geri donecek fieldler, where alanindaki sartlar, ? yerine gelecek parametreler, group, having, order
        Cursor sonuc = db.query("ayarlar", new String[]{"strvalue"}, "ayaradi=?", new String[]{ayarAdi}, null, null, null);

        if (sonuc.getCount() > 0) {
            sonuc.moveToFirst();
            value = sonuc.getString(sonuc.getColumnIndexOrThrow("strvalue"));
        }

        sonuc.close();
        return value;
    }

    public static boolean getBoolValue(String ayarAdi, boolean defaultValue) {
        if (vt==null)
            vt = new Database();

        boolean value = defaultValue;

        SQLiteDatabase db = vt.getReadableDatabase();

        //tabloadi, geri donecek fieldler, where alanindaki sartlar, ? yerine gelecek parametreler, group, having, order
        Cursor sonuc = db.query("ayarlar", new String[]{"intvalue"}, "ayaradi=?", new String[]{ayarAdi}, null, null, null);


        if (sonuc.getCount() > 0) {
            sonuc.moveToFirst();
            if (sonuc.getInt(sonuc.getColumnIndexOrThrow("intvalue")) > 0 )
                value = true;
        }

        sonuc.close();
        return value;
    }

    public Ayar getAyar(String ayarAdi) {
        if (vt==null)
            vt = new Database();

        SQLiteDatabase db = vt.getReadableDatabase();
        Ayar ayar = new Ayar();

        Cursor sonuc = db.rawQuery("select * from ayarlar where ayaradi=?", new String[]{ayarAdi});

        if (sonuc.getCount() > 0) {
            sonuc.moveToFirst();

            ayar.setAyarAdi(ayarAdi);
            ayar.setIntValue(sonuc.getInt(sonuc.getColumnIndexOrThrow("intvalue")));
            ayar.setStrValue(sonuc.getString(sonuc.getColumnIndexOrThrow("strvalue")));
        }else {
            ayar=null;
        }

        sonuc.close();

        return ayar;
    }

    public void setAyar(Ayar ayar) {
        if (vt==null)
            vt = new Database();

        SQLiteDatabase db = vt.getWritableDatabase();

        if (getAyarVarmi(ayar.getAyarAdi())) {
            ContentValues values = new ContentValues();
            values.put("intvalue", ayar.getIntValue());
            values.put("strvalue", ayar.getStrValue());

            db.update("ayarlar", values, "ayaradi=?", new String[] {ayar.getAyarAdi()});
        }else {
            ContentValues values = new ContentValues();
            values.put("ayaradi", ayar.getAyarAdi());
            values.put("intvalue", ayar.getIntValue());
            values.put("strvalue", ayar.getStrValue());

            db.insert("ayarlar", null, values);
        }

        db.close();
    }

    public static void setIntValue(String ayarAdi, int value) {
        ContentValues values = new ContentValues();
        values.put("ayaradi", ayarAdi);
        values.put("intvalue", value);

        ayarKaydet(ayarAdi, values);
    }

    public static void setStrValue(String ayarAdi, String value) {
        ContentValues values = new ContentValues();
        values.put("ayaradi", ayarAdi);
        values.put("strvalue", value);

        ayarKaydet(ayarAdi, values);
    }

    public static void setBoolValue(String ayarAdi, boolean value) {
        ContentValues values = new ContentValues();
        values.put("ayaradi", ayarAdi);
        values.put("boolvalue", value);

        ayarKaydet(ayarAdi, values);
    }
}
