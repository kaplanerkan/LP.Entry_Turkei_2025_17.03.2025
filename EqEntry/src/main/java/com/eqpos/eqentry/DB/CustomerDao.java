package com.eqpos.eqentry.DB;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.opengl.GLES30;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.annotation.Nullable;
import android.util.Log;

import com.eqpos.eqentry.Models.Collect;
import com.eqpos.eqentry.Models.Customer;
import com.eqpos.eqentry.R;
import com.eqpos.eqentry.tools.Variables;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dursu on 20.11.2018.
 */

public class CustomerDao {
    private static Database Db;



    public static void saveSupplier(int id, String suppliername, int code) {
        if (Db == null) {
            Db = new Database();
        }

        ContentValues value = new ContentValues();
        value.put("id", id);
        value.put("suppliername", suppliername);
        value.put("code", code);

        SQLiteDatabase db = Db.getWritableDatabase();

        try {
            db.insert("suppliers", null, value);
        } catch (SQLException e) {
            Log.e("Supplier Save Error 1", e.toString());
        }
    }

    public static void saveSupplier(JsonArray value) {
        try {
            for (int i = 0; i < value.size(); i++) {
                JsonObject jData = value.get(i).getAsJsonObject();

                saveSupplier(jData.get("id").getAsInt(), jData.get("adsoyad").getAsString(), jData.get("kod").getAsInt());
            }
        } catch (Exception e) {
            Log.e("supplier Save Error 2", e.toString());
        }
    }


    public static void saveCustomer(long id, String customername, int code, String phone1, @Nullable String phone2, @Nullable String phone3,
                                    String address1, @Nullable String address2, @Nullable String address3, double credit, double debt, double balance,
                                    String email, @Nullable String taxId, @Nullable String taxOffice, int isnew) { //NHT
        if (Db == null) {
            Db = new Database();
        }

        long lId = id;
        if (id == 0) {
            SQLiteDatabase dbr = Db.getReadableDatabase();
            Cursor cursor = dbr.rawQuery("select max(id) as maxid from customers", null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                lId = cursor.getInt(cursor.getColumnIndexOrThrow("maxid")) + 1;
            }
        }

        ContentValues value = new ContentValues();
        value.put("id", lId);
        value.put("customername", customername);
        if (code > 0) {
            value.put("code", code);
        }
        value.put("phone1", phone1);
        value.put("address1", address1);
        value.put("credit", credit);
        value.put("debt", debt);
        value.put("balance", balance);
        value.put("email", email);
        value.put("taxid", taxId);
        value.put("taxoffice", taxOffice);
        value.put("isnew", isnew);

        SQLiteDatabase db = Db.getWritableDatabase();

        try {
            if (isnew == 1 && id > 0) {
                db.update("customers", value, "id=?", new String[]{String.valueOf(id)});
            } else {
                db.insert("customers", null, value);
            }
        } catch (SQLException e) {
            Log.e("Customer Save Error 1", e.toString());
        }
    }



    public static void saveCustomerErkan(long id, String customername, int code, String phone1, @Nullable String phone2, @Nullable String phone3,
                                    String address1, @Nullable String address2, @Nullable String address3, double credit, double debt, double balance,
                                    String email, @Nullable String taxId, @Nullable String taxOffice, int isnew, double _lastBalance) { //NHT
        if (Db == null) {
            Db = new Database();
        }

        long lId = id;
//        if (id == 0) {
//            SQLiteDatabase dbr = Db.getReadableDatabase();
//            Cursor cursor = dbr.rawQuery("select max(id) as maxid from customers", null);
//            if (cursor.getCount() > 0) {
//                cursor.moveToFirst();
//                lId = cursor.getInt(cursor.getColumnIndexOrThrow("maxid")) + 1;
//            }
//        }

        ContentValues value = new ContentValues();
        value.put("id", lId);
        value.put("customername", customername);
        if (code > 0) {
            value.put("code", code);
        }
        value.put("phone1", phone1);
        value.put("address1", address1);
        value.put("credit", credit);
        value.put("debt", debt);
        value.put("balance", balance);
        value.put("email", email);
        value.put("taxid", taxId);
        value.put("taxoffice", taxOffice);
        value.put("isnew", isnew);
        value.put("lastbalance", _lastBalance);

        SQLiteDatabase db = Db.getWritableDatabase();

        Log.e("Hata", "saveCustomerErkan: " + value.toString() );
        try {
            if (isnew == 1 && id > 0) {
                db.update("customers", value, "id=?", new String[]{String.valueOf(id)});
            } else {
                db.insert("customers", null, value);
            }
        } catch (SQLException e) {
            Log.e("Customer Save Error 1", e.toString());
        }
    }








