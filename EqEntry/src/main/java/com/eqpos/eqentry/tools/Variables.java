package com.eqpos.eqentry.tools;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.eqpos.eqentry.DB.Database;
import com.eqpos.eqentry.Models.Product;

import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by dursu on 23.02.2018.
 */

public class Variables {
    private static Database Db;
    public static final String _ERROR = "[{result,Error}]";
    public static final String _EMPTY = "[{result,Empty}]";
    public static final String _RETURNOK = "[{result,True}]";
    public static final String _RETURNFAULT = "[{result,False}]";
    public static final String _UNREGISTER = "[{result,Unregister}]";
    public static Context context = null;

    public static String companyName = "";
    public static String companyAddres = "";
    public static String companyPhone = "";
    public static String companyTaxId = "";
    public static String companySlogan = "";
    public static String hostIp = "192.168.5.3";
    public static int hostPort = 1454;
    public static String serialNumber = "";
    public static int userId = -1;
    public static String userName = "";
    public static boolean isCreditCardActive = false;
    public static boolean isPrintTaxOnInvoice = true;
    public static boolean showUnitPrice = true;

    public static boolean showbtProducts = true;

    public static boolean showbtPrintLabel = true;
    public static boolean showbtPurchaseOrder = true;
    public static boolean showbtStockEntry = true;
    public static boolean showbtSendDatas = true;
    public static boolean showbtInventory = true;
    public static boolean showbtInvoices = true;
    public static boolean showbtCustomers = true;
    public static boolean showbtChangePrice = false;
    public static boolean showbtTransfers = false;
    public static boolean showbtManagement = false;
    public static boolean showbtReports = false;




    //public static NumberFormat cf = NumberFormat.getCurrencyInstance();
    public static NumberFormat nf ; //NumberFormat.getNumberInstance();

    //Bluetooth yazıcı kullanımı için eklendi
    public static final String TAG = "TAG";
    public static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 2;
    public static BluetoothAdapter mBluetoothAdapter;
    public static UUID applicationUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static BluetoothSocket mBluetoothSocket;
    public static BluetoothDevice mBluetoothDevice;
    public static String printerAddress = "";
    private static char decimalSeparator;

    public static boolean isTurkey = true;

    public enum ServerCommand {
        cmdLogin(10),
        cmdLogout(11),
        cmdGetSetting(12),
        cmdRecordCount(13),
        cmdCrediCardStatus(14),
        cmdGetUsers(15),
        cmdGetPrinterList(20),
        cmdGetProductGroups(30),
        cmdGetProducts(31),
        cmdGetProductPrices(32),
        cmdGetUnits(33),
        cmdGetTaxes(34),
        cmdGetDeposites(35),
        cmdGetSuppliers(36),
        cmdGetCustomers(37),
        cmdGetProductBarcodes(39),
        cmdGetFirmInfo(48),
        cmdRegisterDevice(50),
        cmdIsRegisteredDevice(51),
        cmdSendStockInventor(60),
        cmdChangePrice(70),
        cmdInputDelivery(80),
        cmdPrintLabel(90),
        cmdSaveProducts(100),
        cmdSaveOrder(110),
        cmdSaveInventur(120),
        cmdSaveInvoice(130),
        cmdSaveCustomers(140),
        cmdSaveCollects(150),
        cmdStockTransfer(160),
        cmdWarehouses(170);

        private final int value;

        private ServerCommand(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public String getStrValue() {
            return String.format("%02d", this.getValue());
        }

        public static ServerCommand parse(int value) {
            // ServerCommand.values() metodu deger kumesini dondurur
            for (ServerCommand type : ServerCommand.values()) {
                if (value == type.getValue()) {
                    return type;
                }
            }
            return cmdLogin;
        }
    }

    public static int dp2px(float dips) {
        return (int) (dips * Database.vtContext.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static double roundTo(double value, int decimals) {
        double result;
        long multiplier = Math.round(Math.pow(10, decimals));
        result = value * multiplier;
        result = Math.round(result);
        result = result / multiplier;
        return result;
    }

    public static double strToDouble(String value) throws ParseException {
        if (value.isEmpty())
            return 0.0;
        if (nf == null) {
            nf = NumberFormat.getNumberInstance();
            DecimalFormatSymbols dfs = new DecimalFormatSymbols();
            decimalSeparator = dfs.getDecimalSeparator();

        }

        if (value.indexOf(decimalSeparator)<0) {
            if (decimalSeparator=='.') {
                value = value.replace(',', decimalSeparator);
            } else if (decimalSeparator==',') {
                value = value.replace('.', decimalSeparator);
            }
        }

        return nf.parse(value).doubleValue();
    }

    public static String doubleToStr(double value, int decimals) {
        if (nf == null) {
            nf = NumberFormat.getNumberInstance();
            DecimalFormatSymbols dfs = new DecimalFormatSymbols();
            decimalSeparator = dfs.getDecimalSeparator();
        }
        String lValue = nf.format(value);
        int pos = lValue.indexOf(decimalSeparator);

        if (pos > 0) {
            String lDecimals = lValue.substring(pos+1);
            for (int i = lDecimals.length(); i < decimals; i++) {
                lValue = lValue + "0";
            }
        } else {lValue = lValue + decimalSeparator + "00";}
        return lValue; //nf.format(value);
    }


    public static double calcNet(double total, double taxrate) {
        double result = 0.0;
        try {
            result = total / (1 + (taxrate / 100));
        } catch (Exception ex) {
            result = 0.0;
        }

        return roundTo(result, 2);
    }

    public static String getCurrentTime() {
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        Date dateobj = new Date();
        return df.format(dateobj);
    }
    public static String getCurrentDate() {
        DateFormat df = new SimpleDateFormat("YYYY.mm.dd");
        Date dateobj = new Date();
        return df.format(dateobj);
    }

    public static void getFirmInfo() {
        if (Db == null) {
            Db = new Database();
        }

        SQLiteDatabase db = Db.getReadableDatabase();

        String lSql = "select * from firm";

        Cursor cursor = db.rawQuery(lSql, null);
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            companyName = cursor.getString(cursor.getColumnIndexOrThrow("firmname"));
            companyAddres = cursor.getString(cursor.getColumnIndexOrThrow("firmaddress"));
            companyPhone = cursor.getString(cursor.getColumnIndexOrThrow("firmphone"));
            companyTaxId = cursor.getString(cursor.getColumnIndexOrThrow("firmtaxid"));
            companySlogan = cursor.getString(cursor.getColumnIndexOrThrow("slogan"));
        }
    }
}
