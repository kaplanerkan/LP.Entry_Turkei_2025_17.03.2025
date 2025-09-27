package com.eqpos.eqentry;

import android.content.DialogInterface;
import android.content.Intent;

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
import android.widget.Toast;

import com.eqpos.eqentry.databinding.ActivityMainMenuBinding;
import com.eqpos.eqentry.db.Dao;
import com.eqpos.eqentry.db.Database;
import com.eqpos.eqentry.db.WarehouseDao;
import com.eqpos.eqentry.models.DepoModel;
import com.eqpos.eqentry.tools.JSONProcess;
import com.eqpos.eqentry.tools.SharedPrefUtil;
import com.eqpos.eqentry.tools.SocketProcess;
import com.eqpos.eqentry.tools.Variables;
import com.eqpos.eqentry.views.depo_secimi.DepoDialogFragment;
import com.eqpos.eqentry.views.varyants.Varyants;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static android.view.View.GONE;

import java.util.List;

public class MainMenu extends AppCompatActivity implements View.OnClickListener {
    private String m_Text = "";
    private int _SELECTUSER=100;

    private ActivityMainMenuBinding binding;

    @Override
    protected void onResume() {
        super.onResume();
        SharedPrefUtil.putInt(SharedPrefUtil.KEY_SELECTED_DEPO_ID, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_main_menu);
        binding = ActivityMainMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setTitle(R.string.app_name);

        Database.vtContext = this;
        initViews();

        binding.btTransfers.setEnabled(Dao.Language == "tr");

        binding.btProducts.setVisibility((Variables.showbtProducts) ? View.VISIBLE  : GONE );
        binding.btChangePrice.setVisibility((Variables.showbtChangePrice) ? View.VISIBLE  : GONE );
        binding.btPrintLabel.setVisibility((Variables.showbtPrintLabel) ? View.VISIBLE  : GONE );
        binding.btPurchaseOrder.setVisibility((Variables.showbtPurchaseOrder) ? View.VISIBLE  : GONE );
        binding.btStockEntry.setVisibility((Variables.showbtStockEntry) ? View.VISIBLE  : GONE );
        binding.btSendDatas.setVisibility((Variables.showbtSendDatas) ? View.VISIBLE  : GONE );
        binding.btInventory.setVisibility((Variables.showbtInventory) ? View.VISIBLE  : GONE );
        binding.btInvoices.setVisibility((Variables.showbtInvoices) ? View.VISIBLE  : GONE );
        binding.btCustomers.setVisibility((Variables.showbtCustomers) ? View.VISIBLE  : GONE );
        binding.btTransfers.setVisibility((Variables.showbtTransfers) ? View.VISIBLE  : GONE );
        binding.btManagement.setVisibility((Variables.showbtManagement) ? View.VISIBLE  : GONE );
        binding.btReports.setVisibility((Variables.showbtReports) ? View.VISIBLE  : GONE );

    }

