package com.eqpos.eqentry;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.eqpos.eqentry.Adapters.InventurAdapter;
import com.eqpos.eqentry.DB.SendDao;
import com.eqpos.eqentry.tools.CaptureActivityPortrait;
import com.eqpos.eqentry.tools.JSONProcess;
import com.eqpos.eqentry.tools.SocketProcess;
import com.eqpos.eqentry.tools.Variables;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.HashMap;

import static android.widget.Toast.*;
import static com.eqpos.eqentry.DB.InventurDao.addNewProductsToInventur;
import static com.eqpos.eqentry.DB.InventurDao.getInventurList;

public class InventurActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btFind;
    private Button btBarcode;
    private Button btSend;
    private EditText edFind;
    private ListView lvList;
    private InventurAdapter adp;
    private Toast msg;

    private ArrayList<HashMap<String, String>> gList;

    private String m_Text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventur);
        this.setTitle(R.string.inventory);

        msg = Toast.makeText(this, "", Toast.LENGTH_LONG);
        btFind = (Button) findViewById(R.id.bt_inventur_find);
        btBarcode = (Button) findViewById(R.id.bt_inventur_barcode);
        btSend = (Button) findViewById(R.id.bt_inventur_send);
        edFind = (EditText) findViewById(R.id.ed_inventur_find);
        lvList = (ListView) findViewById(R.id.lv_inventur_list);

        btFind.setOnClickListener(this);
        btBarcode.setOnClickListener(this);
        btSend.setOnClickListener(this);
        edFind.performClick();
        edFind.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    edFind.setText("");
                }
            }
        });
        edFind.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.toString().isEmpty() || s.toString().endsWith("*"))
                    btFind.performClick();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        edFind.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction()==KeyEvent.ACTION_DOWN) {
                    btFind.performClick();
                    return true;
                } else
                    return false;
            }
        });
        listProducts();
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            IntentResult scannResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            if (scannResult != null) {
                String contents = data.getStringExtra("SCAN_RESULT");
                edFind.setText(contents);
                listProducts();
            }
        }
    }

    private void listProducts() {
        addNewProductsToInventur();
        gList = getInventurList(edFind.getText().toString());

        if (gList.size()>0) {
            adp = new InventurAdapter(this, gList);
            lvList.setAdapter(adp);
        } else {
            lvList.setAdapter(null);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_inventur_find:
                listProducts();
                break;
            case R.id.bt_inventur_barcode:
                openBarcode();
                break;
            case R.id.bt_inventur_send:
                if (gList.size() > 0) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getString(R.string.prompt_password));

                    final EditText input = new EditText(this);
                    input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                    builder.setView(input);

                    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            m_Text = input.getText().toString();

                            if (m_Text.length() > 0) {
                                if (isAdmin(m_Text)) {

                                    SendDao.sendInventur();
                                    finish();
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
                }
        }
    }

    private void openBarcode() {
        IntentIntegrator scanIntegrator = new IntentIntegrator(InventurActivity.this);
        scanIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
        scanIntegrator.setOrientationLocked(true);
        scanIntegrator.initiateScan();
    }
}
