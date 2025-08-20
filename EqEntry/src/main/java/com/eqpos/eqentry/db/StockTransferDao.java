package com.eqpos.eqentry.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.eqpos.eqentry.models.StockTransfer;
import com.eqpos.eqentry.tools.Variables;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;

public class StockTransferDao {
    private static Database Db;


    public static void saveWarehouse(int id, String wareName) {
        if (Db == null) {
            Db = new Database();
        }

        ContentValues value = new ContentValues();
        value.put("id", id);
        value.put("warehousename", wareName);

        SQLiteDatabase db = Db.getWritableDatabase();

        try {
            db.insert("warehouses", null, value);
        } catch (SQLException e) {
            Log.e("warehouses Save Error 1", e.toString());
        }
    }

    public static void saveWarehouse(JsonArray value) {
        try {
            for (int i = 0; i < value.size(); i++) {
                JsonObject jData = value.get(i).getAsJsonObject();

                saveWarehouse(jData.get("depoid").getAsInt(), jData.get("depoadi").getAsString());
            }
        } catch (Exception e) {
            Log.e("warehouses Save Error 2", e.toString());
        }
    }

    public static long createNewStockTransfer(int id, String documentnumber, int wareId) {
        if (Db == null)
            Db = new Database();

        SQLiteDatabase db = Db.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (id > 0)
            values.put("id", id);
        values.put("documentnumber", documentnumber);
        values.put("targetwareid", wareId);

        long lId = id;
        try {
            if (id > 0)
                db.update("stocktransfer", values, "id=?", new String[]{String.valueOf(id)});
            else
                lId = db.insert("stocktransfer", null, values);

        } catch (SQLException e) {
            Log.e("create stocktransfer", e.getMessage());
        }

        return lId;
    }

    public static void removeTransfer(long transferId) {
        if (Db == null)
            Db = new Database();

        SQLiteDatabase db = Db.getWritableDatabase();

        try {
            db.delete("stocktransferdetail", "stocktransferid=?", new String[]{String.valueOf(transferId)});
            db.delete("stocktransfer", "id=?", new String[]{String.valueOf(transferId)});
        } catch (SQLException e) {
            Log.e("remove transfer", e.getMessage());
        }
    }

    public static void removeProductFromTransfer(int id) {
        if (Db == null)
            Db = new Database();

        SQLiteDatabase db = Db.getWritableDatabase();

        try {
            db.delete("stocktransferdetail", "id=?", new String[]{String.valueOf(id)});
        } catch (SQLException e) {
            Log.e("remove product from", e.getMessage());
        }
    }



    public static ArrayList<HashMap<String, String>> getWarehouseList() {
        if (Db == null) {
            Db = new Database();
        }

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        SQLiteDatabase db = Db.getReadableDatabase();
        String lSql = "select id, warehousename from warehouses order by warehousename";
        Cursor cursor = db.rawQuery(lSql, null);
        if (cursor.getCount() > 0) {
            HashMap<String, String> map;

            while (cursor.moveToNext()) {
                map = new HashMap<String, String>();
                map.put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")));
                map.put("warehousename", cursor.getString(cursor.getColumnIndexOrThrow("warehousename")));

                list.add(map);
            }
            cursor.close();
        }
        return list;
    }


    public static ArrayList<HashMap<String, String>> getTransferList() {
        if (Db == null) {
            Db = new Database();
        }

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        SQLiteDatabase db = Db.getReadableDatabase();
        String lSql = "select t.id, t.documentnumber, w.warehousename from stocktransfer t " +
                "left join warehouses w on w.id=t.targetwareid order by t.id";
        Cursor cursor = db.rawQuery(lSql, null);
        if (cursor.getCount() > 0) {
            HashMap<String, String> map;

            while (cursor.moveToNext()) {
                map = new HashMap<String, String>();
                map.put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")));
                map.put("documentnumber", cursor.getString(cursor.getColumnIndexOrThrow("documentnumber")));
                map.put("warehousename", cursor.getString(cursor.getColumnIndexOrThrow("warehousename")));

                list.add(map);
            }
            cursor.close();
        }
        return list;
    }


    public static StockTransfer getStockTransfer(long id) {
        if (Db == null)
            Db = new Database();

        SQLiteDatabase db = Db.getReadableDatabase();
        StockTransfer transfer = new StockTransfer();
        transfer.setId(0);
        transfer.setTargetWarehouseId(0);
        transfer.setTargetWarehouseName("");
        transfer.setNumber("");

        String lSql = "select t.id, t.documentnumber, w.id as wareid, w.warehousename from stocktransfer t " +
                "left join warehouses w on w.id=t.targetwareid where t.id = "+ String.valueOf(id) +" order by t.id";

        Cursor cursor = db.rawQuery(lSql, null);
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            transfer.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            transfer.setNumber(cursor.getString(cursor.getColumnIndexOrThrow("documentnumber")));
            transfer.setTargetWarehouseName(cursor.getString(cursor.getColumnIndexOrThrow("warehousename")));
            transfer.setTargetWarehouseId(cursor.getInt(cursor.getColumnIndexOrThrow("wareid")));
            cursor.close();
        }

