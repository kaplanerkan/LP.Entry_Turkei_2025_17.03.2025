package com.eqpos.eqentry.DB;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by dursu on 22.02.2018.
 */

public class Database extends SQLiteOpenHelper {
    public static final String VERITABANI = "eqentrydb.db3";
    private static final int SURUM = 6;
    public static Context vtContext;

    public Database() {
        super(vtContext, VERITABANI, null, SURUM);
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
                "changed integer default 0, isnew integer default 0, printlabel integer default 0)");
        db.execSQL("create index idx_products_barcode on products(barcode)");
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
}
