package com.eqpos.eqentry.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.eqpos.eqentry.models.AddedVaryantsModel;
import com.eqpos.eqentry.models.VaryantModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by dursu on 22.02.2018.
 */

public class Database extends SQLiteOpenHelper {
    public static final String VERITABANI = "entry_db_tr_13.db3";
    private static final int SURUM = 13;
    public static Context vtContext;

    public Database() {
        super(vtContext, VERITABANI, null, SURUM);
    }

    public Database(Context context) {
        super(context, VERITABANI, null, SURUM);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table unites (id integer primary key, unitename text)");
        db.execSQL("create table taxes (id integer primary key, taxname text," +
                "taxin numeric(12,2), taxout numeric(12,2))");
        db.execSQL("create table productgroups (id integer primary key, groupname text)");
        db.execSQL("create table deposites (id integer primary key, depositename text, " +
                "price numeric(12,2), taxpercent numeric(12,2), uniteid integer)");
        db.execSQL("CREATE TABLE products (id integer primary key, stockcode text, groupid integer, rownumber integer, " +
                "barcode text, plu text, variantcode text, productname text, costprice numeric(12,3), " +
                "taxid integer, depositeid integer, uniteid integer, ismanualprice integer, " +
                "stock numeric(12,3), isstockactive integer, unitamount numeric(12,3), " +
                "amountunite text, taraid integer, description text, ingredients text, " +
                "active integer, inputstock numeric(12,3), outputstock numeric(12,3), producttype integer, " +
                "criticalstock numeric(12,3), inputreturn numeric(12,3), " +
                "production numeric(12,3), returns numeric(12,3), wastages numeric(12,3), " +
                "consumption numeric(12,3), suppliers text, suppliersid integer, origin text, " +
                "changed integer default 0, isnew integer default 0, printlabel integer default 0, " +
                "varyant_anagrupid integer default 0, varyant_altgrupid integer default 0)");
        db.execSQL("create index idx_products_barcode on products(barcode)");


        //30.05.2025::erkan  ::  urunbarcodes
        Log.e("Database", "Creating urunbarcodes table");
        db.execSQL("CREATE TABLE urunbarcodes (id INTEGER PRIMARY KEY AUTOINCREMENT, urunid integer, barcod text, miktar text, birimid integer)");
        db.execSQL("create index idx_urunbarcodes_id on urunbarcodes(id)");
        db.execSQL("create index idx_urunbarcodes_barcod on urunbarcodes(barcod)");
        db.execSQL("create index idx_urunbarcodes_urunid on urunbarcodes(urunid)");
        Log.e("Database", "Creating urunbarcodes TAMAM");


        //18.08.2025::erkan  ::  Varyants
        Log.e("Database", "Creating varyants table");
        db.execSQL("create table varyants (id INTEGER PRIMARY KEY AUTOINCREMENT, sira integer, tanim text, rowcell integer, aciklama text, parentid integer)");
        db.execSQL("create index idx_varyants_id on varyants(id)");
        db.execSQL("create index idx_varyants_parentid on varyants(parentid)");


        Log.e("Database", "Creating varyants TAMAM");


        //18.08.2025::erkan  ::  Varyants
        Log.e("Database", "Creating varyantsadded table");
        db.execSQL("create table varyants_added (internid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id integer, " +
                "barcode text , " +
                "plu integer default 0 , " +
                "urunadi text, " +
                "price numeric(12,2), " +
                "anagrupid integer, " +
                "altgrupid integer)");





//        db.execSQL("CREATE TRIGGER if not exists trg_products before UPDATE ON products for each row  " +
//                "BEGIN " +
//                " update products set changed=1 where changed=NEW.changed and id=NEW.id; " +
//                "END;");
        /*Products table
            rownumber: urunsira
            costprice : alış fiyatı
            taxid : urunturid
            depositeid: depozitoid
            uniteid: birimid
            ismanualprice: fiyatsor
            stock: stok
            isstockactive: stoktut
            unitamount: birimmiktari
            amountunit: miktarbirimi
            ingredients: icindekiler
            inputstock: girdi
            outputstock: cikti
            producttype: uruntip
            criticalstock: kritikstok
            inputreturn: alisiadeler
            production: uretim
            origin : uretimyeri
            returns: iadeler
            wastages: fireler
            consumption: sarf
            suppliers: tedarikci
            suppliersid: tedarikciid
         */
        db.execSQL("create table productprices (id INTEGER PRIMARY KEY AUTOINCREMENT,  productid integer, priceorder integer, " +
                "description text, price numeric(12,2), newprice numeric(12,2) default 0, changed integer default 0, isnew integer default 0," +
                "printlabel integer default 0, foreign key (productid) references products(id))");

        db.execSQL("create unique index idx_productprices on productprices(productid,priceorder)");
        db.execSQL("CREATE TRIGGER if not exists trg_update_productrpice before UPDATE ON productprices for each row  " +
                "BEGIN " +
                " update productprices set changed=1 where id=NEW.id; " +
                "END;");

        db.execSQL("CREATE TABLE languages (id INTEGER PRIMARY KEY AUTOINCREMENT , langvalue TEXT);");

        db.execSQL("CREATE TABLE ayarlar (ayaradi TEXT PRIMARY KEY, strvalue TEXT, intvalue integer);");

        // lastbalace: sonbakiye ekledim, kasadaki veriyi kaybetmemek için
        db.execSQL("create table customers (id integer primary key, customername TEXT, code integer, phone1 text, phone2 text, phone3 text, " +
                "address1 text, address2 text, address3 text, credit numeric(12,2), debt numeric(12,2), balance numeric(12,2), " +
                "lastbalance numeric(12,2) default 0.0,email text, owner text," +
                "taxid text, taxoffice text, isnew int default 0)");


        db.execSQL("create table suppliers (id integer primary key, suppliername text, code integer)");

        db.execSQL("CREATE TABLE purchaseorder (id INTEGER Primary KEY Autoincrement, supplierid integer, productid integer not null, " +
                "amount numeric(12,3) default 0.0, unit text)");
        db.execSQL("create unique index idx_purchaseorder_productid on purchaseorder(productid)");

        db.execSQL("create table delivery (id integer primary key autoincrement, documentdate text, documenttime text, documentnumber text," +
                "supplierid integer, receiver text) ");

        db.execSQL("CREATE TABLE deliverydetail (id integer primary key autoincrement, deliveryid integer, productid integer not null, " +
                "partnumber text, expirationdate text, amount numeric(12,3) default 0.0, costprice numeric(12,2) default 0.0, " +
                "packageamount int, total numeric(12,2) default 0.0, foreign key (deliveryid) references delivery(id))");
        db.execSQL("create unique index idx_deliverydetail_productid on deliverydetail(productid)");
        db.execSQL("CREATE TRIGGER if not exists trg_deliverydetail before UPDATE ON deliverydetail for each row  " +
                "BEGIN " +
                " update deliverydetail set total=amount*costprice where id=NEW.id; " +
                "END;");
        db.execSQL("create table inventur (productid integer primary key, currentquantity numeric(12,3), newquantity numeric(12, 3), " +
                "difference numeric(12, 3), foreign key (productid) references products(id))");
        db.execSQL("CREATE TRIGGER if not exists trg_inventur before UPDATE ON inventur for each row  " +
                "BEGIN " +
                " update inventur set difference = newquantity - currentquantity where productid=NEW.productid; " +
                "END;");

        db.execSQL("create table invoice (id integer primary key autoincrement, invoicenumber text, invoicedate text, invoicetime text, customerid integer, " +
                "taxamount numeric(12, 2), total numeric(12,2), discount numeric(12,2), subtotal numeric(12,2), includetax int default 0, foreign key (customerid) references customers(id))");
        db.execSQL("create table invoicedetail(id integer primary key autoincrement, invoiceid integer, productid integer, unitprice numeric(12,2), " +
                "packageamount int, amount numeric(12,3), total numeric(12, 2), discount1 numeric(12, 2), discount2 numeric(12, 2), discount3 numeric(12, 2), " +
                "discount4 numeric(12, 2), subtotal numeric(12,2), taxrate numeric(12,2), tax numeric(12, 2),  " +
                "foreign key(productid) references products(id)," +
                "foreign key(invoiceid) references invoice(id)) ");
        db.execSQL("create index idx_invoicedetails_productid on invoicedetail(productid)");

        db.execSQL("CREATE TRIGGER if not exists trg_invoice before UPDATE ON invoice for each row  " +
                "BEGIN " +
                " update invoice set subtotal = total - discount where id=NEW.id; " +
                "END;");

        db.execSQL("create table collects (id integer primary key autoincrement, documentnumber text, collectdate text, collecttime text, customerid integer, " +
                "paymenttype integer, amount numeric(12,2), description text, foreign key (customerid) references customers(id))");


        db.execSQL("create table warehouses (id integer primary key, warehousename text)");
        db.execSQL("create table stocktransfer (id integer primary key autoincrement, documentnumber text," +
                "targetwareid integer, foreign key (targetwareid) references warehouses(id) ) ");

        db.execSQL("CREATE TABLE stocktransferdetail (id integer primary key autoincrement, stocktransferid integer, productid integer not null, " +
                "amount numeric(12,3) default 0.0, packageamount int, foreign key (stocktransferid) references stocktransfer(id))");
        db.execSQL("create unique index idx_stocktransferdetail_productid on stocktransferdetail(productid)");

        db.execSQL("create table firm (firmname text, firmaddress text, firmphone text, firmtaxid text, slogan text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE IF EXISTS unites");//eğer ki versiyon değiştiyse tabloyu drop et yani kaldır
//        db.execSQL("DROP TABLE IF EXISTS taxes;");
//        db.execSQL("DROP TABLE IF EXISTS productgroups;");
//        db.execSQL("DROP TABLE IF EXISTS deposites;");
//        db.execSQL("DROP TABLE IF EXISTS delivery;");
//        db.execSQL("DROP TABLE IF EXISTS deliverydetail;");
//        db.execSQL("DROP TABLE IF EXISTS products;");
//        db.execSQL("DROP TABLE IF EXISTS productprices;");
//        db.execSQL("DROP TABLE IF EXISTS purchaseorder;");
//        db.execSQL("DROP TABLE IF EXISTS ayarlar;");
//        db.execSQL("DROP TABLE IF EXISTS suppliers;");
//        db.execSQL("DROP TABLE IF EXISTS inventur;");
//        db.execSQL("DROP TABLE IF EXISTS invoice;");
//        db.execSQL("DROP TABLE IF EXISTS invoicedetail;");

//        onCreate(db);//tekrar tablo olştur..
        //Böylece tablomuz sıfırlanacak..Eğer ki veritabanında hata yaptıysanız ve bilgileri tek tek silmek istemiyosanız Database fonk. versiyon sayısını değiştirerel tabloyu silebilirisiniz..
        if (newVersion <= 2) {
            try {
                db.execSQL("alter table invoicedetail add column packageamount int default 0");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (newVersion <= 3) {
            try {
                db.execSQL("alter table invoice add column includetax int default 0");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }


    // LiveData için
    private final MutableLiveData<List<VaryantModel>> varyantsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<VaryantModel>> varyantsGruplarLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<VaryantModel>> varyantsAltGruplarLiveData = new MutableLiveData<>();

    private final MutableLiveData<List<AddedVaryantsModel>> addedVaryantsLiveData = new MutableLiveData<>();

    // Tüm eklenen varyantları LiveData olarak döndür
    public LiveData<List<AddedVaryantsModel>> getAllAddedVaryantsLiveData() {
        loadAddedVaryants(); // İlk yükleme
        return addedVaryantsLiveData;
    }




    // Tüm varyantları LiveData olarak döndür
    public LiveData<List<VaryantModel>> getAllVaryantsLiveData() {
        loadVaryants(); // İlk yükleme
        return varyantsLiveData;
    }

//
    public LiveData<List<VaryantModel>> getAllVaryantGruplarLiveData(int parentId) {
        loadVaryantsGruplar(parentId);
        return varyantsGruplarLiveData;
    }

    public LiveData<List<VaryantModel>> getAllVaryantAltGruplarLiveData(int parentId) {
        loadVaryantsAltGruplar(parentId);
        return varyantsAltGruplarLiveData;
    }


    private void loadVaryantsGruplar(int parentId) {
        List<VaryantModel> varyantList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM varyants WHERE rowcell= 1 AND parentid = "+ parentId +";"; // parentid'ye göre filtreleme yap
        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    int sira = cursor.getInt(cursor.getColumnIndexOrThrow("sira"));
                    String tanim = cursor.getString(cursor.getColumnIndexOrThrow("tanim"));
                    String aciklama = cursor.getString(cursor.getColumnIndexOrThrow("aciklama"));
                    int rowcell = cursor.getInt(cursor.getColumnIndexOrThrow("rowcell"));
                    int parentid = cursor.getInt(cursor.getColumnIndexOrThrow("parentid"));

                    //int id, int sira, String tanim, int rowcell, String aciklama, int parentid
                    VaryantModel varyant = new VaryantModel(id, sira, tanim, rowcell,aciklama, parentid);
                    varyantList.add(varyant);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Veri çekme hatası: " + e.toString());
        } finally {
            cursor.close();
            db.close();
        }
        varyantsGruplarLiveData.postValue(varyantList); // LiveData'yı güncelle
    }
    private void loadVaryantsAltGruplar(int parentId) {
        List<VaryantModel> varyantList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM varyants WHERE rowcell= 2 AND parentid = "+ parentId +";"; // parentid'ye göre filtreleme yap
        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    int sira = cursor.getInt(cursor.getColumnIndexOrThrow("sira"));
                    String tanim = cursor.getString(cursor.getColumnIndexOrThrow("tanim"));
                    String aciklama = cursor.getString(cursor.getColumnIndexOrThrow("aciklama"));
                    int rowcell = cursor.getInt(cursor.getColumnIndexOrThrow("rowcell"));
                    int parentid = cursor.getInt(cursor.getColumnIndexOrThrow("parentid"));

                    //int id, int sira, String tanim, int rowcell, String aciklama, int parentid
                    VaryantModel varyant = new VaryantModel(id, sira, tanim, rowcell,aciklama, parentid);
                    varyantList.add(varyant);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Veri çekme hatası: " + e.toString());
        } finally {
            cursor.close();
            db.close();
        }
        varyantsAltGruplarLiveData.postValue(varyantList); // LiveData'yı güncelle
    }

    // Verileri yükle ve LiveData'yı güncelle
    private void loadVaryants() {
        List<VaryantModel> varyantList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM varyants WHERE parentid = 0;";
        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    int sira = cursor.getInt(cursor.getColumnIndexOrThrow("sira"));
                    String tanim = cursor.getString(cursor.getColumnIndexOrThrow("tanim"));
                    int rowcell = cursor.getInt(cursor.getColumnIndexOrThrow("rowcell"));
                    String aciklama = cursor.getString(cursor.getColumnIndexOrThrow("aciklama"));
                    int parentid = cursor.getInt(cursor.getColumnIndexOrThrow("parentid"));

                    //int id, int sira, String tanim, int rowcell, String aciklama, int parentid
                    VaryantModel varyant = new VaryantModel(id, sira, tanim, rowcell, aciklama, parentid);
                    varyantList.add(varyant);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Veri çekme hatası: " + e.toString());
        } finally {
            cursor.close();
            db.close();
        }
        varyantsLiveData.postValue(varyantList); // LiveData'yı güncelle
    }

    private void loadAddedVaryants() {
        List<AddedVaryantsModel> varyantList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM varyants_added ORDER BY id ASC;";
        Cursor cursor = db.rawQuery(query, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String barcode = cursor.getString(cursor.getColumnIndexOrThrow("barcode"));
                    int plu = cursor.getInt(cursor.getColumnIndexOrThrow("plu"));
                    String urunadi = cursor.getString(cursor.getColumnIndexOrThrow("urunadi"));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                    int anagrupid = cursor.getInt(cursor.getColumnIndexOrThrow("anagrupid"));
                    int altgrupid = cursor.getInt(cursor.getColumnIndexOrThrow("altgrupid"));

                    //int id, int sira, String tanim, int rowcell, String aciklama, int parentid
                    AddedVaryantsModel varyant = new AddedVaryantsModel(id, barcode, plu, urunadi, price, anagrupid, altgrupid);
                    varyantList.add(varyant);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Veri çekme hatası: " + e.toString());
        } finally {
            cursor.close();
            db.close();
        }
        addedVaryantsLiveData.postValue(varyantList); // LiveData'yı güncelle
    }





    // Yeni varyant ekle ve LiveData'yı güncelle
    public void addNewMainVaryant(String aciklama, int sira) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();


        //int id, int sira, String tanim, int rowcell, String aciklama, int parentid
        values.put("sira", sira);
        values.put("tanim", "VARYASYON");
        values.put("aciklama", aciklama);
        values.put("rowcell", 0);
        values.put("parentid", 0);

        db.insert("varyants", null, values);
        db.close();

        // Yeni veri eklendikten sonra LiveData'yı güncelle
        loadVaryants();
    }