        return transfer;
    }


    public static ArrayList<HashMap<String, String>> getStockTransferDetail(long transferId) {
        if (Db == null) {
            Db = new Database();
        }

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        SQLiteDatabase db = Db.getReadableDatabase();

        String lSql = "select t.*, p.productname, u.unitename from stocktransferdetail t " +
                "inner join products p on p.id=t.productid " +
                "left join unites u on u.id=p.uniteid " +
                "where stocktransferid=?";
        Cursor cursor = db.rawQuery(lSql, new String[]{String.valueOf(transferId)});

        if (cursor.getCount() > 0) {
            HashMap<String, String> map;

            while (cursor.moveToNext()) {
                map = new HashMap<String, String>();
                map.put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")));
                map.put("productid", cursor.getString(cursor.getColumnIndexOrThrow("productid")));
                map.put("productname", cursor.getString(cursor.getColumnIndexOrThrow("productname")));
                map.put("unitename", cursor.getString(cursor.getColumnIndexOrThrow("unitename")));
                if (cursor.getInt(cursor.getColumnIndexOrThrow("packageamount")) > 0) {
                    map.put("packageamount", cursor.getString(cursor.getColumnIndexOrThrow("packageamount")) + " Koli");
                } else {
                    map.put("packageamount", " ");
                }
                map.put("amount", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")), 0));

                list.add(map);
            }
            cursor.close();
        }
        return list;
    }

    public static boolean addProductAmount(long transferId, int productId, double amount, boolean isPackage) {
        if (Db == null) {
            Db = new Database();
        }

        double currentAmount = 0.0,
                currentPackage = 0.0;
        if (amount == 0.0)
            amount = 1;
        int lId = 0;
        boolean isUpdate = false;

        SQLiteDatabase db = Db.getReadableDatabase();
        //Ürün var mı varsa miktarı artırılacak
        Cursor cursor = db.rawQuery("select id, amount, packageamount from stocktransferdetail " +
                        " where stocktransferid=? and productid=? order by id desc limit 1",
                new String[]{String.valueOf(transferId), String.valueOf(productId)});
        if (cursor.getCount() > 0) {
            cursor.moveToNext();

            lId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            currentAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
            currentPackage = cursor.getInt(cursor.getColumnIndexOrThrow("packageamount"));

            isUpdate = true;
        }

        if (isUpdate) {
            db = Db.getWritableDatabase();

            /*deleteProductFromInvoice(invoiceId, productId);*/
            currentAmount = currentAmount + amount;

            ContentValues values = new ContentValues();
            values.put("amount", currentAmount);
            if (isPackage) {
                values.put("packageamount", currentPackage + 1);
            }

            db.update("stocktransferdetail", values, "id=?", new String[]{String.valueOf(lId)});
        }

        return isUpdate;
    }

    public static void addProductToTransfer(long transferId, int productId, Double amount, boolean ischange,
                                               boolean isPackage) {
        if (Db == null)
            Db = new Database();

        boolean isUpdate = false;
        double currentAmount = 0.0,
                currentPackage = 0.0;

        SQLiteDatabase db = Db.getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from stocktransferdetail where stocktransferid=? and productid=? ",
                new String[]{String.valueOf(transferId), String.valueOf(productId)});
        if (cursor.getCount() > 0) {
            cursor.moveToNext();

            if (!ischange) {
                currentAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                currentPackage = cursor.getDouble(cursor.getColumnIndexOrThrow("packageamount"));
            }
            isUpdate = true;
        }

        db = Db.getWritableDatabase();
        ContentValues values = new ContentValues();
        long lId = 0;

        values.put("stocktransferid", transferId);
        values.put("productid", productId);
        values.put("amount", amount + currentAmount);
        if (isPackage) {
            values.put("packageamount", currentPackage + 1);
        }

        try {
            if (isUpdate) {
                lId = db.update("stocktransferdetail", values, "stocktransferid=? and productid=?",
                        new String[]{String.valueOf(transferId), String.valueOf(productId)});
            } else {
                db.insert("stocktransferdetail", null, values);
            }

        } catch (SQLException e) {
            Log.e("Add product To stock", e.getMessage());
        }
    }

    public static void changeWareId(long transferId, long wareId) {
        if (Db == null) {
            Db = new Database();
        }


        SQLiteDatabase db = Db.getReadableDatabase();
            db = Db.getWritableDatabase();

            ContentValues values = new ContentValues();
        values.put("targetwareid", wareId);
        db.update("stocktransfer", values, "id=?", new String[]{String.valueOf(transferId)});

    }
}