    public static void saveCustomer(JsonArray value) {
        try {

            int lCode = -1;
            for (int i = 0; i < value.size(); i++) {
                JsonObject jData = value.get(i).getAsJsonObject();

                if (jData.get("kod").isJsonNull())
                    lCode = -1;
                else
                    lCode = jData.get("kod").getAsInt();

                Variables.isTurkey = jData.has("vergino"); // Gelen JSON da vergi no varsa Türkiyedir. NHT

                int id = jData.get("id").isJsonPrimitive() ? jData.get("id").getAsInt() : -2;
                String adsoyad = jData.get("adsoyad").isJsonPrimitive() ? jData.get("adsoyad").getAsString() : "";
                String tel1 = jData.get("tel1").isJsonPrimitive() ? jData.get("tel1").getAsString() : "";
                String tel2 = jData.has("tel2")? jData.get("tel2").getAsString() : null;
                String tel3 = jData.has("tel3") ? jData.get("tel3").getAsString() : null;
                String adres1 = jData.get("adres1").isJsonPrimitive() ? jData.get("adres1").getAsString() : "";
                String adres2 = jData.has("adres2") ? jData.get("adres2").getAsString() : null;
                String adres3 = jData.has("adres3") ? jData.get("adres3").getAsString() : null;
                double alacak = jData.get("alacak").isJsonPrimitive() ? jData.get("alacak").getAsDouble() : 0;
                double borc = jData.get("borc").isJsonPrimitive() ? jData.get("borc").getAsDouble() : 0;

                double bakiye = 0.0 ; // jData.get("bakiye").isJsonPrimitive() ? jData.get("bakiye").getAsDouble() : 0;

                double lastBakiye = jData.get("bakiye").isJsonPrimitive() ? jData.get("bakiye").getAsDouble() : 0;
                String email = jData.get("email").isJsonPrimitive() ? jData.get("email").getAsString() : "";
                String vergino = jData.has("vergino") ? jData.get("vergino").getAsString() : null;
                String vergidairesi = jData.has("vergidairesi") ? jData.get("vergidairesi").getAsString() : null;


                saveCustomerErkan(
                        id,
                        adsoyad,
                        lCode,
                        tel1,
                        tel2,
                        tel3,
                        adres1,
                        adres2,
                        adres3,
                        alacak,
                        borc,
                        bakiye,
                        email,
                        vergino,
                        vergidairesi,
                        0,
                        lastBakiye);

            }
        } catch (Exception e) {
            Log.e("supplier Save Error 2", e.toString());
        }
    }

