package com.eqpos.eqentry.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.eqpos.eqentry.tools.JSONProcess;
import com.eqpos.eqentry.tools.SocketProcess;
import com.eqpos.eqentry.tools.Variables;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Created by dursu on 01.04.2018.
 */

public class SendDao {


    public static void checkProducts(int MinId) {
        Database Db = new Database();

        SQLiteDatabase db = Db.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("changed", 1);
        values.put("isnew", 1);

        db.update("products", values, "id>=?", new String[]{String.valueOf(MinId)});
        db.update("productprices", values, "productid>=?", new String[]{String.valueOf(MinId)});
//        db.rawQuery("update products set changed=1, isnew=1 where id>?",  new String[] {String.valueOf(MinId)});
//        db.rawQuery("update productprices set changed=1, isnew=1 where productid>?",  new String[] {String.valueOf(MinId)});
    }

    public static void sendProducts() {
        JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdSaveProducts.getValue());
        JsonObject jData = new JsonObject();
        JsonObject jElement;
        JsonArray jArr = new JsonArray();

        Database Db = new Database();
        SQLiteDatabase db;
        Cursor cursor;
        int offset = 0;
        int limit = 100;
        int lSayi = 0;
        do {
            jData = null;
            jElement = null;
            jArr = null;
            jData = new JsonObject();
            jArr = new JsonArray();

            db = Db.getReadableDatabase();
            String lSql = "select p.id, groupid, priceorder, stockcode, barcode, plu, productname, costprice, " +
                    "taxid, depositeid, uniteid, p.isnew, f.newprice, p.description, p.unitamount, p.amountunite, p.origin " +
                    ",p.varyant_anagrupid AS variant_anagrupid, p.varyant_altgrupid AS variant_altgrupid " +
                    "from products p " +
                    "left join productprices f on f.productid=p.id and f.priceorder=1 " +
                    "where p.changed=1 or p.isnew=1 order by p.id limit " + String.valueOf(limit);// + " offset " + String.valueOf(offset);
            cursor = db.rawQuery(lSql, null);

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    jElement = new JsonObject();
                    jElement.addProperty("id", cursor.getInt(0));
                    jElement.addProperty("groupid", cursor.getInt(1));
                    jElement.addProperty("priceorder", cursor.getInt(2));
                    jElement.addProperty("stockcode", cursor.getString(3));
                    jElement.addProperty("barcode", cursor.getString(4));
                    jElement.addProperty("plu", cursor.getString(5));
                    jElement.addProperty("productname", cursor.getString(6));
                    jElement.addProperty("costprice", cursor.getDouble(7));
                    jElement.addProperty("taxid", cursor.getInt(8));
                    jElement.addProperty("depositeid", cursor.getInt(9));
                    jElement.addProperty("uniteid", cursor.getInt(10));
                    jElement.addProperty("isnew", cursor.getInt(11));
                    jElement.addProperty("sellprice", cursor.getDouble(12));
                    jElement.addProperty("description", cursor.getString(13));
                    jElement.addProperty("unitamount", cursor.getDouble(14));
                    jElement.addProperty("amountunite", cursor.getString(15));
                    jElement.addProperty("origin", cursor.getString(16));
                    jElement.addProperty("variant_anagrupid", cursor.getInt(17));
                    jElement.addProperty("variant_altgrupid", cursor.getInt(18));
                    jArr.add(jElement);
                }
            } else return;

            jData.addProperty("datas", jArr.toString());

            String msg = JSONProcess.jsonPack(jHead, jData);

            if (jHead != null) {
                String rMsg = SocketProcess.sendMessage(msg);
                if (!rMsg.contains(Variables._RETURNFAULT)) {
                    //Geriye yeni ürünlerin yeni IDsi geliyor onlar Veritabanına işlenecek

                    JsonParser parser = new JsonParser();
                    try {
                        JsonArray lArr = parser.parse(rMsg).getAsJsonArray();

                        db = Db.getWritableDatabase();

                        ContentValues value = new ContentValues();

                        String lCode = "";
                        for (int i = 0; i < lArr.size(); i++) {
                            jData = lArr.get(i).getAsJsonObject();

                            //value = new ContentValues();
                            int lNewId = jData.get("newid").getAsInt();
                            int lOldId = jData.get("oldid").getAsInt();
                            lCode = "";
                            if (jData.has("stockcode")) {
                                lCode = jData.get("stockcode").getAsString();
                            }

                            value.clear();
                            value.put("id", lNewId);
                            value.put("stockcode", lCode);
                            value.put("changed", 0);
                            value.put("isnew", 0);
                            value.put("printlabel", 0);
                            db.update("products", value, "id=?", new String[]{String.valueOf(lOldId)});

                            value.clear();
                            //                    value.put("productid", lNewId);
                            //                    value.put("priceorder", 1);
                            //                    value.put("changed", 0);
                            //                    value.put("isnew", 0);
                            //                    db.update("productprices", value, "productid=?", new String[] {String.valueOf(lOldId)});
                            db.execSQL("update productprices set price=newprice, isnew=0, changed=0 , productid=" + String.valueOf(lNewId) +
                                    " where productid=? and priceorder=1", new String[]{String.valueOf(lOldId)});

                            value.clear();
                            value.put("productid", lNewId);
                            db.update("invoicedetail", value, "productid=?", new String[]{String.valueOf(lOldId)});

                            value.put("productid", lNewId);
                            db.update("deliverydetail", value, "productid=?", new String[]{String.valueOf(lOldId)});

                            value.put("productid", lNewId);
                            db.update("purchaseorder", value, "productid=?", new String[]{String.valueOf(lOldId)});
                        }
                        parser = null;
                        lArr = null;
                    } catch (JsonParseException e) {
                        e.printStackTrace();
                    }

                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        do {
                            ContentValues value = new ContentValues();
                            value.clear();
                            value.put("changed", 0);
                            db = Db.getWritableDatabase();
                            db.update("products", value, "id=?", new String[]{cursor.getString(0)});
                        } while (cursor.moveToNext());
                    }

                } else {

                }
            }

            offset += 100;


        } while (cursor.getCount() > 0);

        //       db = Db.getWritableDatabase();
