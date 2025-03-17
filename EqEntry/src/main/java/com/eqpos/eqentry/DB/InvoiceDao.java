package com.eqpos.eqentry.DB;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.eqpos.eqentry.tools.Variables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by dursu on 02.11.2018.
 */

public class InvoiceDao {
    private static Database Db;

    public static String getRandomInvoiceNumber() {
        String number = "";
        Random rand = new Random();

        int r = rand.nextInt(50) + 1;
        String charList = "AZQSXWDCEFVRGBTHNYJMUKILOP";
        for (int i = 0; i <= 8; i++) {

            if (i < 3) {
                r = rand.nextInt(24);
                number += charList.substring(r, r + 1);
            } else {
                r = rand.nextInt(9);
                number = number + String.valueOf(r);
            }
        }
        return number;
    }

    //Erkan  29.06.2024
    public static double getInvoiceTotalAmount(long invoiceId) {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor;

        double lInvoiceTotal = 0;
        cursor = db.rawQuery("select subtotal from invoice where id=?", new String[]{String.valueOf(invoiceId)});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            lInvoiceTotal = cursor.getDouble(0);
        }
        return lInvoiceTotal;
    }



    public static void deleteInvoice(long invoiceId, long customerID) {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getWritableDatabase();
        db.delete("invoice", "id=?", new String[]{String.valueOf(invoiceId)});
        db.delete("invoicedetail", "invoiceid=?", new String[]{String.valueOf(invoiceId)});

        CustomerDao.changeCustomerBalance2(customerID);
    }

    public static void deleteInvoiceDetail(long invoiceId) {

        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getWritableDatabase();
        db.delete("invoicedetail", "invoiceid=?", new String[]{String.valueOf(invoiceId)});
    }

    public static void changeTaxStatus(long invoiceId, boolean includeTax) {
        if (Db == null) {
            Db = new Database();
        }
        double unitPrice = 0.0,
                amount = 0.0,
                taxrate = 0.0,
                tax = 0.0,
                discount = 0.0,
                total = 0.0,
                subTotal = 0.0;
        int id = 0;

        SQLiteDatabase db = Db.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("includetax", includeTax ? 1 : 0);

        db.update("invoice", values, "id=?", new String[]{String.valueOf(invoiceId)});

        Cursor cursor = db.query("invoicedetail", new String[]{"id", "unitprice", "amount", "discount1", "taxrate"}, "invoiceid=?", new String[]{String.valueOf(invoiceId)}, "", "", "");

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                id = cursor.getInt(0);
                unitPrice = cursor.getDouble(1);
                amount = cursor.getDouble(2);
                discount = cursor.getDouble(3);
                taxrate = cursor.getDouble(4);

                if (!includeTax) {
                    total = (unitPrice * amount) - discount;
                    subTotal = total * (1 + (taxrate / 100));
                    tax = subTotal - total;
                } else {
                    subTotal = (unitPrice * amount) - discount;
                    total = subTotal / (1 + (taxrate / 100));
                    tax = subTotal - total;
                }

                db = Db.getWritableDatabase();
                values.clear();
                values.put("total", total);
                values.put("tax", tax);
                values.put("subtotal", subTotal);

                db.update("invoicedetail", values, "id=" + String.valueOf(id), null);
            }
            cursor.close();
        }

    }

    public static long createInvoice(String invoiceNumber, String invoiceDate, int customerId, double total, double discount, boolean includeTax) {

        if (Db == null) {
            Db = new Database();
        }

        String lInvoiceNumber = invoiceNumber;
        if (lInvoiceNumber.isEmpty())
            lInvoiceNumber = getRandomInvoiceNumber();

        SQLiteDatabase db = Db.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("invoicenumber", lInvoiceNumber);
        values.put("invoicedate", invoiceDate);
        values.put("invoicetime", Variables.getCurrentTime());
        values.put("customerid", customerId);
        values.put("total", total);
        values.put("discount", discount);
        values.put("subtotal", total - discount);
        values.put("includetax", includeTax ? 1 : 0);

        long lId = db.insert("invoice", null, values);

        return lId;
    }

    public static boolean addProductAmount(long invoiceId, int productId, double amount, boolean isPackage, boolean isTaxInclude) {
        if (Db == null) {
            Db = new Database();
        }

        double taxRate = 0.0, tax = 0.0, currentAmount = 0.0, currentDiscount = 0.0,
                currentTotal = 0.0, currentSubTotal = 0.0,
                currentUnitPrice = 0.0, currentPackage = 0.0;
        if (amount == 0.0 && !isPackage)
            amount = 1;
        int lId = 0;
        boolean isUpdate = false;

        SQLiteDatabase db = Db.getReadableDatabase();
        //Ürün var mı varsa miktarı artırılacak
        Cursor cursor = db.rawQuery("select id, amount, unitprice, discount1, total, subtotal, packageamount, taxrate, tax from invoicedetail " +
                        " where invoiceid=? and productid=? order by id desc limit 1",
                new String[]{String.valueOf(invoiceId), String.valueOf(productId)});
        if (cursor.getCount() > 0) {
            cursor.moveToNext();

            lId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            currentAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
            currentUnitPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("unitprice"));
            currentDiscount = cursor.getDouble(cursor.getColumnIndexOrThrow("discount1"));
            currentTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
            currentSubTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("subtotal"));
            taxRate = cursor.getDouble(cursor.getColumnIndexOrThrow("taxrate"));

            if (isPackage) {
                currentPackage = cursor.getDouble(cursor.getColumnIndexOrThrow("packageamount"));
            }

            isUpdate = true;
        }

        if (isUpdate) {
            db = Db.getWritableDatabase();

            /*deleteProductFromInvoice(invoiceId, productId);*/
            currentAmount = currentAmount + amount;
            if (isTaxInclude) {
                currentSubTotal = (currentAmount * currentUnitPrice) - currentDiscount;
                tax = currentSubTotal - currentSubTotal / (1 + (taxRate / 100));
                currentTotal = (currentAmount * currentUnitPrice) - tax;
            } else {
                currentTotal = currentAmount * currentUnitPrice;
                tax = (currentTotal - currentDiscount);
                currentSubTotal = tax * (1 + (taxRate / 100));
                tax = currentSubTotal - tax;
            }
            if (isPackage) {
                currentPackage = currentPackage + 1;
            }

            ContentValues values = new ContentValues();
            values.put("amount", currentAmount);
            values.put("total", currentTotal);
            values.put("tax", tax);
            values.put("subtotal", currentSubTotal);
            if (isPackage) {
                values.put("packageamount", currentPackage);
            } else {
                values.put("packageamount", 0);
            }

            db.update("invoicedetail", values, "id=?", new String[]{String.valueOf(lId)});
        }

        return isUpdate;
    }

    public static double addProductToInvoice(long invoiceId, int productId, double unitPrice, double amount,
                                           double discount1, double discount2, double discount3, double discount4,
                                           double taxRate, double tax, boolean isAddAmount, boolean isPackage,
                                           boolean isTaxInclude, int rowId) {
        if (Db == null) {
            Db = new Database();
        }

        double currentAmount = 0.0, currentDiscount = 0.0,
                currentTotal = 0.0, currentSubTotal = 0.0,
                currentTax = 0.0, taxrate = 0.0, currentPackage = 0;

        double total = Variables.roundTo(amount * unitPrice, 2);
        double subTotal = (total - (discount1 + discount2 + discount3 + discount4)) + tax;
        if (isTaxInclude) {
            subTotal = Variables.roundTo((amount * unitPrice) - (discount1 + discount2 + discount3 + discount4), 2);
            total = subTotal - tax;
        }
        boolean isUpdate = false;
        SQLiteDatabase db;
        if (subTotal >= 0) {
            //Ürün var mı varsa miktarı artırılacak
            if (rowId > 0) {
                db = Db.getWritableDatabase();
                db.delete("invoicedetail", "id=?", new String[]{String.valueOf(rowId)});

            } else {
                db = Db.getReadableDatabase();
                Cursor cursor = db.rawQuery("select * from invoicedetail where invoiceid=? and productid=? and unitprice=? and taxrate=?",
                        new String[]{String.valueOf(invoiceId), String.valueOf(productId), String.valueOf(unitPrice), String.valueOf(taxRate)});

                if (cursor.getCount() > 0) {
                    cursor.moveToNext();

                    if (isAddAmount) {
                        currentAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                        currentDiscount = cursor.getDouble(cursor.getColumnIndexOrThrow("discount1"));
                        currentTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
                        currentSubTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("subtotal"));
                        currentTax = cursor.getDouble(cursor.getColumnIndexOrThrow("tax"));
                    }
                    if (isPackage) {
                        currentPackage = cursor.getInt(cursor.getColumnIndexOrThrow("packageamount"));
                    }
                    isUpdate = true;
                }
            }

            db = Db.getWritableDatabase();

            //deleteProductFromInvoice(invoiceId, productId);
            currentAmount += amount;
            currentDiscount += discount1;
            if (isTaxInclude) {
                currentSubTotal = (currentAmount * unitPrice) - currentDiscount;
                tax = currentSubTotal - currentSubTotal / (1 + (taxRate / 100));
                currentTotal = (currentAmount * unitPrice) - tax;
            } else {
                currentTotal = currentAmount * unitPrice;
                tax = (currentTotal - currentDiscount);
                currentSubTotal = tax * (1 + (taxRate / 100));
                tax = currentSubTotal - tax;
            }


            ContentValues values = new ContentValues();
            values.put("invoiceid", invoiceId);
            values.put("productid", productId);
            values.put("unitprice", unitPrice);
            values.put("amount", currentAmount);
            values.put("total", currentTotal);
            values.put("discount1", currentDiscount);
            values.put("discount2", discount2);
            values.put("discount3", discount3);
            values.put("discount4", discount4);
            values.put("subtotal", currentSubTotal);
            values.put("tax", tax);
            values.put("taxrate", taxRate);

            if (isPackage) {
                values.put("packageamount", currentPackage + 1);
            } else {
                values.put("packageamount", 0);
            }

            if (!isUpdate) {
                db.insert("invoicedetail", null, values);
            } else {
                db.update("invoicedetail", values, "invoiceid=? and  productid=? and unitprice=? and taxrate=?",
                        new String[]{String.valueOf(invoiceId), String.valueOf(productId), String.valueOf(unitPrice), String.valueOf(taxRate)});
            }
        }
        return currentSubTotal;
    }


    public static void deleteProductFromInvoice(long invoiceId, int productId) {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getWritableDatabase();
        db.delete("invoicedetail", "invoiceid=? and productid=?",
                new String[]{String.valueOf(invoiceId), String.valueOf(productId)});
    }


    // Erkan 30.07.2024
    public static void setAktueDetaylariInInvoice(long _invoiceId) {
        if (Db == null) {
            Db = new Database();
        }
        double lTotal = 0.0, lSubtotal = 0.0, lDiscount = 0.0;

        SQLiteDatabase db = Db.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT sum(i.total) as total, sum(i.subtotal) as subtotal, sum(discount1+discount2+discount3+discount4) as discount from invoicedetail i  " +
                "where i.invoiceid=?", new String[]{String.valueOf(_invoiceId)});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            lTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
            lSubtotal = cursor.getDouble(cursor.getColumnIndexOrThrow("subtotal"));
            lDiscount = cursor.getDouble(cursor.getColumnIndexOrThrow("discount"));

            // Sonucu Invoice tablesine update yap
            ContentValues values = new ContentValues();
            values.put("total", lTotal);
            values.put("subtotal", lSubtotal);
            values.put("discount", lDiscount);

            SQLiteDatabase db1 = Db.getWritableDatabase();
            db1.update("invoice", values, "id=?", new String[]{String.valueOf(_invoiceId)});


            //Sonucu Customers tablesine update yap
            SQLiteDatabase db2 = Db.getWritableDatabase();
            ContentValues values1 = new ContentValues();
            values1.put("balance", lSubtotal);
            Cursor cursor1 = db2.rawQuery("select customerid from invoice where id=?", new String[]{String.valueOf(_invoiceId)});
            if (cursor1.getCount() > 0) {
                cursor1.moveToFirst();
                int lCustomerId = cursor1.getInt(cursor1.getColumnIndexOrThrow("customerid"));
                db2.update("customers", values1, "id=?", new String[]{String.valueOf(lCustomerId)});
            }

        }

    }






    public static ArrayList<HashMap<String, String>> getInvoiceList() {
        if (Db == null) {
            Db = new Database();
        }
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        SQLiteDatabase db = Db.getReadableDatabase();

        Cursor cursor = db.rawQuery("select i.id, i.invoicenumber, i.invoicedate, i.customerid, " +
                        "i.total, i.discount, i.subtotal, i.includetax, c.customername from invoice i " +
                        "left join customers c on c.id=i.customerid order by i.id",
                null);

        if (cursor.getCount() > 0) {
            HashMap<String, String> map;

            while (cursor.moveToNext()) {
                map = new HashMap<String, String>();
                map.put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")));
                map.put("invoicenumber", cursor.getString(cursor.getColumnIndexOrThrow("invoicenumber")));
                map.put("invoicedate", cursor.getString(cursor.getColumnIndexOrThrow("invoicedate")));
                map.put("customerid", cursor.getString(cursor.getColumnIndexOrThrow("customerid")));
                map.put("customername", cursor.getString(cursor.getColumnIndexOrThrow("customername")));
                map.put("total", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("total")), 2));
                map.put("discount", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("discount")), 2));
                map.put("subtotal", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("subtotal")), 2));
                map.put("includetax", cursor.getString(cursor.getColumnIndexOrThrow("includetax")));

                list.add(map);
            }
            cursor.close();
        }
        return list;
    }


    public static HashMap<String, String> getInvoiceHeader(long invoiceId) {
        if (Db == null) {
            Db = new Database();
        }
        HashMap<String, String> map = new HashMap<String, String>();

        SQLiteDatabase db = Db.getReadableDatabase();

        String lAmountStr = "";

        Cursor cursor = db.rawQuery("select u.unitename, sum(d.amount) as total, sum(packageamount) as totalpackage from invoicedetail d " +
                "inner join products p on p.id=d.productid " +
                "inner join unites u on u.id=p.uniteid " +
                "where d.invoiceid=? group by u.unitename", new String[]{String.valueOf(invoiceId)});

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                if (cursor.getInt(2) > 0) {
                    lAmountStr = lAmountStr + String.valueOf(cursor.getInt(2)) + " Koli  " + Variables.doubleToStr(cursor.getDouble(1), 0) + " " + cursor.getString(0) + "  ";
                } else {
                    lAmountStr = lAmountStr + Variables.doubleToStr(cursor.getDouble(1), 0) + " " + cursor.getString(0) + "  ";
                }
            }
        }

        cursor = db.rawQuery("select i.id, i.invoicenumber, i.invoicedate, i.invoicetime, i.customerid, " +
                        "i.total, i.taxamount, i.discount, i.subtotal, c.customername, c.taxid, c.address1 from invoice i " +
                        "left join customers c on c.id=i.customerid where i.id=?",
                new String[]{String.valueOf(invoiceId)});

        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                map.put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")));
                map.put("invoicenumber", cursor.getString(cursor.getColumnIndexOrThrow("invoicenumber")));
                map.put("invoicedate", cursor.getString(cursor.getColumnIndexOrThrow("invoicedate")));
                map.put("invoicetime", cursor.getString(cursor.getColumnIndexOrThrow("invoicetime")));
                map.put("customerid", cursor.getString(cursor.getColumnIndexOrThrow("customerid")));
                map.put("customername", cursor.getString(cursor.getColumnIndexOrThrow("customername")));
                map.put("customeraddress", cursor.getString(cursor.getColumnIndexOrThrow("address1")));
                map.put("customertaxid",cursor.getString(cursor.getColumnIndexOrThrow("taxid")));
                map.put("total", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("total")), 2));
                map.put("taxamount", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("taxamount")), 2));
                map.put("discount", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("discount")), 2));
                map.put("subtotal", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("subtotal")), 2));
                map.put("amountstr", lAmountStr);

            }
            cursor.close();
        }
        return map;
    }

    public static void updateInvoiceAuto(long invoiceId, double invoiceOldAmount,  int customerId, String invoiceNumber, String invoiceDate) {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor;
        //önceki fatura tutarı bakiyeden düşülüp tekrar eklenecek
        /*cursor = db.rawQuery("select subtotal, total from invoice where id=?", new String[]{String.valueOf(invoiceId)});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            double lInvoiceTotal = cursor.getDouble(0);
            double lTotal = cursor.getDouble(1);

            CustomerDao.changeCustomerBalance(customerId, -1* lInvoiceTotal );
        }*/
        //önceki fatura tutarı bakiyeden düşülüp tekrar eklenecek
        CustomerDao.changeCustomerBalance(customerId, -1* invoiceOldAmount );
        //Log.e("InvoiceDao", "updateInvoiceAuto: invoiceOldAmount: " + invoiceOldAmount);

        cursor = db.rawQuery("select sum(total) as total, sum(discount1+discount2+discount3+discount4) as discount, sum(tax) as tax from invoicedetail " +
                "where invoiceid=?", new String[]{String.valueOf(invoiceId)});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            double lTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
            double lDiscount = cursor.getDouble(cursor.getColumnIndexOrThrow("discount"));
            double lTax = cursor.getDouble(cursor.getColumnIndexOrThrow("tax"));

            ContentValues values = new ContentValues();
            values.put("customerid", customerId);
            values.put("invoicenumber", invoiceNumber);
            values.put("invoicedate", invoiceDate);
            values.put("total", lTotal);
            values.put("discount", lDiscount);
            values.put("taxamount", lTax);
            values.put("subtotal", (lTotal - lDiscount) + lTax);

            SQLiteDatabase db1 = Db.getWritableDatabase();
            db1.update("invoice", values, "id=?", new String[]{String.valueOf(invoiceId)});

            //Fatura tutari musteri bakiyesine ekleniyor
            double lInvoiceTotal = (lTotal - lDiscount) + lTax;
            CustomerDao.changeCustomerBalance(customerId, lInvoiceTotal);
        }
    }


    public static void updateInvoiceAuto2(long invoiceId, double invoiceOldAmount,
                                          int customerId, String invoiceNumber, String invoiceDate) {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor;
        //önceki fatura tutarı bakiyeden düşülüp tekrar eklenecek
        /*cursor = db.rawQuery("select subtotal, total from invoice where id=?", new String[]{String.valueOf(invoiceId)});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            double lInvoiceTotal = cursor.getDouble(0);
            double lTotal = cursor.getDouble(1);

            CustomerDao.changeCustomerBalance(customerId, -1* lInvoiceTotal );
        }*/
        //önceki fatura tutarı bakiyeden düşülüp tekrar eklenecek
        // double guncel_lCustomerBalance  =  CustomerDao.changeCustomerBalance2(customerId, -1* invoiceOldAmount );
        //Log.e("InvoiceDao", "updateInvoiceAuto: invoiceOldAmount: " + invoiceOldAmount);

        cursor = db.rawQuery("select sum(total) as total, sum(discount1+discount2+discount3+discount4) as discount, sum(tax) as tax from invoicedetail " +
                "where invoiceid=?", new String[]{String.valueOf(invoiceId)});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            double lTotal = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
            double lDiscount = cursor.getDouble(cursor.getColumnIndexOrThrow("discount"));
            double lTax = cursor.getDouble(cursor.getColumnIndexOrThrow("tax"));

            ContentValues values = new ContentValues();
            values.put("customerid", customerId);
            values.put("invoicenumber", invoiceNumber);
            values.put("invoicedate", invoiceDate);
            values.put("total", lTotal);
            values.put("discount", lDiscount);
            values.put("taxamount", lTax);
            values.put("subtotal", (lTotal - lDiscount) + lTax);

            SQLiteDatabase db1 = Db.getWritableDatabase();
            db1.update("invoice", values, "id=?", new String[]{String.valueOf(invoiceId)});

            //Fatura tutari musteri bakiyesine ekleniyor
            double lInvoiceTotal = (lTotal - lDiscount) + lTax;
            CustomerDao.changeCustomerBalance2(customerId);
        }
    }



    public static String getTotalAmount(long invoiceId) {
        if (Db == null) {
            Db = new Database();
        }
        String lAmountStr = "";
        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor = db.rawQuery("select u.unitename, sum(d.amount) as total, sum(packageamount) as totalpackage from invoicedetail d " +
                "inner join products p on p.id=d.productid " +
                "inner join unites u on u.id=p.uniteid " +
                "where d.invoiceid=? group by u.unitename", new String[]{String.valueOf(invoiceId)});

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

    public static Double getInvoiceTotal (long invoiceId){
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor;

        double lInvoiceTotal = 0;
        cursor = db.rawQuery("select subtotal from invoice where id=?", new String[]{String.valueOf(invoiceId)});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            lInvoiceTotal = cursor.getDouble(0);
        }
        return lInvoiceTotal;
    }

    public static Double getCustomerTotal(long customerId){
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor;

        double lInvoiceTotal = 0;
        cursor = db.rawQuery("select sum(subtotal) as subtotal from invoice where customerid=?", new String[]{String.valueOf(customerId)});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            lInvoiceTotal = cursor.getDouble(0);
        }
        return lInvoiceTotal;
    }


    public static HashMap<String, Double> getInvoiceTotals(long invoiceId) {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getReadableDatabase();
        HashMap<String, Double> map;
        map = new HashMap<String, Double>();

        Cursor cursor = db.rawQuery("select sum(total) as total, sum(discount1+discount2+discount3+discount4) as discount, " +
                "sum(subtotal) as subtotal, sum(tax) as taxamount from invoicedetail " +
                "where invoiceid=?", new String[]{String.valueOf(invoiceId)});
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            map.put("total", cursor.getDouble(cursor.getColumnIndexOrThrow("total")));
            map.put("discount", cursor.getDouble(cursor.getColumnIndexOrThrow("discount")));
            map.put("subtotal", cursor.getDouble(cursor.getColumnIndexOrThrow("subtotal")));
            map.put("taxamount", cursor.getDouble(cursor.getColumnIndexOrThrow("taxamount")));

            cursor.close();
        } else {
            map.put("total", 0.0);
            map.put("discount", 0.0);
            map.put("subtotal", 0.0);
            map.put("taxamount", 0.0);
        }


        return map;
    }

    public static ArrayList<HashMap<String, String>> getInvoiceDetail(long invoiceId) {
        if (Db == null) {
            Db = new Database();
        }
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        SQLiteDatabase db = Db.getReadableDatabase();

        Cursor cursor = db.rawQuery("select i.id, i.productid, i.unitprice, i.packageamount, i.amount, " +
                        "i.total, i.discount1, i.discount2, i.discount3, i.discount4, i.subtotal," +
                        "p.productname, p.origin, u.unitename, taxrate, tax from invoicedetail i " +
                        "inner join products p on p.id=i.productid " +
                        "left join unites u on u.id=p.uniteid " +
                        "where i.invoiceid=? order by i.id ",
                new String[]{String.valueOf(invoiceId)});

        if (cursor.getCount() > 0) {
            HashMap<String, String> map;

            while (cursor.moveToNext()) {
                map = new HashMap<String, String>();
                map.put("id", cursor.getString(cursor.getColumnIndexOrThrow("id")));
                map.put("productid", cursor.getString(cursor.getColumnIndexOrThrow("productid")));
                map.put("productname", cursor.getString(cursor.getColumnIndexOrThrow("productname")));
                map.put("origin", cursor.getString(cursor.getColumnIndexOrThrow("origin")));
                map.put("unitename", cursor.getString(cursor.getColumnIndexOrThrow("unitename")));
                map.put("unitprice", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("unitprice")), 2));
                if (cursor.getInt(cursor.getColumnIndexOrThrow("packageamount")) > 0) {
                    map.put("packageamount", cursor.getString(cursor.getColumnIndexOrThrow("packageamount")) + " Koli");
                } else {
                    map.put("packageamount", " ");
                }
                map.put("amount", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")), 0));
                map.put("total", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("total")), 2));
                map.put("discount1", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("discount1")), 2));
                map.put("discount2", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("discount2")), 2));
                map.put("discount3", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("discount3")), 2));
                map.put("discount4", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("discount4")), 2));
                map.put("subtotal", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("subtotal")), 2));
                map.put("taxrate", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("taxrate")), 0));
                map.put("tax", Variables.doubleToStr(cursor.getDouble(cursor.getColumnIndexOrThrow("tax")), 1));

                list.add(map);
            }
            cursor.close();
        }
        return list;
    }
}