    // Ortadaki Grup
    public void addNewGroupVaryant(String tanim, int sira, String aciklama, int parentid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();


        values.put("sira", sira);
        values.put("tanim", tanim);
        values.put("aciklama", aciklama);
        values.put("rowcell", 1);
        values.put("parentid", parentid);

        db.insert("varyants", null, values);
        db.close();

        // Yeni veri eklendikten sonra LiveData'yı güncelle
        loadVaryantsGruplar(parentid); // parentid'ye göre güncelleme yapılıyor
    }


    // Alt Grup
    public void addNewAltGroupVaryant(String tanim, int sira, String aciklama, int parentid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();


        values.put("sira", sira);
        values.put("tanim", tanim);
        values.put("aciklama", aciklama);
        values.put("rowcell", 2);
        values.put("parentid", parentid);


        db.insert("varyants", null, values);
        db.close();

        // Yeni veri eklendikten sonra LiveData'yı güncelle
        loadVaryantsAltGruplar(parentid); // parentid'ye göre güncelleme yapılıyor
    }


    // Yeni seçilen varyantı ekle ve LiveData'yı güncelle
    public void addNewSelected(int id, String barcode, String urunadi, double price, int anagrupid, int altgrupid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("barcode", barcode);
        values.put("urunadi", urunadi);
        values.put("price", price);
        values.put("anagrupid", anagrupid);
        values.put("altgrupid", altgrupid);

        db.beginTransaction();
        db.insert("varyants_added", null, values);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        // Yeni veri eklendikten sonra LiveData'yı güncelle
        loadAddedVaryants();
    }

    public void addNewAddedSelected(int id, String barcode, String urunadi, double price, int anagrupid, int altgrupid, int plu) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("barcode", barcode);
        values.put("plu", plu);
        values.put("urunadi", urunadi);
        values.put("price", price);
        values.put("anagrupid", anagrupid);
        values.put("altgrupid", altgrupid);

        db.beginTransaction();
        db.insert("varyants_added", null, values);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

    }

    public void deleteAddedSelected(String urunAdi) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();
        db.delete("varyants_added", "urunadi = ?", new String[]{urunAdi});
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        // Veri silindikten sonra LiveData'yı güncelle
        loadAddedVaryants();
    }

    public void updateAddedVaryant(String urunadi, String barcode, int plu) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("plu", plu);
        values.put("barcode", barcode);
        db.beginTransaction();
        db.update("varyants_added", values, "urunadi= ?", new String[]{urunadi});
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        // Veri güncellendikten sonra LiveData'yı güncelle
        loadAddedVaryants();
    }



}