//                    value.put("changed", 0);
//                    value.put("printlabel", 0);
//                    db.update("products", value, "changed=1 and isnew=0", null);
        jData = null;
        jHead = null;
        jArr = null;
    }


    public static boolean sendProductsVaryants() {
        JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdSaveProducts.getValue());
        JsonObject jData = new JsonObject();
        JsonArray jArr = new JsonArray();

        Database Db = new Database();
        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor = null;

        try {
            // SQL sorgusu
            String lSql = "select p.id, groupid, priceorder, stockcode, barcode, plu, productname, costprice, " +
                    "taxid, depositeid, uniteid, p.isnew, f.newprice, p.description, p.unitamount, p.amountunite, p.origin, " +
                    "p.varyant_anagrupid AS variant_anagrupid, p.varyant_altgrupid AS variant_altgrupid " +
                    "from products p " +
                    "left join productprices f on f.productid=p.id and f.priceorder=1 " +
                    "where p.changed=1 or p.isnew=1 order by p.id limit 100";

            cursor = db.rawQuery(lSql, null);

            // Veriler varsa JSON dizisine ekle
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    JsonObject jElement = new JsonObject();
                    jElement.addProperty("id", cursor.getInt(0));
                    jElement.addProperty("groupid", cursor.getInt(1));
                    jElement.addProperty("priceorder", cursor.getInt(2));
                    jElement.addProperty("stockcode", cursor.getString(3));
                    jElement.addProperty("barcode", cursor.getString(4));
                    jElement.addProperty("plu", cursor.getString(5));
                    jElement.addProperty("productname", cursor.getString(6));
                    jElement.addProperty("costprice", cursor.getDouble(7));
                    jElement.addProperty("taxid", cursor.getInt(8));
                    jElement.addProperty("depositeid", cursor.getInt(9));
                    jElement.addProperty("uniteid", cursor.getInt(10));
                    jElement.addProperty("isnew", cursor.getInt(11));
                    jElement.addProperty("sellprice", cursor.getDouble(12));
                    jElement.addProperty("description", cursor.getString(13));
                    jElement.addProperty("unitamount", cursor.getDouble(14));
                    jElement.addProperty("amountunite", cursor.getString(15));
                    jElement.addProperty("origin", cursor.getString(16));
                    jElement.addProperty("variant_anagrupid", cursor.getInt(17));
                    jElement.addProperty("variant_altgrupid", cursor.getInt(18));
                    jArr.add(jElement);
                }
            } else {
                // Veri yoksa false döndür
                return false;
            }

            jData.addProperty("datas", jArr.toString());
            String msg = JSONProcess.jsonPack(jHead, jData);

            if (jHead != null) {
                String rMsg = SocketProcess.sendMessage(msg);
                if (!rMsg.contains(Variables._RETURNFAULT)) {
                    // Başarılı yanıt alındı, yeni ID'leri işle
                    JsonParser parser = new JsonParser();
                    try {
                        JsonArray lArr = parser.parse(rMsg).getAsJsonArray();
                        db = Db.getWritableDatabase();
                        ContentValues value = new ContentValues();

                        for (int i = 0; i < lArr.size(); i++) {
                            JsonObject jDataItem = lArr.get(i).getAsJsonObject();
                            int lNewId = jDataItem.get("newid").getAsInt();
                            int lOldId = jDataItem.get("oldid").getAsInt();
                            String lCode = jDataItem.has("stockcode") ? jDataItem.get("stockcode").getAsString() : "";

                            // products tablosunu güncelle
                            value.clear();
                            value.put("id", lNewId);
                            value.put("stockcode", lCode);
                            value.put("changed", 0);
                            value.put("isnew", 0);
                            value.put("printlabel", 0);
                            db.update("products", value, "id=?", new String[]{String.valueOf(lOldId)});

                            // productprices tablosunu güncelle
                            db.execSQL("update productprices set price=newprice, isnew=0, changed=0, productid=" + lNewId +
                                    " where productid=? and priceorder=1", new String[]{String.valueOf(lOldId)});

                            // Diğer tabloları güncelle
                            value.clear();
                            value.put("productid", lNewId);
                            db.update("invoicedetail", value, "productid=?", new String[]{String.valueOf(lOldId)});
                            db.update("deliverydetail", value, "productid=?", new String[]{String.valueOf(lOldId)});
                            db.update("purchaseorder", value, "productid=?", new String[]{String.valueOf(lOldId)});
                        }

                        // products tablosunda changed=0 yap
                        if (cursor.moveToFirst()) {
                            do {
                                value.clear();
                                value.put("changed", 0);
                                db.update("products", value, "id=?", new String[]{cursor.getString(0)});
                            } while (cursor.moveToNext());
                        }

                        return true; // İşlem başarılı
                    } catch (JsonParseException e) {
                        e.printStackTrace();
                        return false; // JSON parse hatası
                    } finally {
                        parser = null;
                    }
                } else {
                    return false; // Sunucudan hata yanıtı alındı
                }
            } else {
                return false; // JSON başlığı null
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
            Db.close();
        }

    }


    public static void sendChangedPrices() {
        JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdChangePrice.getValue());
        JsonObject jData = new JsonObject();
        JsonObject jElement;
        JsonArray jArr = new JsonArray();
        int limit = 100;

        Database Db = new Database();

        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor;
        String lSql = "";
        do {
            jData = null;
            jArr = null;
            jData = new JsonObject();
            jArr = new JsonArray();
            lSql = "select productid, priceorder, description, price, newprice from productprices " +
                    "where changed=1 order by productid limit " + String.valueOf(limit);// + " offset " + String.valueOf(offset);
            cursor = db.rawQuery(lSql, null);

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    jElement = new JsonObject();
                    jElement.addProperty("productid", cursor.getInt(0));
                    jElement.addProperty("priceorder", cursor.getInt(1));
                    jElement.addProperty("description", cursor.getString(2));
                    jElement.addProperty("oldprice", cursor.getDouble(3));
                    jElement.addProperty("newprice", cursor.getDouble(4));

                    jArr.add(jElement);
                }
            }

            jData.addProperty("datas", jArr.toString());

            String msg = JSONProcess.jsonPack(jHead, jData);

            if (jHead != null) {
                String rMsg = SocketProcess.sendMessage(msg);
                if (rMsg.contains(Variables._RETURNOK)) {
                    db = Db.getWritableDatabase();
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        do {
                            db.execSQL("update productprices set price=newprice, changed=0 " +
                                    "where changed=1 and productid=" + cursor.getString(0) + " and " +
                                    "priceorder=" + cursor.getString(1));
                        } while (cursor.moveToNext());
                    }
                } else {

                }
            }
        } while (cursor.getCount() > 0);
        jData = null;
        jArr = null;
        jHead = null;
    }

    public static void sendPrintLabel() {
        JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdPrintLabel.getValue());
        JsonObject jData = new JsonObject();
        JsonObject jElement;
        JsonArray jArr = new JsonArray();
        int limit = 100;

        Database Db = new Database();

        SQLiteDatabase db = Db.getReadableDatabase();

        Cursor cursor;
        String lSql = "";
        do {
            jData = null;
            jArr = null;
            jData = new JsonObject();
            jArr = new JsonArray();
            lSql = "select id from products where printlabel=1 order by id limit " + String.valueOf(limit);// + " offset " + String.valueOf(offset);
            cursor = db.rawQuery(lSql, null);

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    jElement = new JsonObject();
                    jElement.addProperty("productid", cursor.getInt(0));

                    jArr.add(jElement);
                }
            }

            jData.addProperty("datas", jArr.toString());

            String msg = JSONProcess.jsonPack(jHead, jData);

            if (jHead != null) {
                String rMsg = SocketProcess.sendMessage(msg);
                if (rMsg.contains(Variables._RETURNOK)) {
                    db = Db.getWritableDatabase();
                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();
                        do {
                            db.execSQL("update products set printlabel=0 where printlabel=1 and id=" + cursor.getString(0));
                        } while (cursor.moveToNext());
                    }
                } else {

                }
            }
        } while (cursor.getCount() > 0);
        jData = null;
        jHead = null;
        jArr = null;
    }

    public static void sendCustomers() {
        JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdSaveCustomers.getValue());
        JsonObject jData = new JsonObject();
        JsonObject jElement;
        JsonArray jArr = new JsonArray();

        Database Db = new Database();

        SQLiteDatabase db = Db.getReadableDatabase();
        String lSql = "select id, customername, phone1, phone2, address1, email, owner, taxid, taxoffice from customers " +
                "where isnew=1";
        Cursor cursor = db.rawQuery(lSql, null);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                jElement = new JsonObject();
                jElement.addProperty("id", cursor.getInt(0));
                jElement.addProperty("customername", cursor.getString(1));
                jElement.addProperty("phone1", cursor.getString(2));
                jElement.addProperty("phone2", cursor.getString(3));
                jElement.addProperty("address1", cursor.getString(4));
                jElement.addProperty("email", cursor.getString(5));
                jElement.addProperty("owner", cursor.getString(6));
                jElement.addProperty("taxid", cursor.getString(7));
                jElement.addProperty("taxoffice", cursor.getString(8));

                jArr.add(jElement);
            }
            cursor.close();
        }

        jData.addProperty("datas", jArr.toString());

        String msg = JSONProcess.jsonPack(jHead, jData);

        if (jHead != null) {
            String rMsg = SocketProcess.sendMessage(msg);
            if (!rMsg.contains(Variables._RETURNFAULT)) {
                //Geriye yeni ürünlerin yeni IDsi geliyor onlar Veritabanına işlenecek
                JsonParser parser = new JsonParser();
                JsonArray lArr = parser.parse(rMsg).getAsJsonArray();

                db = Db.getWritableDatabase();
                for (int i = 0; i < lArr.size(); i++) {
                    jData = lArr.get(i).getAsJsonObject();

                    ContentValues value = new ContentValues();
                    int lNewId = jData.get("newid").getAsInt();
                    int lOldId = jData.get("oldid").getAsInt();

                    value.put("id", lNewId);
                    value.put("isnew", 0);
                    db.update("customers", value, "id=?", new String[]{String.valueOf(lOldId)});

                    value.clear();
                    value.put("customerid", lNewId);
                    db.update("invoice", value, "customerid=?", new String[]{String.valueOf(lOldId)});

                    value.put("customerid", lNewId);
                    db.update("collects", value, "customerid=?", new String[]{String.valueOf(lOldId)});

                }
                parser = null;
                lArr = null;
            }
        }

        jData = null;
        jHead = null;
        jArr = null;
    }


    public static void sendCollects() {
        JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdSaveCollects.getValue());
        JsonObject jData;
        JsonObject jElement;
        JsonArray jArr;

        Database Db = new Database();

        SQLiteDatabase db = Db.getReadableDatabase();
        String lSql = "select id, documentnumber, collectdate, description, customerid, paymenttype, amount, collecttime from collects ";
        Cursor cursor = db.rawQuery(lSql, null);
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                jData = new JsonObject();
                jArr = new JsonArray();

                jElement = new JsonObject();
                jElement.addProperty("id", cursor.getInt(0));
                jElement.addProperty("documentnumber", cursor.getString(1));
                jElement.addProperty("collectdate", cursor.getString(2) + " " + cursor.getString(7));
                jElement.addProperty("description", cursor.getString(3));
                jElement.addProperty("customerid", cursor.getInt(4));
                jElement.addProperty("paymenttype", cursor.getInt(5));
                jElement.addProperty("amount", cursor.getDouble(6));

                jArr.add(jElement);

                jData.addProperty("datas", jArr.toString());

                String msg = JSONProcess.jsonPack(jHead, jData);

                if (jHead != null) {
                    String rMsg = SocketProcess.sendMessage(msg);
                    if (rMsg.contains(Variables._RETURNOK)) {
                        db = Db.getWritableDatabase();
                        db.delete("collects", "id=?", new String[]{cursor.getString(0)});
                    }
                }
                jData = null;
                jArr = null;

            }
            cursor.close();
        }
        jHead = null;
    }

    public static void sendDelivery(int deliveyId) {
        JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdInputDelivery.getValue());
        JsonObject jData = new JsonObject();
        JsonObject jElement;
        JsonArray jArr = new JsonArray();

        Database Db = new Database();

        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor = db.query("delivery",
                new String[]{"documentdate", "documentnumber", "supplierid", "receiver", "documenttime", "warehouseid"},
                "id=?", new String[]{String.valueOf(deliveyId)}, "", "", "");
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            jHead.addProperty("documentdate", cursor.getString(0) + " " + cursor.getString(4));
            jHead.addProperty("documentnumber", cursor.getString(1));
            jHead.addProperty("supplierid", cursor.getInt(2));
            jHead.addProperty("receiver", cursor.getString(3));
            jHead.addProperty("documenttime", cursor.getString(4));
            jHead.addProperty("warehouseid", cursor.getInt(5));
            cursor.close();
        }

        cursor = db.query("deliverydetail",
                new String[]{"deliveryid", "productid", "partnumber", "expirationdate", "amount", "costprice", "total"},
                "deliveryid=?", new String[]{String.valueOf(deliveyId)}, "", "", "id");

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                jElement = new JsonObject();
                jElement.addProperty("deliveryid", cursor.getInt(0));
                jElement.addProperty("productid", cursor.getInt(1));
                jElement.addProperty("partnumber", cursor.getString(2));
                jElement.addProperty("expirationdate", cursor.getString(3));
                jElement.addProperty("amount", cursor.getDouble(4));
                jElement.addProperty("costprice", cursor.getDouble(5));
                jElement.addProperty("total", cursor.getDouble(6));

                jArr.add(jElement);
            }
            cursor.close();
        }

        jData.addProperty("datas", jArr.toString());

        String msg = JSONProcess.jsonPack(jHead, jData);

        if (jHead != null) {
            String rMsg = SocketProcess.sendMessage(msg);
            if (rMsg.contains(Variables._RETURNOK)) {
                db = Db.getWritableDatabase();
                db.delete("delivery", "id=?", new String[]{String.valueOf(deliveyId)});
                db.delete("deliverydetail", "deliveryid=?", new String[]{String.valueOf(deliveyId)});
            } else {

            }
        }
        jData = null;
        jHead = null;
        jArr = null;
    }


    public static void sendAllDelivery() {
        Database Db = new Database();

        int deliveryId = 0;
        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor = db.query("delivery",
                new String[]{"id"}, "supplierid>0", null, "", "", "");
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                deliveryId = cursor.getInt(0);
                sendDelivery(deliveryId);
            }
        }
        cursor.close();
    }

    public static void sendStockTransfer(int stockStransferId) {
        JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdStockTransfer.getValue());
        JsonObject jData = new JsonObject();
        JsonObject jElement;
        JsonArray jArr = new JsonArray();

        Database Db = new Database();

        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor = db.query("stocktransfer",
                new String[]{"documentnumber", "targetwareid"},
                "id=?", new String[]{String.valueOf(stockStransferId)}, "", "", "");
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            jHead.addProperty("documentnumber", cursor.getString(0));
            jHead.addProperty("targetwareid", cursor.getInt(1));
            cursor.close();
        }

        cursor = db.query("stocktransferdetail",
                new String[]{"productid", "amount"}, "stocktransferid=?",
                new String[]{String.valueOf(stockStransferId)}, "", "", "id");

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                jElement = new JsonObject();
                jElement.addProperty("productid", cursor.getInt(0));
                jElement.addProperty("amount", cursor.getDouble(1));

                jArr.add(jElement);
            }
            cursor.close();
        }

        jData.addProperty("datas", jArr.toString());

        String msg = JSONProcess.jsonPack(jHead, jData);

        if (jHead != null) {
            String rMsg = SocketProcess.sendMessage(msg);
            if (rMsg.contains(Variables._RETURNOK)) {
                db = Db.getWritableDatabase();
                db.delete("stocktransfer", "id=?", new String[]{String.valueOf(stockStransferId)});
                db.delete("stocktransferdetail", "stocktransferid=?", new String[]{String.valueOf(stockStransferId)});
            } else {

            }
        }
        jData = null;
        jHead = null;
        jArr = null;
    }


    public static void sendAllStockTransfer() {
        if (Dao.Language != "tr") {
            return;
        }
        Database Db = new Database();

        int stockTransferId = 0;
        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor = db.query("stocktransfer",
                new String[]{"id"}, "", null, "", "", "");
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                stockTransferId = cursor.getInt(0);
                sendStockTransfer(stockTransferId);
            }
        }
        cursor.close();
    }


    public static void sendOrder() {
        JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdSaveOrder.getValue());
        JsonObject jData = null;
        JsonObject jElement;
        JsonArray jArr = null;

        Database Db = new Database();
        int limit = 10;
        int lOrderId = 0;
        int lLastId = 0;

        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor;

        do {
            jHead.addProperty("orderid", lOrderId);
            db = Db.getReadableDatabase();
            cursor = db.rawQuery("select po.productid, p.productname, p.barcode, p.plu, po.amount, po.unit, po.supplierid, po.id from purchaseorder po " +
                    "inner join products p on p.id=po.productid where supplierid > 0 order by po.id limit " + String.valueOf(limit), null);

            jData = new JsonObject();
            jArr = new JsonArray();
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    jElement = new JsonObject();
                    jElement.addProperty("productid", cursor.getInt(0));
                    jElement.addProperty("productname", cursor.getString(1));
                    jElement.addProperty("barcode", cursor.getString(2));
                    jElement.addProperty("plu", cursor.getString(3));
                    jElement.addProperty("amount", cursor.getDouble(4));
                    jElement.addProperty("unit", cursor.getString(5));
                    jElement.addProperty("supplierid", cursor.getInt(6));

                    lLastId = cursor.getInt(7);
                    jArr.add(jElement);
                }
                cursor.close();
            } else return;

            jData.addProperty("datas", jArr.toString());

            String msg = JSONProcess.jsonPack(jHead, jData);

            if (jHead != null) {
                String rMsg = SocketProcess.sendMessage(msg);
                if (rMsg.contains(Variables._RETURNOK)) {
                    String lStr = rMsg.replace(Variables._RETURNOK, "");
                    lOrderId = Integer.parseInt(lStr);
                    db = Db.getWritableDatabase();
                    db.execSQL("delete from purchaseorder where supplierid > 0 and id<=" + String.valueOf(lLastId));
                } else {

                }
            }
            jData = null;
            jArr = null;
        } while (cursor.getCount() > 0);

        jData = null;
        jHead = null;
        jArr = null;
    }


    public static void sendInventur() {
        int lInventurId = 0;
        int lCount = 0;
        String rMsg = "";
        Database Db = new Database();

        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor = db.rawQuery("select productid, newquantity, warehouseid from inventur where difference<>0", null);
        JsonArray jArr = null;
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {

                JsonObject jElement;
                if (jArr == null) {
                    jArr = new JsonArray();
                }

                jElement = new JsonObject();
                jElement.addProperty("productid", cursor.getInt(0));
                jElement.addProperty("newquantity", cursor.getDouble(1));
                // jElement.addProperty("warehouseid", cursor.getInt(2));

                jArr.add(jElement);

                lCount += 1;
                if (lCount % 200 == 0 || lCount >= cursor.getCount()) {

                    JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdSaveInventur.getValue());
                    jHead.addProperty("inventurid", lInventurId);
                    jHead.addProperty("warehouseid", cursor.getInt(2));

                    JsonObject jData = new JsonObject();
                    jData.addProperty("datas", jArr.toString());

                    String msg = JSONProcess.jsonPack(jHead, jData);

                    if (jHead != null) {
                        rMsg = SocketProcess.sendMessage(msg);
                        if (rMsg.contains(Variables._RETURNOK)) {
                            lInventurId = Integer.parseInt(rMsg.replace(Variables._RETURNOK + "-", ""));
                        }
                    }
                    jData = null;
                    jHead = null;
                    jArr = null;
                }
            }
            cursor.close();
        }

        // 30.10.2025: Sonerin istegi uzerine gönderimden sonra delete olayi kaldirildi
