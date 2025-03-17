package com.eqpos.eqentry;

import static com.eqpos.eqentry.tools.SharedPrefUtil.KEY_BLE_PRINTER_STATUS;

import android.app.Application;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.design.widget.FloatingActionButton;
//import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.eqpos.eqentry.DB.Dao;
import com.eqpos.eqentry.DB.Database;
import com.eqpos.eqentry.DB.SettingsDao;
import com.eqpos.eqentry.Printing.PrintDao;
import com.eqpos.eqentry.tools.Bluetooth;
import com.eqpos.eqentry.tools.JSONProcess;
import com.eqpos.eqentry.tools.SharedPrefUtil;
import com.eqpos.eqentry.tools.SocketProcess;
import com.eqpos.eqentry.tools.Variables;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;



/**
 * Created by developer on 17.08.2017.
 * Bu Classda işleyiş
 * 1- Veritabanından ayarları oku
 * 2- Network Bağlantısını Kontrol et
 * 3- Kullanıcı şifresini kontrol için Socket üzerinden gönder.
 * 4- Gelen cevaba göre login ol yada reddet
 */

public class LoginActivity extends Activity  {
    private Button btLogin;
    private FloatingActionButton btSettings;
    private EditText edPassword;
    private TextView lblLoginTitle;
    private TextView lbl_login_version;
    private Spinner cmbUserName;
    private ImageButton btLogo;
    private String m_Text = "";

    private ArrayList<String> gUsers;