    private void initViews() {

        binding.btProducts.setOnClickListener(view -> {
            Intent productsintent = new Intent(MainMenu.this, ProductsActivity.class);
            startActivity(productsintent);
        });

        binding.btChangePrice.setOnClickListener(this);
        binding.btPrintLabel.setOnClickListener(this);
        binding.btPurchaseOrder.setOnClickListener(this);
        binding.btStockEntry.setOnClickListener(this);
        binding.btSendDatas.setOnClickListener(this);
        binding.btInventory.setOnClickListener(this);
        binding.btInvoices.setOnClickListener(this);
        binding.btCustomers.setOnClickListener(this);
        binding.btTransfers.setOnClickListener(this);

        binding.btnAddVaryants.setOnClickListener(view -> {
            Intent varyants = new Intent(MainMenu.this, Varyants.class);
            startActivity(varyants);
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btChangePrice:
                Intent changePriceIntent = new Intent(this, ChangePriceActivity.class);
                startActivity(changePriceIntent);
                break;
            case R.id.btPrintLabel:
                Intent printLabelIntent = new Intent(this, PrintLabelActivity.class);
                startActivity(printLabelIntent);
                break;
            case R.id.btPurchaseOrder:
                Intent purchaseOrderIntent = new Intent(this, PurchaseOrderActivity.class);
                startActivity(purchaseOrderIntent);
                break;
            case R.id.btStockEntry:
                Intent stockEntryListIntent = new Intent(this, StockEntryListActivity.class);
                startActivity(stockEntryListIntent);
                break;
            case R.id.btInventory:


                // Depo listesini hazırla (örnek veri, sen DB'den çek)
                List<DepoModel> depoList = WarehouseDao.getAllWarehouses();
                if (depoList.size() == 1){
                    SharedPrefUtil.putInt(SharedPrefUtil.KEY_SELECTED_DEPO_ID, depoList.get(0).getId());
                    Intent inventoryIntent = new Intent(this, InventurActivity.class);
                    startActivity(inventoryIntent);
                } else if (depoList.size() > 1) {
                    // Dialog'ı aç ve depo listesini geçir
                    DepoDialogFragment dialog = new DepoDialogFragment((depoId, depoIsmi) -> {

                        Log.e("DepoDialog", "Seçilen Depo ID: " + depoId + ", Depo İsmi: " + depoIsmi);
                        SharedPrefUtil.putInt(SharedPrefUtil.KEY_SELECTED_DEPO_ID, depoId );
                        Intent inventoryIntent = new Intent(this, InventurActivity.class);
                        startActivity(inventoryIntent);

                    }, depoList);

                    dialog.setCancelable(false);
                    dialog.show(getSupportFragmentManager(), "depo_dialog");
                }else {
                    Toast.makeText(MainMenu.this, R.string.depo_bulunamadi, Toast.LENGTH_SHORT).show();

                }







                break;
            case R.id.btInvoices:

                // Depo listesini hazırla (örnek veri, sen DB'den çek)
                List<DepoModel> depoList2 = WarehouseDao.getAllWarehouses();
                if (depoList2.size() == 1){
                    SharedPrefUtil.putInt(SharedPrefUtil.KEY_SELECTED_DEPO_ID, depoList2.get(0).getId());
                    Intent invoiceIntent = new Intent(this, InvoiceActivity.class);
                    startActivity(invoiceIntent);
                } else if (depoList2.size() > 1) {
                    // Dialog'ı aç ve depo listesini geçir
                    DepoDialogFragment dialog = new DepoDialogFragment((depoId, depoIsmi) -> {

                        Log.e("DepoDialog", "Seçilen Depo ID: " + depoId + ", Depo İsmi: " + depoIsmi);
                        SharedPrefUtil.putInt(SharedPrefUtil.KEY_SELECTED_DEPO_ID, depoId );
                        Intent invoiceIntent = new Intent(this, InvoiceActivity.class);
                        startActivity(invoiceIntent);

                    }, depoList2);

                    dialog.setCancelable(false);
                    dialog.show(getSupportFragmentManager(), "depo_dialog");
                }else {
                    Toast.makeText(MainMenu.this, R.string.depo_bulunamadi, Toast.LENGTH_SHORT).show();

                }






                break;
            case R.id.btCustomers:
                Intent customersIntent = new Intent(this, CustomersActivity.class);
                startActivity(customersIntent);
                break;
            case R.id.btTransfers:
                Intent TransferIntent = new Intent(this, StockTransferListActivity.class);
                startActivity(TransferIntent);
                break;

            case R.id.btSendDatas:
                //burada admin şifresi istenecek
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.prompt_password));

                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER| InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                builder.setView(input);

                builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                    m_Text = input.getText().toString();

                    if (!m_Text.isEmpty()) {
                        if (isAdmin(m_Text)) {

                            Intent sendDatasIntent = new Intent(builder.getContext(), SendDataActivity.class);
                            startActivity(sendDatasIntent);
                        }
                        m_Text = "";
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                    dialog.cancel();
                    m_Text = "";
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
                        (dialog, id) -> dialog.cancel());
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
