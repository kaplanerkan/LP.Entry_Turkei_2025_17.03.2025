package com.eqpos.eqentry;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.ParcelFormatException;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.eqpos.eqentry.Adapters.ProductListAdapter;
import com.eqpos.eqentry.DB.InvoiceDao;
import com.eqpos.eqentry.DB.OrderDao;
import com.eqpos.eqentry.DB.ProductDao;
import com.eqpos.eqentry.DB.SendDao;
import com.eqpos.eqentry.Models.Product;
import com.eqpos.eqentry.tools.CaptureActivityPortrait;
import com.eqpos.eqentry.tools.JSONProcess;
import com.eqpos.eqentry.tools.SocketProcess;
import com.eqpos.eqentry.tools.Tools;
import com.eqpos.eqentry.tools.Variables;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

public class PurchaseOrderActivity extends AppCompatActivity implements View.OnClickListener {
    private SwipeMenuListView lsList;
    private EditText edFind;
    private Button btFind;
    private Button btBarcode;
    private Button btAdd;
    private Button btCancel;
    private Button btSend;
    private Button btSelectSupplier;
    private EditText edAmount;
    private TextView lblProductName;
    private TextView lblCount;
    private TextView lblSupplierName;
    private ToggleButton btPackage;

    private int lastPosition = 0;
    private int gSupplierId = 0;
    private Product gProduct;
    private ArrayList<HashMap<String, String>> gProductList;
    private int _SELECTPRODUCT = 1011;
    private int _SELECTSUPPLIER = 1013;

