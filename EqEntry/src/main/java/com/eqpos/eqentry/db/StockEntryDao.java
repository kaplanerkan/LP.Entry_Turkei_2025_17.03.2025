package com.eqpos.eqentry.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.eqpos.eqentry
        .models.Delivery;
import com.eqpos.eqentry.tools.Variables;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dursu on 22.03.2018.
 */

public class StockEntryDao {
    private static Database Db;

    public static ArrayList<HashMap<String, String>> getDeliveryList() {
        if (Db == null) {
            Db = new Database();
        }

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        SQLiteDatabase db = Db.getReadableDatabase();
        String lSql = "select d.*, s.suppliername from delivery d " +
                "left join suppliers s on s.id=d.supplierid order by documentdate, suppliername";
        Cursor cursor = db.rawQuery(lSql, null);
        if (cursor.getCount() > 0) {
            HashMap<String, String> map;

            while (cursor.moveToNext()) {
                map = new HashMap<String, String>();
                map.put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")));
                map.put("documentnumber", cursor.getString(cursor.getColumnIndexOrThrow("documentnumber")));
                map.put("documentdate", cursor.getString(cursor.getColumnIndexOrThrow("documentdate")));
                map.put("suppliername", cursor.getString(cursor.getColumnIndexOrThrow("suppliername")));
                map.put("supplierid", cursor.getString(cursor.getColumnIndexOrThrow("supplierid")));
                map.put("receiver", cursor.getString(cursor.getColumnIndexOrThrow("receiver")));

                list.add(map);
            }
            cursor.close();
        }
        return list;
    }


    public static void removeDelivery(int deliveryId) {
        if (Db == null)
            Db = new Database();

        SQLiteDatabase db = Db.getWritableDatabase();

        try {
            deleteEntryFromStock(deliveryId);
            db.delete("deliverydetail", "deliveryid=?", new String[]{String.valueOf(deliveryId)});
            db.delete("delivery", "id=?", new String[]{String.valueOf(deliveryId)});
        } catch (SQLException e) {
            Log.e("remove delivery", e.getMessage());
        }
    }

    public static void removeProductFromDelivery(int id) {
        if (Db == null)
            Db = new Database();

        SQLiteDatabase db = Db.getWritableDatabase();

        try {
            db.delete("deliverydetail", "id=?", new String[]{String.valueOf(id)});
        } catch (SQLException e) {
            Log.e("remove product from", e.getMessage());
        }
    }

    public static long saveDeliveryNote(int id, String documentnumber, String documentdate, int supplierId, String receiver) {
        if (Db == null)
            Db = new Database();

        SQLiteDatabase db = Db.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (id > 0)
            values.put("id", id);
        values.put("documentnumber", documentnumber);
        values.put("documentdate", documentdate);
        values.put("documenttime", Variables.getCurrentTime());
        values.put("supplierid", supplierId);
        values.put("receiver", receiver);

        long lId = id;
        try {
            if (id > 0)
                db.update("delivery", values, "id=?", new String[]{String.valueOf(id)});
            else
                lId = db.insert("delivery", null, values);

        } catch (SQLException e) {
            Log.e("create Delivery", e.getMessage());
        }

        return lId;
    }

    public static boolean addProductAmount(long deliveryId, int productId, double amount, boolean isPackage) {
        if (Db == null) {
            Db = new Database();
        }

        double currentAmount = 0.0,
                currentTotal = 0.0,
                currentCostPrice = 0.0,
                currentPackage = 0.0;
        if (amount == 0.0)
            amount = 1;
        int lId = 0;
        boolean isUpdate = false;

        SQLiteDatabase db = Db.getReadableDatabase();
        //Ürün var mı varsa miktarı artırılacak
        Cursor cursor = db.rawQuery("select id, amount, costprice, total, packageamount from deliverydetail " +
                        " where deliveryid=? and productid=? order by id desc limit 1",
                new String[]{String.valueOf(deliveryId), String.valueOf(productId)});
        if (cursor.getCount() > 0) {
            cursor.moveToNext();

            lId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            currentAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
            currentCostPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("costprice"));
            currentTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
            currentPackage = cursor.getInt(cursor.getColumnIndexOrThrow("packageamount"));

            isUpdate = true;
        }

        if (isUpdate) {
            db = Db.getWritableDatabase();

            /*deleteProductFromInvoice(invoiceId, productId);*/
            currentAmount = currentAmount + amount;
            currentTotal = currentAmount * currentCostPrice;

            ContentValues values = new ContentValues();
            values.put("amount", currentAmount);
            values.put("total", currentTotal);
            if (isPackage) {
                values.put("packageamount", currentPackage + 1);
            }

            db.update("deliverydetail", values, "id=?", new String[]{String.valueOf(lId)});
        }

