package com.eqpos.eqentry;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.eqpos.eqentry.DB.CustomerDao;
import com.eqpos.eqentry.DB.Dao;
import com.eqpos.eqentry.DB.ProductDao;
import com.eqpos.eqentry.DB.SendDao;
import com.eqpos.eqentry.DB.SettingsDao;
import com.eqpos.eqentry.DB.SyncDB;
import com.eqpos.eqentry.tools.SocketProcess;

import java.io.IOException;

public class SendDataActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btSendChangePrices;
    private Button btSendDelivery;
    private Button btSendProducts;
    private Button btSendPrintLabel;
    private Button btSendOrder;
    private Button btSendInventur;
    private Button btSendInvoice;
    private Button btSendCustomers;
    private Button btSendCollects;
    private Button btSendTransfers;
    private Button btSendAll;
    private ImageButton btLogo;
    private ProgressDialog progressDialog;
    private int vId;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_data);
        this.setTitle(R.string.senddatas);
        context = this;

        btSendChangePrices = (Button) findViewById(R.id.bt_senddatas_changeprice);
        btSendDelivery = (Button) findViewById(R.id.bt_senddatas_stockentry);
        btSendProducts = (Button) findViewById(R.id.bt_senddatas_productlist);
        btSendPrintLabel = (Button) findViewById(R.id.bt_senddatas_printlabel);
        btSendOrder = (Button) findViewById(R.id.bt_senddatas_purchaseorder);
        btSendInventur = (Button) findViewById(R.id.bt_senddatas_inventur);
        btSendInvoice = (Button) findViewById(R.id.bt_senddatas_invoice);
        btSendCustomers = (Button) findViewById(R.id.bt_senddatas_customers);
        btSendCollects = (Button) findViewById(R.id.bt_senddatas_collects);
        btSendTransfers = (Button) findViewById(R.id.bt_senddatas_transfers);
        btSendAll = (Button) findViewById(R.id.bt_senddatas_alldatas);

        btSendChangePrices.setOnClickListener(this);
        btSendDelivery.setOnClickListener(this);
        btSendProducts.setOnClickListener(this);
        btSendPrintLabel.setOnClickListener(this);
        btSendOrder.setOnClickListener(this);
        btSendInventur.setOnClickListener(this);
        btSendInvoice.setOnClickListener(this);
        btSendCustomers.setOnClickListener(this);
        btSendCollects.setOnClickListener(this);
        btSendTransfers.setOnClickListener(this);
        btSendAll.setOnClickListener(this);

        if (Dao.Language != "tr") {
            btSendTransfers.setVisibility(View.GONE);
        }
    }

    private void showMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        AlertDialog dialog = builder.create();
    }

    @Override
    public void onClick(View v) {
        progressDialog = ProgressDialog.show(this, "", getString(R.string.msg_sendingdata), true, false);

        Button bt = (Button) findViewById(v.getId());
        bt.setEnabled(false);
        bt.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
        vId = v.getId();
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(2000);

                } catch (Exception e) {
                }

                try {

                    switch (vId) {
                        case R.id.bt_senddatas_changeprice:
                            if (ProductDao.isThereNewOrUpdatedProduct()) {
                                Toast.makeText(context, "Önce ürünleri gönderin", Toast.LENGTH_LONG).show();
                                return; //SendDao.sendProducts();
                            }
                            //progressDialog.setTitle(context.getString(R.string.changeprice));
                            SendDao.sendChangedPrices();
                            break;
                        case R.id.bt_senddatas_stockentry:
                            //progressDialog.setTitle(context.getString(R.string.deliverynote));
                            if (ProductDao.isThereNewOrUpdatedProduct()) {
                                Toast.makeText(context, "Önce ürünleri gönderin", Toast.LENGTH_LONG).show();
                                return; //SendDao.sendProducts();
                            }
                            SendDao.sendAllDelivery();
                            break;
                        case R.id.bt_senddatas_productlist:
                            //progressDialog.setTitle(context.getString(R.string.productlist));
                            SendDao.sendProducts();
                            break;
                        case R.id.bt_senddatas_printlabel:
                            if (ProductDao.isThereNewOrUpdatedProduct()) {
                                Toast.makeText(context, "Önce ürünleri gönderin", Toast.LENGTH_LONG).show();
                                return; //SendDao.sendProducts();
                            }
                            //progressDialog.setTitle(context.getString(R.string.printlabel));
                            SendDao.sendPrintLabel();
                            break;
                        case R.id.bt_senddatas_purchaseorder:
                            //progressDialog.setTitle(context.getString(R.string.purchaseorder));
                            if (ProductDao.isThereNewOrUpdatedProduct())
                                SendDao.sendProducts();
                            SendDao.sendOrder();
                            break;
                        case R.id.bt_senddatas_inventur:
                            //progressDialog.setTitle(context.getString(R.string.inventorycount));
                            if (ProductDao.isThereNewOrUpdatedProduct())
                                SendDao.sendProducts();
                            SendDao.sendInventur();
                            break;
                        case R.id.bt_senddatas_invoice:
                            //progressDialog.setTitle(context.getString(R.string.invoice));


                            try {
                                SendDao.sendCustomers();
                                
                                if (ProductDao.isThereNewOrUpdatedProduct()){
                                    SendDao.sendProducts();
                                }
                            } catch (Exception e) {
                                showMessage(getString(R.string.productlist) + "  error: " + e.getMessage());
                            }
                            try {
                                if (CustomerDao.isThereNewCustomer())
                                    SendDao.sendCustomers();
                            } catch (Exception e) {
                                showMessage(getString(R.string.customers) + "  error: " + e.getMessage());
                            }

                            try {
                                SendDao.sendAllInvoice();

                                // Customers leri guncelle, cunku yeni fatura gonderildi
                                runOnUiThread(() -> {
                                    CustomerDao.deleteAllCustomers();
                                    SyncDB db = new SyncDB(v.getContext(), "dummy Customers guncelle");
                                });

                            } catch (Exception e) {
                                showMessage(getString(R.string.invoice) + "  error: " + e.getMessage());
                                break;
                            }
                            break;
                        case R.id.bt_senddatas_customers:
                            //progressDialog.setTitle(context.getString(R.string.customers));
                            SendDao.sendCustomers();
                            break;
                        case R.id.bt_senddatas_collects:
                            //progressDialog.setTitle(context.getString(R.string.collects));
                            if (CustomerDao.isThereNewCustomer())
                                SendDao.sendCustomers();
                            SendDao.sendCollects();
                            break;
                        case R.id.bt_senddatas_transfers:
                            SendDao.sendAllStockTransfer();
                            break;

                        case R.id.bt_senddatas_alldatas:
                            //progressDialog.setTitle(context.getString(R.string.updating_the_database));

                            if (ProductDao.isThereNewOrUpdatedProduct())
                                SendDao.sendProducts();

                            if (CustomerDao.isThereNewCustomer())
                                SendDao.sendCustomers();

                            SendDao.sendChangedPrices();

                            SendDao.sendCollects();

                            SendDao.sendAllDelivery();

                            SendDao.sendPrintLabel();

                            SendDao.sendOrder();

                            SendDao.sendInventur();

                            SendDao.sendAllInvoice();

                            if (Dao.Language == "tr") {
                                SendDao.sendAllStockTransfer();
                            }
                            break;
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

                progressDialog.dismiss();
            }
        }.start();


        bt.setEnabled(true);
        bt.getBackground().setColorFilter(null);
    }
}