    private String m_Text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_order);

        this.setTitle(R.string.purchaseorder);

        lsList = (SwipeMenuListView) findViewById(R.id.ls_purchase_list);
        edAmount = (EditText) findViewById(R.id.ed_purchase_amount);
        edFind = (EditText) findViewById(R.id.ed_purchase_find);
        btBarcode = (Button) findViewById(R.id.bt_purchase_barcode);
        btAdd = (Button) findViewById(R.id.bt_purchase_add);
        btFind = (Button) findViewById(R.id.bt_purchase_find);
        btCancel = (Button) findViewById(R.id.bt_purchase_cancel);
        btSend = (Button) findViewById(R.id.bt_purchase_send);
        btSelectSupplier = (Button) findViewById(R.id.bt_purchase_select_supplier);
        lblProductName = (TextView) findViewById(R.id.lbl_purchase_productname);
        lblCount = (TextView) findViewById(R.id.lbl_purchase_productcount);
        lblSupplierName = (TextView) findViewById(R.id.lbl_purchase_suppliername);
        btPackage = (ToggleButton) findViewById(R.id.bt_purchase_package);

        btBarcode.setOnClickListener(this);
        btFind.setOnClickListener(this);
        btAdd.setOnClickListener(this);
        btCancel.setOnClickListener(this);
        btSend.setOnClickListener(this);
        btSelectSupplier.setOnClickListener(this);

        gProduct = null;
        gSupplierId = 0;
        lblSupplierName.setText("");
        lblProductName.setText("");
        edAmount.setText("");
        getProductList(gSupplierId);
        listProducts();
        lblSupplierName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (BuildConfig.DEBUG) {
                    OrderDao.createRandomOrder(gSupplierId);
                    getProductList(gSupplierId);
                    listProducts();
                }
                return false;
            }
        });

        createSwipeListview();

        lsList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                int lProductId = Integer.parseInt(gProductList.get(position).get("productid"));
                int lId = Integer.parseInt(gProductList.get(position).get("id"));
                lastPosition = position;
                switch (index) {
                    case 0:
                        //Edit Product
                        gProduct = ProductDao.getProduct(lProductId, "");
                        if (gProduct.getId()>0) {
                            lblProductName.setText(gProduct.getProductName());

                        }
                        Double lAmount = 0.0;
                        try {
                            lAmount = Variables.strToDouble(gProductList.get(position).get("amount"));
                        } catch (Exception e) {
                            Log.e("Order amount", e.getMessage());
                            lAmount = 0.0;
                        }
                        edAmount.setText(Variables.doubleToStr(lAmount,2));
                        edAmount.requestFocus();
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                        break;

                    case 1:
                        //remove from order list
                        OrderDao.removeFromOrder(lId);
                        getProductList(gSupplierId);
                        listProducts();
                        break;

                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        edFind.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER) ) {
                    btFind.performClick();
                    return true;
                }
                return false;
            }
        });

        if (BuildConfig.DEBUG) {
            lblProductName.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    OrderDao.addProductToOrderRandom(28);

                    getProductList(gSupplierId);
                    listProducts();
                    return false;
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == _SELECTPRODUCT) {

            int lProductId;
            lProductId = data.getIntExtra("id", 0);

            gProduct = ProductDao.getProduct(lProductId,"");
            lblProductName.setText(gProduct.getProductName());
            btPackage.setTextOff(gProduct.getUniteName());
            btPackage.setChecked(false);
            edFind.setText("");
            edAmount.setText("");
            edAmount.requestFocus();
            edAmount.performClick();
            Tools.showSoftKeyboard(this, edAmount);

        } else if (resultCode == RESULT_OK && requestCode == _SELECTSUPPLIER) {
            gSupplierId = data.getIntExtra("id", 0);
            lblSupplierName.setText(data.getStringExtra("suppliername"));

            getProductList(gSupplierId);
            listProducts();

        } else if (resultCode == RESULT_OK) {
            IntentResult scannResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            if (scannResult != null) {
                String contents = data.getStringExtra("SCAN_RESULT");
                edFind.setText(contents);
                getProduct();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_purchase_barcode:
                openBarcode();
                break;
            case R.id.bt_purchase_find:
                getProduct();
                break;
            case R.id.bt_purchase_select_supplier:
                Intent intent = new Intent(this, SelectSupplierActivity.class);
                startActivityForResult(intent, _SELECTSUPPLIER);
                break;
            case R.id.bt_purchase_add:
                if (gProduct == null) {
                    return;
                }

                Double lAmount = 0.0;
                String lUnit = "";
                if (btPackage.isChecked())
                    lUnit = btPackage.getText().toString();
                else
                    lUnit = gProduct.getUniteName();

                try {
                    lAmount = Variables.strToDouble(edAmount.getText().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                    lAmount = 0.0;
                }
                OrderDao.addProductToOrder(gSupplierId, gProduct.getId(), lAmount, lUnit);
                gProduct = null;
                edFind.setText("");
                edAmount.setText("");
                lblProductName.setText("");
                edFind.requestFocus();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

                getProductList(gSupplierId);
                listProducts();
                break;

            case R.id.bt_purchase_cancel:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(PurchaseOrderActivity.this);
                builder2.setTitle(R.string.delete);
                builder2.setMessage(R.string.msg_areyousure);
                builder2.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });


                builder2.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        OrderDao.removeOrder();
                        getProductList(gSupplierId);
                        listProducts();

                    }
                });


                builder2.show();
                break;

            case R.id.bt_purchase_send:
                //burada admin şifresi istenecek
                if (gProductList.size() > 0) {
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

                                    SendDao.sendOrder();
                                    getProductList(gSupplierId);
                                    listProducts();
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


    private void listProducts() {
        if (gProductList != null) {
            SimpleAdapter adp = new SimpleAdapter(this, gProductList, R.layout.orderlist_row,
                    new String[]{"barcode", "productname", "amount", "unit"},
                    new int[]{R.id.lbl_orderlistrow_barcode, R.id.lbl_orderlistrow_productname, R.id.lbl_orderlistrow_amount,
                            R.id.lbl_orderlistrow_unit});
            lsList.setAdapter(adp);

            lblCount.setText(String.valueOf(gProductList.size()));
        }
    }

    private void getProductList(int supplierId) {
        gProductList = OrderDao.getOrderList(supplierId);
        btSend.setEnabled(gProductList.size()>0);
        btCancel.setEnabled(gProductList.size()>0);
    }

    private void openBarcode() {
        IntentIntegrator scanIntegrator = new IntentIntegrator(PurchaseOrderActivity.this);
        scanIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
        scanIntegrator.setOrientationLocked(true);
        scanIntegrator.initiateScan();
    }

    private void getProduct() {
        gProduct = null;

        ArrayList<HashMap<String, String>> lList = ProductDao.getProductList(edFind.getText().toString(), 0, "", "", "productname", "asc", "");

        if (lList.size()==1) {
            gProduct = ProductDao.getProduct(Integer.parseInt(lList.get(0).get("id")),"");
            lblProductName.setText(gProduct.getProductName());
            btPackage.setTextOff(gProduct.getUniteName());
            btPackage.setChecked(false);
            edAmount.setText("");
            edFind.setText("");
            edAmount.requestFocus();
            edAmount.performClick();
            Tools.showSoftKeyboard(this, edAmount);
        } else if (lList.size() > 1) {
            Intent intent = new Intent(this, SelectProductActivity.class);
            intent.putExtra("search", edFind.getText().toString());
            startActivityForResult(intent, _SELECTPRODUCT);
        }
    }


    private void createSwipeListview() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                //create changeprice item
                SwipeMenuItem EditItem = new SwipeMenuItem(getApplicationContext());
                EditItem.setBackground(R.color.colorWhite);
                EditItem.setTitleColor(R.color.colorBlack);
                EditItem.setWidth(Variables.dp2px(90));
                EditItem.setIcon(R.mipmap.img_editproduct);
                EditItem.setTitle(R.string.editproduct);
                EditItem.setTitleSize(12);
                menu.addMenuItem(EditItem);


                //create printlabel item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(R.color.colorWhite);
                deleteItem.setTitleColor(R.color.colorBlack);
                deleteItem.setWidth(Variables.dp2px(90));
                deleteItem.setIcon(R.mipmap.img_delete);
                deleteItem.setTitle(R.string.removefromorderlist);
                deleteItem.setTitleSize(12);
                menu.addMenuItem(deleteItem);

            }
        };
        lsList.setMenuCreator(creator);
    }
}
