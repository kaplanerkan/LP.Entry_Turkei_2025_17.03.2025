package com.eqpos.eqentry;

import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.eqpos.eqentry.db.BarcodeDao;
import com.eqpos.eqentry.db.SettingsDao;
import com.eqpos.eqentry.db.SyncDB;
import com.eqpos.eqentry.models.BarcodeSettings;
import com.eqpos.eqentry.printing.DeviceListActivity;
import com.eqpos.eqentry.printing.PrintDao;
import com.eqpos.eqentry.tools.Bluetooth;
import com.eqpos.eqentry.tools.JSONProcess;
import com.eqpos.eqentry.tools.SharedPrefUtil;
import com.eqpos.eqentry.tools.SocketProcess;
import com.eqpos.eqentry.tools.Variables;
import com.google.gson.JsonObject;

import java.io.IOException;

import static com.eqpos.eqentry.tools.Bluetooth.closeSocket;
import static com.eqpos.eqentry.tools.Bluetooth.connectToPrinter;

public class Settings extends AppCompatActivity implements Runnable {
    Button btSync, btRegisterDevice, btConnect, btSelectPrinter;
    EditText edServerIp, edPort, edPricePrefix, edPricePlu, edPriceValue,
            edQuantityPrefix, edQuantityPlu, edQuantityValue;
    TextView lblSerialNumber, lblPrinterName;
    Spinner cmbLanguage;
    Spinner cmbCurrency;
    ProgressDialog mBluetoothConnectProgressDialog;
    String gPrinterName, gPrinterAddress;
    BarcodeSettings barcodeSettings;
    Switch chPrintTaxOnInvoice;
    Switch chUnitPrice;

    Switch chshowbtChangePrice ;
    Switch chshowbtCustomers ;
    Switch chshowbtInventory ;
    Switch chshowbtInvoices ;
    Switch chshowbtPrintLabel;
    Switch chshowbtPurchaseOrder ;
    Switch chshowbtSendDatas;
    Switch chshowbtStockEntry ;
    Switch chshowbtTransfers ;
    Switch chshowbtProducts;



    boolean isTestSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        this.setTitle(R.string.settings);

        edServerIp = (EditText) findViewById(R.id.ed_settings_ip);
        edPort = (EditText) findViewById(R.id.ed_settings_port);
        edPricePrefix = (EditText) findViewById(R.id.ed_settings_price_prefix);
        edPricePlu = (EditText) findViewById(R.id.ed_settings_price_plu);
        edPriceValue = (EditText) findViewById(R.id.ed_settings_price_value);
        edQuantityPrefix = (EditText) findViewById(R.id.ed_settings_quantity_prefix);
        edQuantityPlu = (EditText) findViewById(R.id.ed_settings_quantity_plu);
        edQuantityValue = (EditText) findViewById(R.id.ed_settings_quantity_value);
        btSync = (Button) findViewById(R.id.bt_settings_syncdb);
        btRegisterDevice = (Button) findViewById(R.id.bt_settings_registerdevice);
        btConnect = (Button) findViewById(R.id.bt_settings_connecttoserver);
        btSelectPrinter = (Button) findViewById(R.id.bt_settings_selectprinter);
        lblSerialNumber = (TextView) findViewById(R.id.lbl_settings_serialnumber);
        lblPrinterName = (TextView) findViewById(R.id.lbl_settings_printername);
        cmbLanguage = (Spinner) findViewById(R.id.cmb_settings_language);
        cmbCurrency = (Spinner) findViewById(R.id.cmb_settings_currency);
        chPrintTaxOnInvoice = (Switch) findViewById(R.id.ch_settings_printtaxoninvoice);
        chUnitPrice = (Switch) findViewById(R.id.ch_settings_unitprice);