    private String gUsername = "";
    private String gPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        try {

            SharedPrefUtil.init(LoginActivity.this);

            Database.vtContext =
            Variables.context =getApplicationContext(); getApplicationContext();
            //deleteDatabase(Database.VERITABANI);

            Variables.hostIp = SettingsDao.getStrValue("ServerIP");
            Variables.hostPort = SettingsDao.getIntValue("ServerPort");
            Variables.printerAddress = SettingsDao.getStrValue("PrinterAddress");
            Variables.isCreditCardActive = SettingsDao.getIntValue("isCreditCardActive") == 1;
            Variables.isPrintTaxOnInvoice = SettingsDao.getIntValue("PrintTaxOnInvoice", 1)>0;
            Variables.showUnitPrice = SettingsDao.getIntValue("ShowUnitPrice", 1)>0;

            Variables.showbtChangePrice = SettingsDao.getIntValue("showbtChangePrice", 1)>0;
            Variables.showbtCustomers = SettingsDao.getIntValue("showbtCustomers", 1)>0;
            Variables.showbtInventory = SettingsDao.getIntValue("showbtInventory", 1)>0;
            Variables.showbtInvoices = SettingsDao.getIntValue("showbtInvoices", 1)>0;
            Variables.showbtPrintLabel = SettingsDao.getIntValue("showbtPrintLabel", 1)>0;
            Variables.showbtPurchaseOrder = SettingsDao.getIntValue("showbtPurchaseOrder", 1)>0;
            Variables.showbtSendDatas = SettingsDao.getIntValue("showbtSendDatas", 1)>0;
            Variables.showbtStockEntry = SettingsDao.getIntValue("showbtStockEntry", 1)>0;
            Variables.showbtTransfers = SettingsDao.getIntValue("showbtTransfers", 1)>0;
            Variables.showbtProducts = SettingsDao.getIntValue("showbtProducts", 1)>0;

            if (SharedPrefUtil.getBoolean(KEY_BLE_PRINTER_STATUS, false)) {
                Bluetooth.connectToPrinter( Variables.printerAddress);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Dao.LanguageId = SettingsDao.getIntValue("Language");

            switch (Dao.LanguageId) {
                case 0:
                    Dao.Language = "de";
                    break;
                case 1:
                    Dao.Language = "fr";
                    break;
                case 2:
                    Dao.Language = "nl";
                    break;
                case 3:
                    Dao.Language = "tr";
                    break;
                case 4:
                    Dao.Language = "en";
                    break;
                default:
                    Dao.Language = "en";
                    break;
            }
            setLocale(Dao.Language);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Dao.CurrencyId = SettingsDao.getIntValue("Currency");

            switch (Dao.CurrencyId) {
                case 0:
                    Dao.Currency = "EUR";
                    break;
                case 1:
                    Dao.Currency = "TL";
                    break;
                case 2:
                    Dao.Currency = "£";
                    break;
                case 3:
                    Dao.Currency = "CHF";
                    break;

                default:
                    Dao.Currency = "EUR";
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Variables.serialNumber = android.provider.Settings.Secure.getString(this.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID) + android.os.Build.SERIAL;



        this.setTitle(R.string.title_activity_login);

        this.setTitle(getText(R.string.app_name) + ":: " + getString(R.string.action_sign_in) + getResources().getString(R.string.app_version));
        SocketProcess.isConnectWiFiNetwork(this, true);


        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        //lblLoginTitle = (TextView) findViewById(R.id.lbl_login_title);
        lbl_login_version = (TextView)findViewById(R.id.lbl_login_version);
        setVersion();
        btLogin = (Button) findViewById(R.id.bt_login_login);
        btSettings = (FloatingActionButton) findViewById(R.id.bt_login_settings);
        edPassword = (EditText) findViewById(R.id.ed_login_password);
        //cmbUserName = (Spinner) findViewById(R.id.cmb_login_username);
        //btLogo = (ImageButton) findViewById(R.id.imgLoginLogo);

        btSettings.setOnClickListener(v -> {

            //burada admin şifresi istenecek
            final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setTitle(getString(R.string.prompt_password));

            final EditText input = new EditText(LoginActivity.this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER| InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            builder.setView(input);

            builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                m_Text = input.getText().toString();

                if (m_Text.length() > 0) {
                    if (m_Text.equals("0000")) {

                        Intent myIntent = new Intent(LoginActivity.this, Settings.class);
                        startActivityForResult(myIntent, 100);
                    }
                    m_Text = "";
                }
            });
            builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                dialog.cancel();
                m_Text = "";
            });

            builder.show();


        });

//        try {
//            gPassword = SettingsDao.getStrValue("userpassword");
//            gUsername = SettingsDao.getStrValue("username");
//        } catch (Exception e) {
//            gPassword = "";
//            gUsername = "";
//            //btSettings.performClick();
//        }
//
//        try {
//            getUsers();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        btLogin.setOnClickListener(v -> {
            try {
                String lPass = SettingsDao.getStrValue("userpassword");
                getLogin(lPass);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void setVersion() {


        String versionText = "";
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            int versionCode = pInfo.versionCode;
            versionText += String.format("\n Version: %s -- Build:%s ", versionCode, version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            versionText += String.format("\n Version: %s -- Build:%s", "FEHLER", "FEHLER");
        }
        lbl_login_version.setText(versionText);
    }




    private void getLogin(String pass) throws IOException { // Bu kısım Loof için Farklı
        String lSifre = edPassword.getText().toString();
        if (pass.length() > 0 && lSifre.equals(pass)) {
            Variables.userId = SettingsDao.getIntValue("userid");
            Variables.userName = SettingsDao.getStrValue("username");

            Intent siparis = new Intent(this, ProductsActivity.class);
            startActivity(siparis);
            finish();
        } else {
            if ( Variables.hostIp != "" &&  Variables.hostIp != null &&  Variables.hostIp.length() >= 9) {
                //if (SocketProcess.isConnectWiFiNetwork(this, true)) {

                    //if (!BuildConfig.DEBUG ) {
                    SocketProcess.context = this;
                    String rMsg = "";
                    try {

                        JsonObject jHead = JSONProcess.getJSONHeader( Variables.ServerCommand.cmdLogin.getValue());
                        JsonObject jData = new JsonObject();
                        jData.addProperty("password", edPassword.getText().toString());
                        String msg = JSONProcess.jsonPack(jHead, jData);
                        rMsg = SocketProcess.sendMessage(msg);
                        if (rMsg != "") {
                            JsonParser parser = new JsonParser();
                            JsonObject jResult = parser.parse(rMsg).getAsJsonArray().get(0).getAsJsonObject();
                            Variables.userId = jResult.get("id").getAsInt();
                            Variables.userName = jResult.get("adsoyad").getAsString() + "( " + jResult.get("kullaniciadi").getAsString() + " )";
                        }
                    } catch (Exception e) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                        builder1.setMessage(getString(R.string.error_couldnot_connect_to_the_server));
                        builder1.setCancelable(true);
                        builder1.setPositiveButton("Ok",
                                (dialog, id) -> dialog.cancel());
                        AlertDialog alert11 = builder1.create();
                        alert11.show();
                        try {
                            SocketProcess.client.close();
                            SocketProcess.client = null;
                        } catch (Exception ex) {
                        }
                        return;
                    }

                    if (rMsg == null || rMsg.length() == 0 || rMsg.contains( Variables._RETURNFAULT)) {
                        final Toast mesaj = Toast.makeText(this, getString(R.string.error_incorrect_password), Toast.LENGTH_LONG);
                        mesaj.show();
                    } else if (rMsg.contains( Variables._ERROR)) {
                        final Toast mesaj = Toast.makeText(this, getString(R.string.error_couldnot_connect_to_the_server), Toast.LENGTH_LONG);
                        mesaj.show();
                    } else if (rMsg.contains( Variables._UNREGISTER)) {
                        final Toast mesaj = Toast.makeText(this, getString(R.string.error_unregistered_device), Toast.LENGTH_LONG);
                        mesaj.show();
                    } else {

                        SettingsDao.setStrValue("userpassword", edPassword.getText().toString());
                        SettingsDao.setIntValue("userid",  Variables.userId);
                        SettingsDao.setStrValue("username",  Variables.userName);
                        Intent siparis = new Intent(this, ProductsActivity.class);
                        startActivity(siparis);
                        finish();
                    }
//                    } else {
//
//                        SettingsDao.setStrValue("userpassword", edPassword.getText().toString());
//                        Intent siparis = new Intent(this, MainMenu.class);
//                        startActivity(siparis);
//                        finish();
//                    }
                //}
            } else {
                Intent intent = new Intent(this, Settings.class);
                startActivity(intent);
            }
        }

    }

//    private void getUsers() throws IOException {
//        gUsers = new ArrayList<String>();
//        if (gUsername.length() > 0) {
//            gUsers.add(gUsername);
//        } else {
//
//            if (Variables.hostIp != "" && Variables.hostIp != null && Variables.hostIp.length() >= 9) {
//                if (SocketProcess.isConnectWiFiNetwork(this, true)) {
//
//                    //if (!BuildConfig.DEBUG ) {
//                    SocketProcess.context = this;
//                    String rMsg = "";
//                    try {
//
//                        JsonObject jHead = JSONProcess.getJSONHeader(Variables.ServerCommand.cmdGetUsers.getValue());
//                        JsonObject jData = new JsonObject();
//                        String msg = JSONProcess.jsonPack(jHead, jData);
//                        rMsg = SocketProcess.sendMessage(msg);
//                        if (rMsg != "") {
//                            JsonParser parser = new JsonParser();
//                            JsonArray lArr = parser.parse(rMsg).getAsJsonArray();
//
//                            try {
//                                for (int i = 0; i < lArr.size(); i++) {
//                                    jData = lArr.get(i).getAsJsonObject();
//
//                                    gUsers.add(jData.get("kullaniciadi").getAsString());
//                                }
//                            } catch (Exception e) {
//                                Log.e("getUsers Error", e.toString());
//                            }
//                        }
//                    } catch (Exception e) {
//                        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
//                        builder1.setMessage(getString(R.string.error_couldnot_connect_to_the_server));
//                        builder1.setCancelable(true);
//                        builder1.setPositiveButton("Ok",
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int id) {
//                                        dialog.cancel();
//                                    }
//                                });
//                        AlertDialog alert11 = builder1.create();
//                        alert11.show();
//                        try {
//                            SocketProcess.client.close();
//                            SocketProcess.client = null;
//                        } catch (Exception ex) {
//                        }
//                        return;
//                    }
//
//                    if (rMsg == null || rMsg.length() == 0 || rMsg.contains(Variables._RETURNFAULT)) {
//                        final Toast mesaj = Toast.makeText(this, getString(R.string.error_incorrect_password), Toast.LENGTH_LONG);
//                        mesaj.show();
//                    } else if (rMsg.contains(Variables._ERROR)) {
//                        final Toast mesaj = Toast.makeText(this, getString(R.string.error_couldnot_connect_to_the_server), Toast.LENGTH_LONG);
//                        mesaj.show();
//                    } else if (rMsg.contains(Variables._UNREGISTER)) {
//                        final Toast mesaj = Toast.makeText(this, getString(R.string.error_unregistered_device), Toast.LENGTH_LONG);
//                        mesaj.show();
//                    }
//                }
//            } else {
//                Intent intent = new Intent(this, Settings.class);
//                startActivity(intent);
//            }
//        }
//
//        ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this,
//                android.R.layout.simple_spinner_dropdown_item,
//                gUsers);
//        cmbUserName.setAdapter(spinnerArrayAdapter);
//
//        cmbUserName.setSelection(gUsers.indexOf(gUsername));
//    }


    private String paralosifrele(String pass) {
        String Src;
        Src = pass;
        String lSrc;
        String lSrcY;

        int i;
        lSrc = "";

        for (i = 0; i < Src.length(); i++)

        {
            // Log.e("char", String.valueOf((int)(Src.charAt(i)))) ;
            lSrc = lSrc + String.valueOf((int) (Src.charAt(i))) + 7;
        }
        //Log.e("ilk fordan çıkış lsrc",lSrc);

        if (lSrc.length() % 2 == 1) {
            lSrc = lSrc + 7;
        }
        //Log.e("ilk if den çıkış lsrc",lSrc);

        i = 0;
        lSrcY = "";

        do {
            //Log.e(String.valueOf(i), lSrc+ " ---- " +lSrcY);

            // lSrcY=lSrcY+String.valueOf(Integer.parseInt(lSrc.substring(i,1))*Integer.parseInt(lSrc.substring(i+1,1)));
            lSrcY = lSrcY + String.valueOf(Integer.parseInt(String.valueOf(lSrc.charAt(i))) * Integer.parseInt(String.valueOf(lSrc.charAt(i + 1))));
            i = i + 2;
        } while (i < lSrc.length());

        //Log.e("ilk dowhile çıkış lsrcY",lSrcY);


        if (lSrcY.length() % 2 == 1) {
            lSrcY = lSrcY + 3;
        }
        Log.e("if2 Lsrcy ", lSrcY);

        i = 0;
        lSrc = "";
        //bu ikinci do/while
        do {
            lSrc = lSrc + String.valueOf(Integer.parseInt(lSrcY.substring(i, i + 2)) * Integer.parseInt(lSrcY.substring(i + 1, i + 3)));

            Log.e(String.valueOf(i), lSrc);
            i = i + 3;
        } while (i < lSrcY.length() - 1);
        //bu ikinci do/while
        //Log.e("ikinci while lsrc= ",lSrc);


        for (i = 0; i < Src.length(); i++) {
            switch ((i + 1) % 5) {
                case 0:
                    lSrc = lSrc + String.valueOf((int) (Src.charAt(i))) + 0;
                    break;
                case 1:
                    lSrc = lSrc + String.valueOf((int) (Src.charAt(i))) + 8;
                    break;
                case 2:
                    lSrc = lSrc + String.valueOf((int) (Src.charAt(i))) + 3;
                    break;
                case 3:
                    lSrc = lSrc + String.valueOf((int) (Src.charAt(i))) + 6;
                    break;
                case 4:
                    lSrc = lSrc + String.valueOf((int) (Src.charAt(i))) + 9;
                    break;
            }
        }

        //Log.e("LSrc Sonuç= ", lSrc);

        return lSrc;
    }


    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.msg_doubleclickforexit, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }


    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        getBaseContext().getResources().updateConfiguration(conf, getBaseContext().getResources().getDisplayMetrics());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == RESULT_OK && requestCode == 100) {
//            if (data.getBooleanExtra("issync", false)) {
//                gPassword = "";
//                gUsername = "";
//
//                try {
//                    getUsers();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }

    }

}