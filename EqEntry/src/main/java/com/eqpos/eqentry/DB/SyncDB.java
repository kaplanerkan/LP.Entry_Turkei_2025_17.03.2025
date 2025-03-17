package com.eqpos.eqentry.DB;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.eqpos.eqentry.BuildConfig;
import com.eqpos.eqentry.R;
import com.eqpos.eqentry.Settings;
import com.eqpos.eqentry.tools.JSONProcess;
import com.eqpos.eqentry.tools.SocketProcess;
import com.eqpos.eqentry.tools.Variables;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.google.gson.JsonObject;

import com.eqpos.eqentry.DB.ProductDao;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by dursu on 23.02.2018.
 * <p>
 * Bu sınıfta Veritabanı senkronizasyon rutinleri olacak
 */

public class SyncDB {
    private ProgressDialog prgDialog;
    private static Context context = null;
    private Toast msg;


    public SyncDB(Context context, String dummy) {
        SyncDB.context = context;
        msg = Toast.makeText(SyncDB.context, "", Toast.LENGTH_SHORT);
        prgDialog = ProgressDialog.show(SyncDB.context, "", context.getString(R.string.updating_the_customer), true, false);

        new Thread(() -> {
            try {
                Thread.sleep(500);

            } catch (Exception e) {
            }
        });

        new Thread() {
            public void run() {
                try {
                    Thread.sleep(2000);

                } catch (Exception e) {
                }

                try {
                   syncCustomers();
                } catch (Exception e) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(SyncDB.context);
                    builder1.setMessage(SyncDB.context.getString(R.string.cannot_connect_to_the_server));
                    builder1.setCancelable(true);
                    builder1.setPositiveButton("Ok",
                            (dialog, id) -> dialog.cancel());
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                    e.printStackTrace();
                    try {
                        SocketProcess.client.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    SocketProcess.client = null;
                    e.printStackTrace();
                }

                prgDialog.dismiss();

                msg.cancel();
                msg.setText(R.string.update_was_done);
                msg.show();
            }
        }.start();




    }