        chshowbtChangePrice= (Switch) findViewById(R.id.ch_settings_bt_menu_changeprice);
        chshowbtCustomers= (Switch) findViewById(R.id.ch_settings_bt_menu_customers);
        chshowbtInventory= (Switch) findViewById(R.id.ch_settings_bt_menu_inventory);
        chshowbtInvoices= (Switch) findViewById(R.id.ch_settings_bt_menu_invoice);
        chshowbtPrintLabel= (Switch) findViewById(R.id.ch_settings_bt_menu_printlabel);
        chshowbtProducts= (Switch) findViewById(R.id.ch_settings_bt_menu_productlist);
        chshowbtPurchaseOrder= (Switch) findViewById(R.id.ch_settings_bt_menu_purchaseorder);
        chshowbtSendDatas= (Switch) findViewById(R.id.ch_settings_bt_menu_send);
        chshowbtStockEntry= (Switch) findViewById(R.id.ch_settings_bt_menu_stockentry);
        chshowbtTransfers= (Switch) findViewById(R.id.ch_settings_bt_menu_stocktransfer);

        lblSerialNumber.setText(Variables.serialNumber);
        SocketProcess.context = this;
        readSettings();

        btSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SyncDB db = new SyncDB(v.getContext());
            }
        });

        btRegisterDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerDevice();
            }
        });

        btConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testConnection();
            }
        });

        cmbLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                if (cmbLanguage.getSelectedItemPosition() >= 0)
                    SettingsDao.setIntValue("Language", cmbLanguage.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        cmbCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                if (cmbCurrency.getSelectedItemPosition() >= 0)
                    SettingsDao.setIntValue("Currency", cmbCurrency.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        btSelectPrinter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View mView) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                    if(!checkPermission(new String[]{BLUETOOTH_CONNECT,BLUETOOTH_SCAN},PERMISSION_REQUEST_CODE)){
                        return;
                    }

                }

                    Variables.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (Variables.mBluetoothAdapter == null) {
                        Toast.makeText(Settings.this, "Bluetooth Error", Toast.LENGTH_SHORT).show();
                    } else {
                        if (!Variables.mBluetoothAdapter.isEnabled()) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, Variables.REQUEST_ENABLE_BT);
                        } else {
                            Bluetooth.ListPairedDevices();
                            Intent connectIntent = new Intent(Settings.this, DeviceListActivity.class);
                            startActivityForResult(connectIntent, Variables.REQUEST_CONNECT_DEVICE);
                        }
                    }



            }
        });

    }

    public void onActivityResult(int mRequestCode, int mResultCode,
                                 Intent mDataIntent) {
        super.onActivityResult(mRequestCode, mResultCode, mDataIntent);

        switch (mRequestCode) {
            case Variables.REQUEST_CONNECT_DEVICE:
                if (mResultCode == Activity.RESULT_OK) {
                    Bundle mExtra = mDataIntent.getExtras();
                    String mDeviceAddress = mExtra.getString("DeviceAddress");
                    gPrinterName = mExtra.getString("DeviceName");
                    gPrinterAddress = mDeviceAddress;
                    Log.v(Variables.TAG, "Coming incoming address " + mDeviceAddress);
                    Variables.mBluetoothDevice = Variables.mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
                    mBluetoothConnectProgressDialog = ProgressDialog.show(this,
                            "Connecting...", Variables.mBluetoothDevice.getName() + " : "
                                    + Variables.mBluetoothDevice.getAddress(), true, false);
                    Thread mBlutoothConnectThread = new Thread(this);
                    mBlutoothConnectThread.start();
                    // pairToDevice(mBluetoothDevice); This method is replaced by
                    // progress dialog with thread
                    SharedPrefUtil.init(Settings.this);
                    SharedPrefUtil.putBoolean(SharedPrefUtil.KEY_BLE_PRINTER_STATUS, true);

                }
                break;

            case Variables.REQUEST_ENABLE_BT:
                if (mResultCode == Activity.RESULT_OK) {
                    Bluetooth.ListPairedDevices();
                    Intent connectIntent = new Intent(Settings.this,
                            DeviceListActivity.class);
                    startActivityForResult(connectIntent, Variables.REQUEST_CONNECT_DEVICE);
                } else {
                    Toast.makeText(Settings.this, "Bluetooth Error II", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    @Override
    protected void onDestroy() {
        if (!isTestSuccess)
            saveSettings();

        setResult(RESULT_OK);
        super.onDestroy();
    }

    private void registerDevice() {
        SocketProcess.context = this;
        String rMsg = "";
        try {
            JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdRegisterDevice.getValue());
            JsonObject jData = new JsonObject();
            String msg = JSONProcess.jsonPack(jHead, jData);

            rMsg = SocketProcess.sendMessage(msg);
            if (rMsg.contains(Variables._RETURNOK)) {
                Toast.makeText(this, getString(R.string.register_request_was_sent), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.device_cannot_registered), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.device_cannot_registered), Toast.LENGTH_LONG).show();
        }
    }

    private void readSettings() {
        try {
            edServerIp.setText(SettingsDao.getStrValue("ServerIP"));
            String lPort = String.valueOf(SettingsDao.getIntValue("ServerPort"));
            if (!lPort.equals("")) {
                if (Integer.parseInt(lPort) < 0)
                    lPort = "1454";
            }

            edPort.setText(lPort);


            cmbLanguage.setSelection(SettingsDao.getIntValue("Language"));
            cmbCurrency.setSelection(SettingsDao.getIntValue("Currency", 1));
            chPrintTaxOnInvoice.setChecked(SettingsDao.getIntValue("PrintTaxOnInvoice", 1)>0);
            chUnitPrice.setChecked(SettingsDao.getIntValue("ShowUnitPrice", 1)>0);

            chshowbtPrintLabel.setChecked(SettingsDao.getIntValue("showbtPrintLabel", 1)>0);
            chshowbtPurchaseOrder.setChecked(SettingsDao.getIntValue("showbtPurchaseOrder", 1)>0);
            chshowbtSendDatas.setChecked(SettingsDao.getIntValue("showbtSendDatas", 1)>0);
            chshowbtStockEntry.setChecked(SettingsDao.getIntValue("showbtStockEntry", 1)>0);
            chshowbtTransfers.setChecked(SettingsDao.getIntValue("showbtTransfers", 1)>0);
            chshowbtProducts.setChecked(SettingsDao.getIntValue("showbtProducts", 1)>0);
            chshowbtInvoices.setChecked(SettingsDao.getIntValue("showbtInvoices", 1)>0);
            chshowbtInventory.setChecked(SettingsDao.getIntValue("showbtInventory", 1)>0);
            chshowbtCustomers.setChecked(SettingsDao.getIntValue("showbtCustomers", 1)>0);
            chshowbtChangePrice.setChecked(SettingsDao.getIntValue("showbtChangePrice", 1)>0);


            gPrinterName = SettingsDao.getStrValue("PrinterName");
            gPrinterAddress = SettingsDao.getStrValue("PrinterAddress");


            lblPrinterName.setText(gPrinterName);

            if (!gPrinterAddress.isEmpty()) {
                try {
                    connectToPrinter(gPrinterAddress);
                } catch (Exception e) {
                   //Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("error", e.getMessage());
                }
            }

            barcodeSettings = BarcodeDao.getBarcodeSettings();
            edPricePrefix.setText(barcodeSettings.getP_Prefix());
            edPricePlu.setText(String.valueOf(barcodeSettings.getP_PLU()));
            edPriceValue.setText(String.valueOf(barcodeSettings.getP_Value()));
            edQuantityPrefix.setText(barcodeSettings.getQ_Prefix());
            edQuantityPlu.setText(String.valueOf(barcodeSettings.getQ_PLU()));
            edQuantityValue.setText(String.valueOf(barcodeSettings.getQ_Value()));

        } catch (Exception e) {
            e.printStackTrace();
            edServerIp.setText("");
            edPort.setText("");
        }
    }

    private void testConnection() {
        //if (SocketProcess.isConnectWiFiNetwork(this, false)) {

            try {
                Variables.hostIp = edServerIp.getText().toString().trim();
                Variables.hostPort = Integer.parseInt(edPort.getText().toString());

                try {
                    if (SocketProcess.connectToServer()) {
                        btSync.setEnabled(true);
                        btRegisterDevice.setEnabled(true);
                        isTestSuccess = true;
                        saveSettings();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, getString(R.string.cannot_connect_to_the_server), Toast.LENGTH_LONG).show();
                    Log.e("error", e.getMessage());
                }
            } catch (Exception e) {
                if (edServerIp.getText().equals("")) {
                    edServerIp.requestFocus();
                    return;
                }
                if (edPort.getText().equals("")) {
                    edPort.requestFocus();
                    return;
                }
            }
        //}
    }

    private void saveSettings() {
        //SettingsDao.setStrValue("ServerIP", edServerIp.getText().toString()); //Veritabani silindiginde ilk veri donderilince tablo create ediliyor.
        //O sebeple kaydetmiyor Bir daha gonderiyorum o sebeple.
        if (isTestSuccess) {
            SettingsDao.setStrValue("ServerIP", edServerIp.getText().toString());
            SettingsDao.setIntValue("ServerPort", Integer.parseInt(edPort.getText().toString()));
        }
        SettingsDao.setIntValue("Language", cmbLanguage.getSelectedItemPosition());
        SettingsDao.setIntValue("Currency", cmbCurrency.getSelectedItemPosition());

        Variables.isPrintTaxOnInvoice = chPrintTaxOnInvoice.isChecked();
        if (chPrintTaxOnInvoice.isChecked()) {
            SettingsDao.setIntValue("PrintTaxOnInvoice", 1);
        } else {
            SettingsDao.setIntValue("PrintTaxOnInvoice", 0);
        }

        Variables.showUnitPrice = chUnitPrice.isChecked();
        if (chUnitPrice.isChecked()) {
            SettingsDao.setIntValue("ShowUnitPrice", 1);
        } else {
            SettingsDao.setIntValue("ShowUnitPrice", 0);
        }

        SettingsDao.setStrValue("PrinterName", gPrinterName);
        SettingsDao.setStrValue("PrinterAddress", gPrinterAddress);


        Variables.showbtProducts = chshowbtProducts.isChecked();
        if (chshowbtProducts.isChecked()) {
            SettingsDao.setIntValue("showbtProducts", 1);
        } else {
            SettingsDao.setIntValue("showbtProducts", 0);
        }

        Variables.showbtTransfers = chshowbtTransfers.isChecked();
        if (chshowbtTransfers.isChecked()) {
            SettingsDao.setIntValue("showbtTransfers", 1);
        } else {
            SettingsDao.setIntValue("showbtTransfers", 0);
        }

        Variables.showbtStockEntry = chshowbtStockEntry.isChecked();
        if (chshowbtStockEntry.isChecked()) {
            SettingsDao.setIntValue("showbtStockEntry", 1);
        } else {
            SettingsDao.setIntValue("showbtStockEntry", 0);
        }

        Variables.showbtSendDatas = chshowbtSendDatas.isChecked();
        if (chshowbtSendDatas.isChecked()) {
            SettingsDao.setIntValue("showbtSendDatas", 1);
        } else {
            SettingsDao.setIntValue("showbtSendDatas", 0);
        }

        Variables.showbtPurchaseOrder = chshowbtPurchaseOrder.isChecked();
        if (chshowbtPurchaseOrder.isChecked()) {
            SettingsDao.setIntValue("showbtPurchaseOrder", 1);
        } else {
            SettingsDao.setIntValue("showbtPurchaseOrder", 0);
        }

        Variables.showbtPrintLabel = chshowbtPrintLabel.isChecked();
        if (chshowbtPrintLabel.isChecked()) {
            SettingsDao.setIntValue("showbtPrintLabel", 1);
        } else {
            SettingsDao.setIntValue("showbtPrintLabel", 0);
        }

        Variables.showbtInvoices = chshowbtInvoices.isChecked();
        if (chshowbtInvoices.isChecked()) {
            SettingsDao.setIntValue("showbtInvoices", 1);
        } else {
            SettingsDao.setIntValue("showbtInvoices", 0);
        }

        Variables.showbtInventory = chshowbtInventory.isChecked();
        if (chshowbtInventory.isChecked()) {
            SettingsDao.setIntValue("showbtInventory", 1);
        } else {
            SettingsDao.setIntValue("showbtInventory", 0);
        }

        Variables.showbtCustomers = chshowbtCustomers.isChecked();
        if (chshowbtCustomers.isChecked()) {
            SettingsDao.setIntValue("showbtCustomers", 1);
        } else {
            SettingsDao.setIntValue("showbtCustomers", 0);
        }

        Variables.showbtChangePrice = chshowbtChangePrice.isChecked();
        if (chshowbtChangePrice.isChecked()) {
            SettingsDao.setIntValue("showbtChangePrice", 1);
        } else {
            SettingsDao.setIntValue("showbtChangePrice", 0);
        }



        int plu = 0;
        int value = 0;
        try {
            plu = Integer.parseInt(edPricePlu.getText().toString());
        } catch (Exception e) {
            plu = 5;
        }
        try {
            value = Integer.parseInt(edPriceValue.getText().toString());
        } catch (Exception e) {
            value = 5;
        }
        BarcodeDao.saveBarcodeSettings(false, edPricePrefix.getText().toString(), plu, value);

        try {
            plu = Integer.parseInt(edQuantityPlu.getText().toString());
        } catch (Exception e) {
            plu = 5;
        }
        try {
            value = Integer.parseInt(edQuantityValue.getText().toString());
        } catch (Exception e) {
            value = 5;
        }
        BarcodeDao.saveBarcodeSettings(true, edQuantityPrefix.getText().toString(), plu, value);

    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mBluetoothConnectProgressDialog.dismiss();
            Toast.makeText(Settings.this, "DeviceConnected", Toast.LENGTH_SHORT).show();
        }
    };
    final public int PERMISSION_REQUEST_CODE = 1011;
    private boolean checkPermission(String[] permissions,int requestCode) {
        //String[] permissions = new String[]{permission};
        boolean result=true;
        for (int i=0;i<permissions.length;i++){
            if (ContextCompat.checkSelfPermission(this, permissions[i]) == PackageManager.PERMISSION_DENIED) {
                result =false;
            }

        }
        if (!result) {
            ActivityCompat.requestPermissions(this, permissions, requestCode);
        }
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Bluetooth Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
               // Toast.makeText(this, "Bluetooth Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void run() {
        try {
            if (gPrinterAddress != Variables.mBluetoothDevice.getName()) {
                Variables.mBluetoothSocket = Variables.mBluetoothDevice.createRfcommSocketToServiceRecord(Variables.applicationUUID);
                Variables.mBluetoothAdapter.cancelDiscovery();
                Variables.mBluetoothSocket.connect();
                mHandler.sendEmptyMessage(0);
                gPrinterName = Variables.mBluetoothDevice.getName();
                gPrinterAddress = Variables.mBluetoothDevice.getAddress();
                SettingsDao.setStrValue("PrinterName", gPrinterName);
                SettingsDao.setStrValue("PrinterAddress", gPrinterAddress);
                Variables.printerAddress = gPrinterAddress;
            }
            PrintDao.printTest();
        } catch (IOException eConnectException) {
            Log.d(Variables.TAG, "CouldNotConnectToSocket", eConnectException);
            closeSocket(Variables.mBluetoothSocket);
            return;
        }
    }



}