//        if (rMsg.contains(Variables._RETURNOK)) {
//            db = Db.getWritableDatabase();
//            db.delete("inventur", "", null);
//        }


    }


    public static boolean sendInventurSayimdakini(int productId) {
        boolean result = false;
        int lInventurId = 0;
        int lCount = 0;
        String rMsg = "";
        Database Db = new Database();

        SQLiteDatabase db = Db.getReadableDatabase();
        String sql = "select productid, newquantity, warehouseid from inventur where difference<>0 AND productid='" + productId + "';";
        Cursor cursor = db.rawQuery(sql, null);
        JsonArray jArr = null;
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {

                JsonObject jElement;
                if (jArr == null) {
                    jArr = new JsonArray();
                }

                jElement = new JsonObject();
                jElement.addProperty("productid", cursor.getInt(0));
                jElement.addProperty("newquantity", cursor.getDouble(1));
                // jElement.addProperty("warehouseid", cursor.getInt(2));

                jArr.add(jElement);

                lCount += 1;
                if (lCount % 200 == 0 || lCount >= cursor.getCount()) {

                    JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdSaveInventur.getValue());
                    jHead.addProperty("inventurid", lInventurId);
                    jHead.addProperty("warehouseid", cursor.getInt(2));

                    JsonObject jData = new JsonObject();
                    jData.addProperty("datas", jArr.toString());

                    String msg = JSONProcess.jsonPack(jHead, jData);

                    if (jHead != null) {
                        rMsg = SocketProcess.sendMessage(msg);
                        if (rMsg.contains(Variables._RETURNOK)) {
                            lInventurId = Integer.parseInt(rMsg.replace(Variables._RETURNOK + "-", ""));
                            result = true;
                        }
                    }
                    jData = null;
                    jHead = null;
                    jArr = null;
                }
            }
            cursor.close();
        }

        return result;
    }

    public static boolean sendInventur2_SayimdaKullaniyim() {
        boolean result = false;
        int lInventurId = 0;
        int lCount = 0;
        String rMsg = "";
        Database Db = new Database();

        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor = db.rawQuery("select productid, newquantity, warehouseid from inventur where difference<>0", null);
        JsonArray jArr = null;
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {

                JsonObject jElement;
                if (jArr == null) {
                    jArr = new JsonArray();
                }

                jElement = new JsonObject();
                jElement.addProperty("productid", cursor.getInt(0));
                jElement.addProperty("newquantity", cursor.getDouble(1));
                // jElement.addProperty("warehouseid", cursor.getInt(2));

                jArr.add(jElement);

                lCount += 1;
                if (lCount % 200 == 0 || lCount >= cursor.getCount()) {

                    JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdSaveInventur.getValue());
                    jHead.addProperty("inventurid", lInventurId);
                    jHead.addProperty("warehouseid", cursor.getInt(2));

                    JsonObject jData = new JsonObject();
                    jData.addProperty("datas", jArr.toString());

                    String msg = JSONProcess.jsonPack(jHead, jData);

                    if (jHead != null) {
                        rMsg = SocketProcess.sendMessage(msg);
                        if (rMsg.contains(Variables._RETURNOK)) {
                            lInventurId = Integer.parseInt(rMsg.replace(Variables._RETURNOK + "-", ""));
                            result = true;
                        }
                    }
                    jData = null;
                    jHead = null;
                    jArr = null;
                }
            }
            cursor.close();
        }

        return result;
    }


    public static void sendInvoice(int invoiceId) {
        JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdSaveInvoice.getValue());
        JsonObject jData = new JsonObject();
        JsonObject jElement;
        JsonArray jArr = new JsonArray();

        Database Db = new Database();

        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor = db.query("invoice",
                new String[]{"invoicenumber", "invoicedate", "customerid", "total", "discount", "taxamount", "subtotal", "includetax", "invoicetime", "warehouseid"},
                "id=?", new String[]{String.valueOf(invoiceId)}, "", "", "");
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            jHead.addProperty("invoicenumber", cursor.getString(0));
            jHead.addProperty("invoicedate", cursor.getString(1) + " " + cursor.getString(8));
            jHead.addProperty("customerid", cursor.getInt(2));
            jHead.addProperty("total", cursor.getDouble(3));
            jHead.addProperty("discount", cursor.getDouble(4));
            jHead.addProperty("taxamount", cursor.getDouble(5));
            jHead.addProperty("subtotal", cursor.getDouble(6));
            jHead.addProperty("includetax", cursor.getInt(7));
            jHead.addProperty("invoicetime", cursor.getString(8));
            jHead.addProperty("warehouseid", cursor.getInt(9));
            cursor.close();
        }

        cursor = db.query("invoicedetail",
                new String[]{"invoiceid", "productid", "unitprice", "amount", "total", "discount1", "discount2", "discount3", "discount4", "subtotal", "tax", "taxrate"},
                "invoiceid=?", new String[]{String.valueOf(invoiceId)}, "", "", "id");


        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                jElement = new JsonObject();
                jElement.addProperty("invoiceid", cursor.getInt(0));
                jElement.addProperty("productid", cursor.getInt(1));
                jElement.addProperty("unitprice", cursor.getDouble(2));
                jElement.addProperty("amount", cursor.getDouble(3));
                jElement.addProperty("total", cursor.getDouble(4));
                jElement.addProperty("discount1", cursor.getDouble(5));
                jElement.addProperty("discount2", cursor.getDouble(6));
                jElement.addProperty("discount3", cursor.getDouble(7));
                jElement.addProperty("discount4", cursor.getDouble(8));
                jElement.addProperty("subtotal", cursor.getDouble(9));
                jElement.addProperty("tax", cursor.getDouble(10));
                jElement.addProperty("taxrate", cursor.getDouble(11));

                jArr.add(jElement);
            }
            cursor.close();
        }

        jData.addProperty("datas", jArr.toString());

        String msg = JSONProcess.jsonPack(jHead, jData);

        if (jHead != null) {
            String rMsg = SocketProcess.sendMessage(msg);
            if (rMsg.contains(Variables._RETURNOK)) {
                db = Db.getWritableDatabase();
                db.delete("invoice", "id=?", new String[]{String.valueOf(invoiceId)});
                db.delete("invoicedetail", "invoiceid=?", new String[]{String.valueOf(invoiceId)});
            } else {

            }
        }
        jData = null;
        jHead = null;
        jArr = null;
    }


    public static void sendAllInvoice() {
        Database Db = new Database();

        int invoiceId = 0;
        SQLiteDatabase db = Db.getReadableDatabase();
        Cursor cursor = db.query("invoice",
                new String[]{"id"}, "customerid>0", null, "", "", "");
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                invoiceId = cursor.getInt(0);
                sendInvoice(invoiceId);
            }
        }
        cursor.close();
    }

}