    public SyncDB(Context context) {
        SyncDB.context = context;
        msg = Toast.makeText(SyncDB.context, "", Toast.LENGTH_SHORT);
        prgDialog = ProgressDialog.show(SyncDB.context, "", context.getString(R.string.updating_the_database), true, false);

        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }
            }
        });


        new Thread() {
            public void run() {
                try {
                    Thread.sleep(2000);

                } catch (Exception e) {
                }

                try {
                    syncTables();

                    ProductDao.deleteAll();

                    Thread.sleep(2000);

                    if (BuildConfig.DEBUG) {
                        syncUnites();
                        syncTaxes();
                        syncDeposites();
                        syncProductGroups();
//                        syncProducts();
                        syncProductPrices();
                        syncProducts();
                        syncSuppliers();
                        syncCustomers();
                        syncSettings();
                        syncFirmInfo();
                        syncWarehouses();

                    } else {
                        syncUnites();
                        syncTaxes();
                        syncDeposites();
                        syncProductGroups();
                        syncProductPrices();
                        syncProducts();

                        syncSuppliers();
                        syncCustomers();
                        syncSettings();
                        syncFirmInfo();
                        syncWarehouses();
                    }

                    SettingsDao.setStrValue("userpassword", "");

                } catch (Exception e) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(SyncDB.context);
                    builder1.setMessage(SyncDB.context.getString(R.string.cannot_connect_to_the_server));
                    builder1.setCancelable(true);
                    builder1.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                    e.printStackTrace();
                    try {
                        SocketProcess.client.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    SocketProcess.client = null;
                    e.printStackTrace();
                }

                prgDialog.dismiss();

                msg.cancel();
                msg.setText(R.string.update_was_done);
                msg.show();
            }
        }.start();
    }

    private void syncUnites() {
        JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdGetUnits.getValue());
        JsonObject jData = new JsonObject();

        String msg = JSONProcess.jsonPack(jHead, jData);

        if (jHead != null) {
            String rMsg = SocketProcess.sendMessage(msg);
            if (rMsg.contains(Variables._ERROR)) {
                //hata oluştu
            } else {
                //Veriler geldi Veritabanına yazılacak
                try {
                    try {
                        JsonParser parser = new JsonParser();
                        JsonArray lArr = parser.parse(rMsg).getAsJsonArray();
                        ProductDao.saveUnite(lArr);
                    } catch (IllegalStateException e) {
                        Log.e("Error syncUnites", e.toString());
                    }
                } catch (JsonParseException e) {
                    Log.e("Error syncUnites", e.toString());
                }
            }
        }
    }
    private void syncWarehouses() {
        JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdWarehouses.getValue());
        JsonObject jData = new JsonObject();

        String msg = JSONProcess.jsonPack(jHead, jData);

        if (jHead != null) {
            String rMsg = SocketProcess.sendMessage(msg);
            if (rMsg.isEmpty() || rMsg.contains(Variables._ERROR)) {
                //hata oluştu
            } else {
                //Veriler geldi Veritabanına yazılacak
                try {
                    try {
                        JsonParser parser = new JsonParser();
                        JsonArray lArr = parser.parse(rMsg).getAsJsonArray();
                        StockTransferDao.saveWarehouse(lArr);
                    } catch (IllegalStateException e) {
                        Log.e("Error syncWarehouse", e.toString());
                    }
                } catch (JsonParseException e) {
                    Log.e("Error syncWarehouse", e.toString());
                }
            }
        }
    }

    private void syncFirmInfo() {
        JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdGetFirmInfo.getValue());
        JsonObject jData = new JsonObject();

        String msg = JSONProcess.jsonPack(jHead, jData);

        if (jHead != null) {
            String rMsg = SocketProcess.sendMessage(msg);
            if (rMsg.contains(Variables._ERROR)) {
                //hata oluştu
            } if (rMsg.contains(Variables._RETURNFAULT)) {
                //hata oluştu
            } else {
                //Veriler geldi Veritabanına yazılacak
                try {
                    try {
                        JsonParser parser = new JsonParser();
                        JsonArray lArr = parser.parse(rMsg).getAsJsonArray();
                        Dao.saveFirmInfo(lArr);
                    } catch (IllegalStateException e) {
                        Log.e("Error syncUnites", e.toString());
                    }
                } catch (JsonParseException e) {
                    Log.e("Error syncUnites", e.toString());
                }
            }
        }
    }

    private void syncSettings() {
        JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdCrediCardStatus.getValue());
        JsonObject jData = new JsonObject();

        String msg = JSONProcess.jsonPack(jHead, jData);

        if (jHead != null) {
            String rMsg = SocketProcess.sendMessage(msg);
            if (rMsg.contains(Variables._ERROR)) {
                //hata oluştu
            } if (rMsg.contains(Variables._RETURNFAULT)) {
                //hata oluştu
            } else {
                //Veriler geldi Veritabanına yazılacak
                try {
                    try {
                        JsonParser parser = new JsonParser();
                        JsonArray jResult = parser.parse(rMsg).getAsJsonArray();
                        if (jResult.size()!=0) {
                            JsonObject test = new JsonObject();
                            test = parser.parse(rMsg).getAsJsonArray().get(0).getAsJsonObject();
                            Variables.isCreditCardActive = test.get("hesapid").getAsInt() > 0;
                            if (Variables.isCreditCardActive)
                                SettingsDao.setIntValue("isCreditCardActive", 1);
                            else
                                SettingsDao.setIntValue("isCreditCardActive", 0);
                        }

                    } catch (IllegalStateException e) {
                        Log.e("Error syncUnites", e.toString());
                    }
                } catch (JsonParseException e) {
                    Log.e("Error syncUnites", e.toString());
                }
            }
        }
    }


    private void syncTaxes() {
        JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdGetTaxes.getValue());
        JsonObject jData = new JsonObject();

        String msg = JSONProcess.jsonPack(jHead, jData);

        if (jHead != null) {
            String rMsg = SocketProcess.sendMessage(msg);
            if (rMsg.contains(Variables._ERROR)) {
                //hata oluştu
            } else {
                //Veriler geldi Veritabanına yazılacak
                try {
                    try {
                        JsonParser parser = new JsonParser();
                        JsonArray lArr = parser.parse(rMsg).getAsJsonArray();
                        ProductDao.saveTax(lArr);
                    } catch (IllegalStateException e) {
                        Log.e("Error syncTaxes", e.toString());
                    }
                } catch (JsonParseException e) {
                    Log.e("Error syncTaxes", e.toString());
                }
            }
        }
    }

    private void syncDeposites() {
        JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdGetDeposites.getValue());
        JsonObject jData = new JsonObject();

        String msg = JSONProcess.jsonPack(jHead, jData);

        if (jHead != null) {
            String rMsg = SocketProcess.sendMessage(msg);
            if (rMsg.contains(Variables._ERROR)) {
                //hata oluştu
            } else {
                //Veriler geldi Veritabanına yazılacak
                try {
                    JsonParser parser = new JsonParser();
                    try {
                        JsonArray lArr = parser.parse(rMsg).getAsJsonArray();
                        ProductDao.saveDeposit(lArr);
                    } catch (IllegalStateException e) {
                        Log.e("Error syncDeposites", e.toString());
                    }
                } catch (JsonParseException e) {
                    Log.e("Error syncDeposites", e.toString());
                }
            }
        }
    }

    private void syncProductGroups() {
        JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdGetProductGroups.getValue());
        JsonObject jData = new JsonObject();

        String msg = JSONProcess.jsonPack(jHead, jData);

        if (jHead != null) {
            String rMsg = SocketProcess.sendMessage(msg);
            if (rMsg.contains(Variables._ERROR)) {
                //hata oluştu
            } else {
                //Veriler geldi Veritabanına yazılacak
                try {
                    try {
                        JsonParser parser = new JsonParser();
                        JsonArray lArr = parser.parse(rMsg).getAsJsonArray();
                        ProductDao.saveGroup(lArr);
                    } catch (IllegalStateException e) {
                        Log.e("Error syncProductGroups", e.toString());
                    }
                } catch (JsonParseException e) {
                    Log.e("Error syncProductGroups", e.toString());
                }
            }
        }
    }


    private void syncProducts() {
        int limit = 250;
        int offset = 0;
        String rMsg = "";
        String msg = "";
        JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdGetProducts.getValue());
        JsonObject jData;

        int i = 0;
        do {
            i++;

            jData = new JsonObject();
            try {
                jData.addProperty("groupid", 0);
                jData.addProperty("limit", limit);
                jData.addProperty("offset", offset);
            } catch (JsonParseException e) {
                e.printStackTrace();
            }

            msg = JSONProcess.jsonPack(jHead, jData);
            if (jHead != null) {
                rMsg = SocketProcess.sendMessage(msg);
                if (rMsg.contains(Variables._ERROR)) {
                    //hata oluştu
                } else if (!rMsg.contains(Variables._EMPTY)) {
                    //Veriler geldi Veritabanına yazılacak
                    try {
                        try {
                            long start = System.currentTimeMillis();
                            Log.e("testtest", "ProductDao.saveProduct -startet");

                            JsonParser parser = new JsonParser();
                            JsonArray lArr = parser.parse(rMsg).getAsJsonArray();
                            parser = null;
                            ProductDao.saveProduct(lArr);
                            lArr = null;

                            long end = System.currentTimeMillis();
                            Log.e("testtest", "ProductDao.saveProduct - tur " + (i) + " dbye kaydetme -> " + ((end - start)) + " miliseconds");

                        } catch (IllegalStateException e) {
                            Log.e("Error syncProducts", e.toString());
                        }
                    } catch (JsonParseException e) {
                        Log.e("Error syncProducts", e.toString());
                    }
                }
            }
            jData = null;
            offset += 250;
        } while (!rMsg.contains(Variables._EMPTY)||rMsg.contains(Variables._ERROR));
    }

    private void syncProductPrices() {
        int limit = 500;
        int offset = 0;
        String rMsg = "";
        String msg = "";

        JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdGetProductPrices.getValue());
        JsonObject jData;

        int i = 0;
        do {
            i++;
            jData = new JsonObject();
            try {
                jData.addProperty("groupid", 0);
                jData.addProperty("limit", limit);
                jData.addProperty("offset", offset);
            } catch (JsonParseException e) {
                e.printStackTrace();
            }

            msg = JSONProcess.jsonPack(jHead, jData);
            if (jHead != null) {
                rMsg = SocketProcess.sendMessage(msg);
                if (rMsg.contains(Variables._ERROR)) {
                    //hata oluştu
                } else if (!rMsg.contains(Variables._EMPTY)) {
                    //Veriler geldi Veritabanına yazılacak
                    try {
                        try {

                            long start = System.currentTimeMillis();

                            JsonParser parser = new JsonParser();
                            JsonArray lArr = parser.parse(rMsg).getAsJsonArray();
                            parser = null;
                            ProductDao.savePrice(lArr);
                            lArr = null;

                            long end = System.currentTimeMillis();
                            Log.e("testtest", "ProductDao.savePrice tur   " + (i) + " (syncProductPrices) dbye kaydetme -> " + ((end - start)) + " miliseconds");

                        } catch (IllegalStateException e) {
                            Log.e("Error syncProductPrices", e.toString());
                        }
                    } catch (JsonParseException e) {
                        Log.e("Error syncProductPrices", e.toString());
                    }
                }
            }
            jData = null;
            offset += 500;
        } while (!rMsg.contains(Variables._EMPTY));
    }

    private void syncSuppliers() {
        JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdGetSuppliers.getValue());
        JsonObject jData = new JsonObject();

        String msg = JSONProcess.jsonPack(jHead, jData);

        if (jHead != null) {
            String rMsg = SocketProcess.sendMessage(msg);
            if (rMsg.contains(Variables._ERROR)) {
                //hata oluştu
            } else {
                //Veriler geldi Veritabanına yazılacak
                try {
                    try {
                        JsonParser parser = new JsonParser();
                        JsonArray lArr = parser.parse(rMsg).getAsJsonArray();
                        CustomerDao.saveSupplier(lArr);
                    } catch (IllegalStateException e) {
                        Log.e("Error syncSuppliers", e.toString());
                    }
                } catch (JsonParseException e) {
                    Log.e("Error syncSuppliers", e.toString());
                }
            }
        }
    }

    public void syncCustomers() {
        JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdGetCustomers.getValue());
        JsonObject jData = new JsonObject();

        String msg = JSONProcess.jsonPack(jHead, jData);

        if (jHead != null) {
            String rMsg = SocketProcess.sendMessage(msg);
            if (rMsg.contains(Variables._ERROR)) {
                //hata oluştu
            } else {
                //Veriler geldi Veritabanına yazılacak
                try {
                    try {
                        JsonParser parser = new JsonParser();
                        JsonArray lArr = parser.parse(rMsg).getAsJsonArray();
                        CustomerDao.saveCustomer(lArr);
                    } catch (IllegalStateException e) {
                        Log.e("Error syncCustomers", e.toString());
                    }
                } catch (JsonParseException e) {
                    Log.e("Error syncCustomers", e.toString());
                }
            }
        }
    }

    private void syncTables(){
        Database Db = new Database();
        SQLiteDatabase db = Db.getReadableDatabase();
        try {
         //   db.execSQL("alter table products add column origin text");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