    public static ArrayList<HashMap<String, String>> getCustomerList(String search) {
        if (Db == null)
            Db = new Database();

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map;
        SQLiteDatabase db = Db.getReadableDatabase();

        Cursor cursor = db.rawQuery("select id, customername, balance, isnew, lastbalance from customers where customername like ?",
                new String[] {"%" + search + "%"});// where customername like ? order by customername",
        //       new String[]{ "%"+search+"%"});
        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                map = new HashMap<String, String>();
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                map.put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")));
                map.put("customername", cursor.getString(cursor.getColumnIndexOrThrow("customername")));
                double balance = cursor.getDouble(cursor.getColumnIndexOrThrow("balance"));
                map.put("balance", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("balance")),2));
                double faturaToplam = cursor.getDouble(cursor.getColumnIndexOrThrow("lastbalance")) ;
                map.put("lastbalance", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("lastbalance")),2));
                map.put("isnew", cursor.getString(cursor.getColumnIndexOrThrow("isnew")));

                double discount = getCustomerDiscount(cursor.getInt(cursor.getColumnIndexOrThrow("id")));  // 1375.00
                double subtotal = getCustomerSubtotal(cursor.getInt(cursor.getColumnIndexOrThrow("id")));  // 3500.00
                map.put("subtotal", Variables.doubleToStr(subtotal,2));
                map.put("discount", Variables.doubleToStr(discount,2));

                double yeniToplamBalance = faturaToplam + balance ;
                map.put("lblYeniBakiye", Variables.doubleToStr(yeniToplamBalance,2));
                list.add(map);
            }
            cursor.close();
        }
        return list;
    }

    // Intern kullanicam, porivate
    private static double getCustomerSubtotal(long prCustomerId) {
        if (Db == null)
            Db = new Database();

        SQLiteDatabase db = Db.getReadableDatabase();
        double result = 0.0;
        Cursor cursor = db.rawQuery("select sum(total) as total from invoice where customerid=?", new String[]{String.valueOf(prCustomerId)});
        if (cursor.getCount() > 0) {

            cursor.moveToNext();
            result = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
        }

        cursor.close();
        return result;
    }

    // Intern kullanicam, privat
    private static double getCustomerDiscount(long prCustomerId) {
        if (Db == null)
            Db = new Database();

        SQLiteDatabase db = Db.getReadableDatabase();
        double result = 0.0;
        Cursor cursor = db.rawQuery("select sum(discount) as discount from invoice where customerid=?", new String[]{String.valueOf(prCustomerId)});
        if (cursor.getCount() > 0) {

            cursor.moveToNext();
            result = cursor.getDouble(cursor.getColumnIndexOrThrow("discount"));
        }

        cursor.close();
        return result;
    }



    public static ArrayList<HashMap<String, String>> getSupplierList(String search) {
        if (Db == null)
            Db = new Database();

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map;
        SQLiteDatabase db = Db.getReadableDatabase();

        Cursor cursor = db.rawQuery("select id, suppliername, code from suppliers where suppliername like ?",
                new String[] {"%" + search + "%"});// where customername like ? order by customername",
        //       new String[]{ "%"+search+"%"});
        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                map = new HashMap<String, String>();
                map.put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")));
                map.put("suppliername", cursor.getString(cursor.getColumnIndexOrThrow("suppliername")));
                map.put("code", cursor.getString(cursor.getColumnIndexOrThrow("code")));

                list.add(map);
            }
            cursor.close();
        }
        return list;
    }

    public static int getProductSupplierId(long prProductId) {
        if (Db == null)
            Db = new Database();

        SQLiteDatabase db = Db.getReadableDatabase();
        int result = 0;
        Cursor cursor = db.rawQuery("select suppliersid from products where id=?", new String[]{String.valueOf(prProductId)});
        if (cursor.getCount() > 0) {

            cursor.moveToNext();
            result = cursor.getInt(cursor.getColumnIndexOrThrow("suppliersid"));
        }
        cursor.close();
        return result;
    }

    public static double getCustomerBalance(long prCustomerId) {
        if (Db == null)
            Db = new Database();

        SQLiteDatabase db = Db.getReadableDatabase();
        double result = 0.0;
        Cursor cursor = db.rawQuery("select balance from customers where id=?", new String[]{String.valueOf(prCustomerId)});
        if (cursor.getCount() > 0) {

            cursor.moveToNext();
            result = cursor.getDouble(cursor.getColumnIndexOrThrow("balance"));
        }

        cursor.close();
        return result;
    }

    public static double getCustomerNewBalance(long prCustomerId) {
        if (Db == null)
            Db = new Database();

        SQLiteDatabase db = Db.getReadableDatabase();
        double result = getCustomerBalance(prCustomerId);  //  3500


        Cursor cursor = db.rawQuery("select sum(amount) as total from collects where customerid=?", new String[]{String.valueOf(prCustomerId)});
        if (cursor.getCount() > 0) {

            cursor.moveToNext();
            result = result - cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
        }

        cursor.close();
        return result;
    }


    public static double getCustomerLastBalance(long prCustomerId) {
        if (Db == null)
            Db = new Database();

        SQLiteDatabase db = Db.getReadableDatabase();
        //double result = getCustomerBalance(prCustomerId);  //  3500
        double result = 0.0;

        Cursor cursor = db.rawQuery("select lastbalance as total from customers where id=?", new String[]{String.valueOf(prCustomerId)});
        if (cursor.getCount() > 0) {

            cursor.moveToNext();
            result = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
        }

        cursor.close();
        return result;
    }



    public static double getCustomerNewBalance2(long prCustomerId) {
        if (Db == null)
            Db = new Database();

        SQLiteDatabase db = Db.getReadableDatabase();
        double result = getCustomerBalance(prCustomerId);  //  3500


// ERKAN:  28.06.2024
//
//        Cursor cursor = db.rawQuery("select sum(amount) as total from collects where customerid=?", new String[]{String.valueOf(prCustomerId)});
//        if (cursor.getCount() > 0) {
//
//            cursor.moveToNext();
//            result = result - cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
//        }
//
//        cursor.close();
        return result;
    }




    public static double getCustomerTotalCollects(long prCustomerId) {
        if (Db == null)
            Db = new Database();

        SQLiteDatabase db = Db.getReadableDatabase();
        double result = 0.0;
        Cursor cursor = db.rawQuery("select sum(amount) as total from collects where customerid=?", new String[]{String.valueOf(prCustomerId)});
        if (cursor.getCount() > 0) {

            cursor.moveToNext();
            result = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
        }

        cursor.close();
        return result;
    }

    public static Customer getCustomer(long prCustomerId) {
        if (Db == null)
            Db = new Database();

        Customer cus = new Customer();

        SQLiteDatabase db = Db.getReadableDatabase();

        Cursor cursor = db.rawQuery("select id, code, customername, phone1, phone2, address1, email, taxid, taxoffice, balance, isnew, lastbalance from customers cus " +
                "where cus.id=?", new String[]{String.valueOf(prCustomerId)});
        //       new String[]{ "%"+search+"%"});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            cus.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            cus.setCode(cursor.getInt(cursor.getColumnIndexOrThrow("code")));
            cus.setName(cursor.getString(cursor.getColumnIndexOrThrow("customername")));
            cus.setPhone1(cursor.getString(cursor.getColumnIndexOrThrow("phone1")));
            cus.setPhone2(cursor.getString(cursor.getColumnIndexOrThrow("phone2")));
            cus.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address1")));
            cus.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            cus.setTaxId(cursor.getString(cursor.getColumnIndexOrThrow("taxid")));
            cus.setTaxOffice(cursor.getString(cursor.getColumnIndexOrThrow("taxoffice")));
            cus.setIsNew(cursor.getInt(cursor.getColumnIndexOrThrow("isnew")));
            cus.setOldBalance(cursor.getDouble(cursor.getColumnIndexOrThrow("lastbalance")));
            cus.setNewBalance(getCustomerNewBalance(prCustomerId));
            cus.setCollects(getCustomerTotalCollects(prCustomerId));

            cus.setBalance(cursor.getDouble(cursor.getColumnIndexOrThrow("balance")));          // 01.08.2025 ERKAN

        }
        return cus;
    }

    public static void deleteCollect(int prId) {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor = db.rawQuery("select customerid, amount from collects where id=?", new String[]{String.valueOf(prId)});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            long lCustomerId = cursor.getInt(0);
            double lTotal = cursor.getDouble(1);


            changeCustomerBalance(lCustomerId, lTotal);
        }

        db = Db.getWritableDatabase();

        try {
            db.delete("collects", "id=?", new String[]{String.valueOf(prId)});
        } catch (SQLException e) {
            Log.e("Collect Save Error 1", e.toString());
        }

    }

    public static void addCollect(int prId, long prCustomerId, String prDocumentNumber, String prDocumentDate, int prPaymentType,
                                  double prAmount, String prDescription, double yeniBakiye) {

        if (prId > 0 && prAmount == 0.0) {
            deleteCollect(prId);
            return;
        } else if (prId == 0 && prAmount == 0.0) {
            return;
        }

        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getReadableDatabase();
        if (prId > 0) {
            Cursor cursor = db.rawQuery("select amount from collects where id=?", new String[]{String.valueOf(prId)});
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                double lAmount = cursor.getDouble(0);
                changeCustomerBalance(prCustomerId, lAmount);
            }
        }

        ContentValues value = new ContentValues();
        value.put("documentnumber", prDocumentNumber);
        value.put("collectdate", prDocumentDate);
        value.put("collecttime", Variables.getCurrentTime());
        value.put("customerid", prCustomerId);
        value.put("paymenttype", prPaymentType);
        value.put("amount", prAmount);
        value.put("description", prDescription);

        db = Db.getWritableDatabase();

        try {
            if (prId > 0)
                db.update("collects", value, "id=?", new String[]{String.valueOf(prId)});
            else
                db.insert("collects", null, value);

            changeCustomerBalanceErkan(prCustomerId, -1* prAmount, yeniBakiye);
        } catch (SQLException e) {
            Log.e("Collect Save Error 1", e.toString());
        }
    }


    public static ArrayList<HashMap<String, String>> getCollectList(long prCustomerId) {
        if (Db == null)
            Db = new Database();

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map;
        SQLiteDatabase db = Db.getReadableDatabase();

        Cursor cursor;
        try {
            if (prCustomerId > 0) {
                cursor = db.rawQuery("select col.id, cus.customername, cus.balance, col.documentnumber, " +
                                "col.collectdate, col.customerid, col.paymenttype, col.amount, col.description from collects as col " +
                                "inner join customers as cus on col.customerid=cus.id " +
                                "where col.customerid=? order by cus.customername ",
                        new String[]{String.valueOf(prCustomerId)});
            } else {
                cursor = db.rawQuery("select col.id, cus.customername, cus.balance, col.documentnumber, " +
                        "col.collectdate, col.customerid, col.paymenttype, col.amount, col.description from collects col " +
                        "inner join customers cus on col.customerid=cus.id " +
                        "order by col.collectdate desc", null);
            }

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    map = new HashMap<String, String>();
                    map.put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")));
                    map.put("customername", cursor.getString(cursor.getColumnIndexOrThrow("customername")));
                    map.put("balance", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("balance")),2));
                    map.put("amount", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),2));
                    map.put("documentnumber", cursor.getString(cursor.getColumnIndexOrThrow("documentnumber")));
                    map.put("collectdate", cursor.getString(cursor.getColumnIndexOrThrow("collectdate")));
                    map.put("description", cursor.getString(cursor.getColumnIndexOrThrow("description")));
                    map.put("customerid", cursor.getString(cursor.getColumnIndexOrThrow("customerid")));
                    if (cursor.getInt(cursor.getColumnIndexOrThrow("paymenttype")) == 0) {
                        map.put("paymenttype", Variables.context.getString(R.string.paymentcash));
                    } else {
                        map.put("paymenttype", Variables.context.getString(R.string.paymentcard));
                    }

                    list.add(map);
                }
                cursor.close();
            }
        } catch (SQLException ex) {
            Log.d("Collect List", ex.getMessage());
        }
        return list;
    }


    public static ArrayList<HashMap<String, String>> getAllCollectList(long prCustomerId) {
        if (Db == null)
            Db = new Database();

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map;
        SQLiteDatabase db = Db.getReadableDatabase();

        Cursor cursor;
        try {
            if (prCustomerId > 0) {
                cursor = db.rawQuery("select col.id, cus.customername, cus.balance, col.documentnumber, " +
                                "col.collectdate, col.customerid, col.paymenttype, col.amount, col.description from collects as col " +
                                "inner join customers as cus on col.customerid=cus.id " +
                                "where col.customerid=? order by cus.customername ",
                        new String[]{String.valueOf(prCustomerId)});
            } else {
                cursor = db.rawQuery("select col.id, cus.customername, cus.balance, col.documentnumber, " +
                        "col.collectdate, col.customerid, col.paymenttype, col.amount, col.description from collects col " +
                        "inner join customers cus on col.customerid=cus.id " +
                        "order by col.collectdate desc", null);
            }

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    map = new HashMap<String, String>();
                    map.put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")));
                    map.put("customername", cursor.getString(cursor.getColumnIndexOrThrow("customername")));
                    map.put("balance", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("balance")),2));
                    map.put("amount", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),2));
                    map.put("documentnumber", cursor.getString(cursor.getColumnIndexOrThrow("documentnumber")));
                    map.put("collectdate", cursor.getString(cursor.getColumnIndexOrThrow("collectdate")));
                    map.put("description", cursor.getString(cursor.getColumnIndexOrThrow("description")));
                    map.put("customerid", cursor.getString(cursor.getColumnIndexOrThrow("customerid")));
                    if (cursor.getInt(cursor.getColumnIndexOrThrow("paymenttype")) == 0) {
                        map.put("paymenttype", Variables.context.getString(R.string.paymentcash));
                    } else {
                        map.put("paymenttype", Variables.context.getString(R.string.paymentcard));
                    }

                    list.add(map);
                }
                cursor.close();
            }
        } catch (SQLException ex) {
            Log.d("Collect List", ex.getMessage());
        }
        return list;
    }




    public static HashMap<String, String> getCollectForPrint(int prCollectId) {
        if (Db == null)
            Db = new Database();

        HashMap<String, String> map = new HashMap<String, String>();

        SQLiteDatabase db = Db.getReadableDatabase();

        Cursor cursor = db.rawQuery("select col.documentnumber, col.collectdate, col.collecttime, col.description, " +
                "col.amount, col.paymenttype, cus.customername, cus.taxid, cus.taxoffice, col.customerid, cus.address1," +
                "cus.balance from collects col " +
                "inner join customers cus on cus.id=col.customerid where col.id=? ", new String[]{String.valueOf(prCollectId)});

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            map.put("customerid", cursor.getString(cursor.getColumnIndexOrThrow("customerid")));
            map.put("documentnumber", cursor.getString(cursor.getColumnIndexOrThrow("documentnumber")));
            map.put("collectdate", cursor.getString(cursor.getColumnIndexOrThrow("collectdate")));
            map.put("collecttime", cursor.getString(cursor.getColumnIndexOrThrow("collecttime")));
            map.put("description", cursor.getString(cursor.getColumnIndexOrThrow("description")));
            map.put("amount", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),2));
            if (cursor.getInt(cursor.getColumnIndexOrThrow("paymenttype")) == 0) {
                map.put("paymenttype", Variables.context.getString(R.string.paymentcash));
            } else {
                map.put("paymenttype", Variables.context.getString(R.string.paymentcard));
            }
            map.put("customername", cursor.getString(cursor.getColumnIndexOrThrow("customername")));
            map.put("customeraddress", cursor.getString(cursor.getColumnIndexOrThrow("address1")));
            map.put("balance", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("balance")),2));
            map.put("taxid", cursor.getString(cursor.getColumnIndexOrThrow("taxid")));
            map.put("taxoffice", cursor.getString(cursor.getColumnIndexOrThrow("taxoffice")));
        }
        cursor.close();

        return map;
    }

    public static Collect getCollect(int prCollectId) {
        if (Db == null)
            Db = new Database();

        Collect col = new Collect();

        SQLiteDatabase db = Db.getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from collects where id=? ", new String[]{String.valueOf(prCollectId)});

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            col.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            col.setCustomerId(cursor.getInt(cursor.getColumnIndexOrThrow("customerid")));
            col.setPaymentType(cursor.getInt(cursor.getColumnIndexOrThrow("paymenttype")));
            col.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")));
            col.setNumber(cursor.getString(cursor.getColumnIndexOrThrow("documentnumber")));
            col.setDate(cursor.getString(cursor.getColumnIndexOrThrow("collectdate")));
            col.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        }
        cursor.close();

        return col;
    }

    public static boolean isThereNewCustomer() {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getReadableDatabase();

        String lSql = "select id from customers where isnew=1";

        Cursor cursor = db.rawQuery(lSql, null);

        return (cursor.getCount() > 0);
    }


    // Erkan : 29.06.2024
    public static double getLastBalancaNachRechnung(long invoiceId, long customerId) {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getReadableDatabase();

        double lCustomerBalance = 0.0;

        String sql = "select COALESCE (sum(i.subtotal),0) from invoice i " +
                " WHERE i.id < "+ invoiceId  +
                " AND i.customerid =  " + customerId;

        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            lCustomerBalance = cursor.getDouble(0);
        }

        return lCustomerBalance;
    }

    // Erkan: 29.06.2024
    public static double getCustomerGuncelBalance(long customerId) {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getReadableDatabase();

        double lCustomerBalance = 0.0;

        Cursor cursor = db.rawQuery("select COALESCE (balance,0) as balance from customers where id=?", new String[]{String.valueOf(customerId)});

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            lCustomerBalance = cursor.getDouble(0);
        }

        return lCustomerBalance;
    }





    public static double changeCustomerBalanceErkan(long customerId, double total, double yeniBakiye) {
        if (Db == null) {
            Db = new Database();
        }


        SQLiteDatabase db = Db.getReadableDatabase();

        double lCustomerBalance = 0.0;

        Cursor cursor = db.rawQuery("select balance from customers where id=?", new String[]{String.valueOf(customerId)});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            lCustomerBalance = cursor.getDouble(0);
        }

        db = Db.getWritableDatabase();
        ContentValues values = new ContentValues();
        values = new ContentValues();
        values.put("balance", lCustomerBalance + total);
        db.update("customers", values, "id=?", new String[]{String.valueOf(customerId)});

        return lCustomerBalance + total;
    }


    //
    public static double changeCustomerBalance(long customerId, double total) {
        if (Db == null) {
            Db = new Database();
        }


        SQLiteDatabase db = Db.getReadableDatabase();

        double lCustomerBalance = 0.0;

        Cursor cursor = db.rawQuery("select balance from customers where id=?", new String[]{String.valueOf(customerId)});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            lCustomerBalance = cursor.getDouble(0);
        }

        db = Db.getWritableDatabase();
        ContentValues values = new ContentValues();
        values = new ContentValues();
        values.put("balance", lCustomerBalance + total);
        db.update("customers", values, "id=?", new String[]{String.valueOf(customerId)});

        return lCustomerBalance + total;
    }


    // EDITEN SONRA KULLANILACAK
    public static void changeCustomerBalance2(long customerId) {
        if (Db == null) {
            Db = new Database();
        }

        double total = InvoiceDao.getCustomerTotal(customerId);

        SQLiteDatabase db = Db.getWritableDatabase();
        ContentValues values = new ContentValues();
        values = new ContentValues();
        values.put("balance", total);
        db.update("customers", values, "id=?", new String[]{String.valueOf(customerId)});
    }


    public static void deleteAllCustomers(){
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getWritableDatabase();
        db.delete("customers", "", null);
    }

}
