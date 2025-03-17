package com.eqpos.eqentry;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.ImageButton;

import com.eqpos.eqentry.DB.Dao;
import com.eqpos.eqentry.DB.Database;
import com.eqpos.eqentry.DB.ProductDao;
import com.eqpos.eqentry.Models.StockTransfer;
import com.eqpos.eqentry.tools.JSONProcess;
import com.eqpos.eqentry.tools.SocketProcess;
import com.eqpos.eqentry.tools.Variables;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static android.view.View.GONE;

public class MainMenu extends AppCompatActivity implements View.OnClickListener {
    Button btPrintLabel;
    Button btProducts;
    Button btChangePrice;
    Button btPurchaseOrder;
    Button btStockEntry;
    Button btSendDatas;
    Button btInventory;
    Button btInvoices;
    Button btCustomers;
    Button btTransfers;
    Button btManagement;
    Button btReports;
    //ImageButton btMainLogo;

    private String m_Text = "";
    private int _SELECTUSER=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        this.setTitle(R.string.app_name);

        Database.vtContext = this;

        btProducts = (Button) findViewById(R.id.bt_menu_productlist);
        btChangePrice = (Button) findViewById(R.id.bt_menu_changeprice);
        btPrintLabel = (Button) findViewById(R.id.bt_menu_printlabel);
        btPurchaseOrder = (Button) findViewById(R.id.bt_menu_purchaseorder);
        btStockEntry = (Button) findViewById(R.id.bt_menu_stockentry);
        btSendDatas = (Button) findViewById(R.id.bt_menu_send);
        btInventory = (Button) findViewById(R.id.bt_menu_inventory);
        btInvoices = (Button) findViewById(R.id.bt_menu_invoice);
        btCustomers = (Button) findViewById(R.id.bt_menu_customers);
        btTransfers = (Button) findViewById(R.id.bt_menu_stocktransfer);
        btManagement = (Button) findViewById(R.id.bt_menu_management);
        btReports = (Button) findViewById(R.id.bt_menu_reports);

        btProducts.setOnClickListener(this);
        btChangePrice.setOnClickListener(this);
        btPrintLabel.setOnClickListener(this);
        btPurchaseOrder.setOnClickListener(this);
        btStockEntry.setOnClickListener(this);
        btSendDatas.setOnClickListener(this);
        btInventory.setOnClickListener(this);
        btInvoices.setOnClickListener(this);
        btCustomers.setOnClickListener(this);
        btTransfers.setOnClickListener(this);

        btTransfers.setEnabled(Dao.Language == "tr");

        btProducts.setVisibility((Variables.showbtProducts) ? View.VISIBLE  : GONE );
        btChangePrice.setVisibility((Variables.showbtChangePrice) ? View.VISIBLE  : GONE );
        btPrintLabel.setVisibility((Variables.showbtPrintLabel) ? View.VISIBLE  : GONE );
        btPurchaseOrder.setVisibility((Variables.showbtPurchaseOrder) ? View.VISIBLE  : GONE );
        btStockEntry.setVisibility((Variables.showbtStockEntry) ? View.VISIBLE  : GONE );
        btSendDatas.setVisibility((Variables.showbtSendDatas) ? View.VISIBLE  : GONE );
        btInventory.setVisibility((Variables.showbtInventory) ? View.VISIBLE  : GONE );
        btInvoices.setVisibility((Variables.showbtInvoices) ? View.VISIBLE  : GONE );
        btCustomers.setVisibility((Variables.showbtCustomers) ? View.VISIBLE  : GONE );
        btTransfers.setVisibility((Variables.showbtTransfers) ? View.VISIBLE  : GONE );
        btManagement.setVisibility((Variables.showbtManagement) ? View.VISIBLE  : GONE );
        btReports.setVisibility((Variables.showbtReports) ? View.VISIBLE  : GONE );




        if (BuildConfig.DEBUG) {
            //btMainLogo.setOnClickListener(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == RESULT_OK && requestCode == _SELECTUSER) {
//
//            if (data.getIntExtra("authority", 1) == 0) {
//
//                Intent sendDatasIntent = new Intent(this, SendDataActivity.class);
//                startActivity(sendDatasIntent);
//            }
//        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_menu_productlist:
                Intent productsintent = new Intent(this, ProductsActivity.class);
                startActivity(productsintent);
                break;
            case R.id.bt_menu_changeprice:
                Intent changePriceIntent = new Intent(this, ChangePriceActivity.class);
                startActivity(changePriceIntent);
                break;
            case R.id.bt_menu_printlabel:
                Intent printLabelIntent = new Intent(this, PrintLabelActivity.class);
                startActivity(printLabelIntent);
                break;
            case R.id.bt_menu_purchaseorder:
                Intent purchaseOrderIntent = new Intent(this, PurchaseOrderActivity.class);
                startActivity(purchaseOrderIntent);
                break;
            case R.id.bt_menu_stockentry:
                Intent stockEntryListIntent = new Intent(this, StockEntryListActivity.class);
                startActivity(stockEntryListIntent);
                break;
            case R.id.bt_menu_inventory:
                Intent inventoryIntent = new Intent(this, InventurActivity.class);
                startActivity(inventoryIntent);
                break;
            case R.id.bt_menu_invoice:
                Intent invoiceIntent = new Intent(this, InvoiceActivity.class);
                startActivity(invoiceIntent);
                break;
            case R.id.bt_menu_customers:
                Intent customersIntent = new Intent(this, CustomersActivity.class);
                startActivity(customersIntent);
                break;
            case R.id.bt_menu_stocktransfer:
                Intent TransferIntent = new Intent(this, StockTransferListActivity.class);
                startActivity(TransferIntent);
                break;

            case R.id.bt_menu_send:
                //burada admin şifresi istenecek
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.prompt_password));

                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER| InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                builder.setView(input);

                builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text = input.getText().toString();

                        if (m_Text.length() > 0) {
                            if (isAdmin(m_Text)) {

                                Intent sendDatasIntent = new Intent(builder.getContext(), SendDataActivity.class);
                                startActivity(sendDatasIntent);
                            }
                            m_Text = "";
                        }
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        m_Text = "";
                    }
                });

                builder.show();
                break;



        }
    }

    private boolean isAdmin(String password) {
        int yetki = 2;
        if (SocketProcess.isConnectWiFiNetwork(this, true)) {

            SocketProcess.context = this;
            String rMsg = "";
            try {

                JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdLogin.getValue());
                JsonObject jData = new JsonObject();
                jData.addProperty("password", password);
                String msg = JSONProcess.jsonPack(jHead, jData);
                rMsg = SocketProcess.sendMessage(msg);
                if (rMsg != "") {
                    JsonParser parser = new JsonParser();
                    JsonObject jResult = parser.parse(rMsg).getAsJsonArray().get(0).getAsJsonObject();
                    yetki = jResult.get("yetki").getAsInt();
                }
            } catch (Exception e) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage(getString(R.string.error_incorrect_password));
                builder1.setCancelable(true);
                builder1.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert11 = builder1.create();
                alert11.show();
                Log.e("admin password ", e.getMessage());
                try {
                    SocketProcess.client.close();
                    SocketProcess.client = null;
                } catch (Exception ex) {
                }
                return false;
            }
        }

        return yetki == 0;
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        try {
            if (Variables.mBluetoothSocket != null)
                Variables.mBluetoothSocket.close();
        } catch (Exception e) {
            Log.e("Tag", "Exe ", e);
        }
    }

}