        return isUpdate;
    }

    public static void addProductToDeliverNote(int deliveryId, int productId, String partNumber,
                                               String expirationDate, Double amount, Double costPrice, boolean ischange,
                                               boolean isPackage) {
        if (Db == null)
            Db = new Database();

        boolean isUpdate = false;
        double currentAmount = 0.0,
                currentTotal = 0.0,
                currentPackage = 0.0;
        double total = Variables.roundTo(amount * costPrice, 2);

        SQLiteDatabase db = Db.getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from deliverydetail where deliveryid=? and productid=? and partnumber=?",
                new String[]{String.valueOf(deliveryId), String.valueOf(productId), partNumber});
        if (cursor.getCount() > 0) {
            cursor.moveToNext();

            if (!ischange) {
                currentAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                currentTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
                currentPackage = cursor.getDouble(cursor.getColumnIndexOrThrow("packageamount"));
            }
            isUpdate = true;
        }

        db = Db.getWritableDatabase();
        ContentValues values = new ContentValues();
        long lId = 0;

        values.put("deliveryid", deliveryId);
        values.put("productid", productId);
        values.put("partnumber", partNumber);
        values.put("expirationdate", expirationDate);
        values.put("amount", amount + currentAmount);
        values.put("costprice", costPrice);
        values.put("total", total + currentTotal);
        if (isPackage) {
            values.put("packageamount", currentPackage + 1);
        }

        try {
            if (isUpdate) {
                lId = db.update("deliverydetail", values, "deliveryid=? and productid=? and partnumber=?",
                        new String[]{String.valueOf(deliveryId), String.valueOf(productId), partNumber});
            } else {
                db.insert("deliverydetail", null, values);
            }

        } catch (SQLException e) {
            Log.e("Add product To Delivery", e.getMessage());
        }
    }

    public static Delivery getDelivery(int id) {
        if (Db == null)
            Db = new Database();

        SQLiteDatabase db = Db.getReadableDatabase();
        Delivery delivery = new Delivery();

        String lSql = "select d.*, s.suppliername from delivery d " +
                "left join suppliers s on s.id=d.supplierid where d.id= "+ String.valueOf(id) +" order by documentdate, suppliername";
        Cursor cursor = db.rawQuery(lSql, null);
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            delivery.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            delivery.setNumber(cursor.getString(cursor.getColumnIndexOrThrow("documentnumber")));
            delivery.setDate(cursor.getString(cursor.getColumnIndexOrThrow("documentdate")));
            delivery.setSupplierName(cursor.getString(cursor.getColumnIndexOrThrow("suppliername")));
            delivery.setSupplierId(cursor.getInt(cursor.getColumnIndexOrThrow("supplierid")));
            delivery.setReceiverName(cursor.getString(cursor.getColumnIndexOrThrow("receiver")));
            cursor.close();
        }

        return delivery;
    }

    public static String getTotalAmount(long deliveryId) {
        if (Db == null) {
            Db = new Database();
        }
        String lAmountStr = "";
        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor = db.rawQuery("select u.unitename, sum(d.amount) as total, sum(packageamount) as totalpackage from deliverydetail d " +
                "inner join products p on p.id=d.productid " +
                "inner join unites u on u.id=p.uniteid " +
                "where d.deliveryid=? group by u.unitename", new String[]{String.valueOf(deliveryId)});

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                if (cursor.getInt(2) > 0) {
                    lAmountStr = lAmountStr + String.valueOf(cursor.getInt(2)) + " Koli  " + Variables.doubleToStr(cursor.getDouble(1), 0) + " " + cursor.getString(0) + "  ";
                } else {
                    lAmountStr = lAmountStr + Variables.doubleToStr(cursor.getDouble(1), 0) + " " + cursor.getString(0) + "  ";
                }
            }
        }

        return lAmountStr;
    }

    public static HashMap<String, String> getDeliveryHeader(long deliveryId) {
        if (Db == null) {
            Db = new Database();
        }
        HashMap<String, String> map = new HashMap<String, String>();

        SQLiteDatabase db = Db.getReadableDatabase();

        String lAmountStr = "";

        Cursor cursor = db.rawQuery("select u.unitename, sum(d.amount) as total, sum(packageamount) as totalpackage from deliverydetail d " +
                "inner join products p on p.id=d.productid " +
                "inner join unites u on u.id=p.uniteid " +
                "where d.deliveryid=? group by u.unitename", new String[]{String.valueOf(deliveryId)});

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                if (cursor.getInt(2) > 0) {
                    lAmountStr = lAmountStr + String.valueOf(cursor.getInt(2)) + " Koli  " + Variables.doubleToStr(cursor.getDouble(1), 0) + " " + cursor.getString(0) + "  ";
                } else {
                    lAmountStr = lAmountStr + Variables.doubleToStr(cursor.getDouble(1), 0) + " " + cursor.getString(0) + "  ";
                }
            }
        }

        cursor = db.rawQuery("select sum(total) as total from deliverydetail " +
                "where deliveryid=? ", new String[]{String.valueOf(deliveryId)});
        double total = 0.0;
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));

            }
        }

        cursor = db.rawQuery("select i.id, i.documentnumber, i.documentdate, i.documenttime, i.supplierid, " +
                        "i.receiver, c.customername, c.address1 from delivery i " +
                        "left join customers c on c.id=i.supplierid where i.id=?",
                new String[]{String.valueOf(deliveryId)});

        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                map.put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")));
                map.put("documentnumber", cursor.getString(cursor.getColumnIndexOrThrow("documentnumber")));
                map.put("documentdate", cursor.getString(cursor.getColumnIndexOrThrow("documentdate")));
                map.put("documenttime", cursor.getString(cursor.getColumnIndexOrThrow("documenttime")));
                map.put("supplierid", cursor.getString(cursor.getColumnIndexOrThrow("supplierid")));
                map.put("customername", cursor.getString(cursor.getColumnIndexOrThrow("customername")));
                map.put("customeraddress", cursor.getString(cursor.getColumnIndexOrThrow("address1")));
                map.put("receiver", cursor.getString(cursor.getColumnIndexOrThrow("receiver")));
                map.put("total", Variables.doubleToStr(total, 2));
                map.put("amountstr", lAmountStr);

            }
            cursor.close();
        }
        return map;
    }

    public static ArrayList<HashMap<String, String>> getDeliveryDetail(long deliveryId) {
        if (Db == null) {
            Db = new Database();
        }

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        SQLiteDatabase db = Db.getReadableDatabase();

        String lSql = "select d.*, p.productname, u.unitename from deliverydetail d " +
                "inner join products p on p.id=d.productid " +
                "left join unites u on u.id=p.uniteid " +
                "where deliveryid=?";
        Cursor cursor = db.rawQuery(lSql, new String[]{String.valueOf(deliveryId)});

        if (cursor.getCount() > 0) {
            HashMap<String, String> map;

            while (cursor.moveToNext()) {
                map = new HashMap<String, String>();
                map.put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")));
                map.put("productid", cursor.getString(cursor.getColumnIndexOrThrow("productid")));
                map.put("productname", cursor.getString(cursor.getColumnIndexOrThrow("productname")));
                map.put("unitename", cursor.getString(cursor.getColumnIndexOrThrow("unitename")));
                map.put("partnumber", cursor.getString(cursor.getColumnIndexOrThrow("partnumber")));
                map.put("expirationdate", cursor.getString(cursor.getColumnIndexOrThrow("expirationdate")));
                if (cursor.getInt(cursor.getColumnIndexOrThrow("packageamount")) > 0) {
                    map.put("packageamount", cursor.getString(cursor.getColumnIndexOrThrow("packageamount")) + " Koli");
                } else {
                    map.put("packageamount", " ");
                }
                map.put("amount", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")), 0));
                String lCostPrice = "";
                String lTotal = "";
                try {
                    lCostPrice = Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("costprice")), 2);
                } catch (NumberFormatException e) {
                    lCostPrice = Variables.doubleToStr(0.0, 2);
                    Log.e("Cost Price convert", e.getMessage());
                }
                try {
                    lTotal = Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("total")), 2);
                } catch (NumberFormatException e) {
                    lTotal = Variables.doubleToStr(0.0, 2);
                    Log.e("total Price convert", e.getMessage());
                }
                map.put("costprice", lCostPrice);
                map.put("total", lTotal);

                list.add(map);
            }
            cursor.close();
        }
        return list;
    }

    public static ArrayList<String> getSupplierNameList() {
        if (Db == null) {
            Db = new Database();
        }

        ArrayList<String> list = new ArrayList<String>();

        SQLiteDatabase db = Db.getReadableDatabase();
        String lSql = "select suppliername from suppliers order by suppliername";
        Cursor cursor = db.rawQuery(lSql, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                list.add(cursor.getString(0));
            }
            cursor.close();
        }
        return list;
    }

    public static ArrayList<Integer> getSupplierIdList() {
        if (Db == null) {
            Db = new Database();
        }

        ArrayList<Integer> list = new ArrayList<Integer>();

        SQLiteDatabase db = Db.getReadableDatabase();
        String lSql = "select id from suppliers order by suppliername";
        Cursor cursor = db.rawQuery(lSql, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                list.add(cursor.getInt(0));
            }
            cursor.close();
        }
        return list;
    }

    public static void deleteEntryFromStock(int deliveryId) {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor = db.rawQuery("select productid, amount from deliverydetail where deliveryid=?", new String[]{String.valueOf(deliveryId)});
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                double lProductAmount = 0.0;
                long lProductId = 0;

                lProductId = cursor.getInt(0);
                lProductAmount = cursor.getDouble(1);

                ProductDao.changeProductStock(lProductId, -1* lProductAmount);
            }
        }
    }

    public static void addEntryToStock(int deliveryId) {
        if (Db == null) {
            Db = new Database();
        }
        double lProductAmount = 0.0;
        long lProductId = 0;

        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor = db.rawQuery("select productid, amount from deliverydetail where deliveryid=?", new String[]{String.valueOf(deliveryId)});
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {

                lProductId = cursor.getInt(0);
                lProductAmount = cursor.getDouble(1); //ProductDao.getProductStock(lProductId);

                ProductDao.changeProductStock(lProductId, lProductAmount );
            }
        }
    }

}
