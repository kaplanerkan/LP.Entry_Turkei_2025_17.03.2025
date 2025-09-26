/*
 * Bu sınıfta var olan bir irsaliyeyi ve ürünleri düzenlemek
  * ve yeni irsaliye oluşturma işlemleri yapılıyor.
  * Barkod okutarak veya barkodu elle girerek ürünleri ekleyebilir
  * ürünleri silebilir,
  * miktarlarını değiştirebilirsiniz
 */
package com.eqpos.eqentry;

import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.eqpos.eqentry.db.BarcodeDao;
import com.eqpos.eqentry.db.ProductDao;
import com.eqpos.eqentry.db.StockEntryDao;
import com.eqpos.eqentry.models.BarcodeSettings;
import com.eqpos.eqentry.models.Delivery;
import com.eqpos.eqentry.models.Product;
import com.eqpos.eqentry.tools.CaptureActivityPortrait;
import com.eqpos.eqentry.tools.SharedPrefUtil;
import com.eqpos.eqentry.tools.Variables;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.HashMap;

public class NewStockEntryActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btBarcode;
    private Button btFind;
    private EditText edFind;
    private EditText edDocumentNumber;
    private EditText edDocumentDate;
    private EditText edSupplierName;
    private EditText edReceiverName;
    private ImageButton btEditDocument;
    private SwipeMenuListView lsList;
    private TextView lblTotalAmount;

    Product gProduct;
    private ArrayList<HashMap<String, String>> gList;
    private int gStockEntryId = 0;
    private int lastPosition;
    private int _CREATESTOCKENTRY = 6000;
    private int _GETAMOUNT = 7000;
    private int _CHANGEAMOUNT = 9000;
    private int _SELECTPRODUCT = 8000;

    private Delivery gDelivery;
    private BarcodeSettings barcodeSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_entry);
        this.setTitle(R.string.newstockentry);

        gStockEntryId = 0;
        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            gStockEntryId = extra.getInt("deliveryid");
            StockEntryDao.deleteEntryFromStock(gStockEntryId);
        }

        btBarcode = (Button) findViewById(R.id.bt_stockentry_barcode);
        btFind = (Button) findViewById(R.id.bt_stockentry_find);
        edFind = (EditText) findViewById(R.id.ed_stockentry_find);
        edDocumentNumber = (EditText) findViewById(R.id.ed_stockentry_documentnumber);
        edDocumentDate = (EditText) findViewById(R.id.ed_stockentry_documentdate);
        edSupplierName = (EditText) findViewById(R.id.ed_stockentry_suppliername);
        edReceiverName = (EditText) findViewById(R.id.ed_stockentry_receiver);
        lsList = (SwipeMenuListView) findViewById(R.id.ls_stockentry_list);
        btEditDocument = (ImageButton) findViewById(R.id.bt_stockentry_editdocument);
        lblTotalAmount = (TextView) findViewById(R.id.lbl_stockentry_total_amount);

        barcodeSettings = BarcodeDao.getBarcodeSettings();

        btBarcode.setOnClickListener(this);
        btFind.setOnClickListener(this);
        btEditDocument.setOnClickListener(this);

        if (gStockEntryId > 0) {
            getDelivery();
            getDeliveryDetail();
        } else {
            createNewDelivery(0);
        }

        createSwipeListview();

        lsList.setOnMenuItemClickListener((position, menu, index) -> {
            int lId = Integer.parseInt(gList.get(position).get("id"));

            lastPosition = position;
            switch (index) {
                case 0:
                    //changeprice
                    editProduct(lId);
                    break;
                case 1:
                    //PrintLabel
                    StockEntryDao.removeProductFromDelivery(lId);
                    getDeliveryDetail();
                    break;

            }
            // false : close the menu; true : not close the menu
            return false;
        });



        edFind.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER) ) {
                btFind.performClick();
                return true;
            }
            return false;
        });
    }

    private void fillFields() {
        if (gDelivery.getId() > 0) {
            edDocumentDate.setText(gDelivery.getDate());
            edDocumentNumber.setText(gDelivery.getNumber());
            edSupplierName.setText(gDelivery.getSupplierName());
            edReceiverName.setText(gDelivery.getReceiverName());
        } else {
            gStockEntryId = 0;
            edDocumentDate.setText("");
            edDocumentNumber.setText("");
            edSupplierName.setText("");
            edReceiverName.setText("");

            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void getDelivery() {
        gDelivery = StockEntryDao.getDelivery(gStockEntryId);
        fillFields();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == _CREATESTOCKENTRY) {
            gStockEntryId = (int) data.getLongExtra("deliveryid", 0);
            if (gStockEntryId > 0) {
                gDelivery = StockEntryDao.getDelivery(gStockEntryId);
                fillFields();
            }
        }else if (resultCode == RESULT_OK && requestCode == _SELECTPRODUCT) {
            int lProductId;
            lProductId = data.getIntExtra("id", 0);

            gProduct = ProductDao.getProduct(lProductId,"");
            Intent intent = new Intent(this, StockEntryDetailAmountActivity.class);
            intent.putExtra("expirationdate", "");
            intent.putExtra("partnumber", "");
            intent.putExtra("unitname", gProduct.getUniteName());
            startActivityForResult(intent, _GETAMOUNT);

        } else if (resultCode == RESULT_OK && requestCode == _GETAMOUNT) {

            String lPartNumber;
            String lExpirationDate;
            Double lAmount;
            Double lCostPrice;

            try {
                lPartNumber = data.getStringExtra("partnumber");
            } catch (Exception e) {
                lPartNumber = "";
                Log.e("get partnumber", e.getMessage());
            }

            try {
                lExpirationDate = data.getStringExtra("expirationdate");
            } catch (Exception e) {
                lExpirationDate = "";
                Log.e("get expirationdate", e.getMessage());
            }
            try {
                lAmount = data.getDoubleExtra("amount", 0);
            } catch (Exception e) {
                lAmount = 0.0;
                Log.e("get amount", e.getMessage());
            }


            try {
                lCostPrice = data.getDoubleExtra("costprice", 0);
            } catch (Exception e) {
                lCostPrice = 0.0;
                Log.e("get costprice", e.getMessage());
            }

            boolean isPackage;
            if (data.getBooleanExtra("ispackage", false)) isPackage = true;
            else isPackage = false;

            getProduct(lPartNumber, lExpirationDate, lAmount, lCostPrice, false, isPackage);

        }else if (resultCode == RESULT_OK && requestCode == _CHANGEAMOUNT) {

            String lPartNumber;
            String lExpirationDate;
            Double lAmount;
            Double lCostPrice;

            try {
                lPartNumber = data.getStringExtra("partnumber");
            } catch (Exception e) {
                lPartNumber = "";
                Log.e("get partnumber", e.getMessage());
            }

            try {
                lExpirationDate = data.getStringExtra("expirationdate");
            } catch (Exception e) {
                lExpirationDate = "";
                Log.e("get expirationdate", e.getMessage());
            }
            try {
                lAmount = data.getDoubleExtra("amount", 0);
            } catch (Exception e) {
                lAmount = 0.0;
                Log.e("get amount", e.getMessage());
            }


            try {
                lCostPrice = data.getDoubleExtra("costprice", 0);
            } catch (Exception e) {
                lCostPrice = 0.0;
                Log.e("get costprice", e.getMessage());
            }

            getProduct(lPartNumber, lExpirationDate, lAmount, lCostPrice, true, false);

        } else if (resultCode == RESULT_OK) {
            IntentResult scannResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            if (scannResult != null) {
                String contents = data.getStringExtra("SCAN_RESULT");
                edFind.setText(contents);
                getProduct();
            }
        }

    }

    private void getDeliveryDetail() {
        gList = StockEntryDao.getDeliveryDetail(gStockEntryId);

        if (gList != null) {
            SimpleAdapter adp = new SimpleAdapter(this, gList, R.layout.stock_entry_detail_row,
                    new String[]{"productname", "packageamount", "amount", "unitename", "costprice", "total"},
                    new int[]{R.id.lbl_stockentry_detail_row_productname, R.id.lbl_stockentry_detail_row_packageamount,R.id.lbl_stockentry_detail_row_amount,
                            R.id.lbl_stockentry_detail_row_unitename, R.id.lbl_stockentry_detail_row_costprice,
                            R.id.lbl_stockentry_detail_row_total});
            lsList.setAdapter(adp);
            lsList.setSelection(lastPosition);
        }
        getTotalAmount();
    }

    private void createNewDelivery(int stockEnrtyId) {
        Intent createStockEntryIntent = new Intent(this, CreateStockEntryActivity.class);
        createStockEntryIntent.putExtra("deliveryid", stockEnrtyId);
        startActivityForResult(createStockEntryIntent, _CREATESTOCKENTRY);
    }

    private void getProduct() {
        String barcode = edFind.getText().toString();
        String plu = barcode;
        String barcodeValue = "";
        String lPre = "";
        int lFilterType = 0;
        if (plu.length()>2)
            lPre = plu.substring(0, 2);
        boolean isQuantity = true;
        if (plu.length()>=12) {
            if (barcodeSettings.getQ_Prefix().contains(lPre) && barcodeSettings.getQ_Prefix().length() > 0) {
                int lStart = 2;
                int lEnd = lStart + barcodeSettings.getQ_PLU();
                plu = barcode.substring(lStart, lEnd);

                lStart = lEnd;
                lEnd = lStart + barcodeSettings.getQ_Value();
                lFilterType = 2;
                barcodeValue = barcode.substring(lStart, lEnd);
            } else if (barcodeSettings.getP_Prefix().contains(lPre) && barcodeSettings.getP_Prefix().length() > 0) {
                int lStart = 2;
                int lEnd = lStart + barcodeSettings.getP_PLU();
                plu = barcode.substring(lStart, lEnd);

                lStart = lEnd;
                lEnd = lStart + barcodeSettings.getP_Value();

                barcodeValue = barcode.substring(lStart, lEnd);
                lFilterType = 2;
                isQuantity = false;
            }
        }

        ArrayList<HashMap<String, String>> lList = ProductDao.getProductList(plu, lFilterType, "", "", "productname", "asc", "");

        if (lList.size()==1) {
            gProduct = ProductDao.getProduct(Integer.parseInt(lList.get(0).get("id")),"");

            //Eger urun eklenmisse miktarini artıracak
            boolean isUpdate = false;
            double lAmount = 0.0;
            if (barcodeValue.length() > 0) {
                lAmount = Double.parseDouble(barcodeValue) / 1000;
            } else {
                lAmount = 1;
            }
            if (!StockEntryDao.addProductAmount(gStockEntryId, gProduct.getId(), lAmount, (isQuantity && barcodeValue.length() > 0))) {

                Intent intent = new Intent(this, StockEntryDetailAmountActivity.class);
                intent.putExtra("expirationdate", "");
                intent.putExtra("partnumber", "");
                intent.putExtra("unitname", gProduct.getUniteName());
                intent.putExtra("isquantity", isQuantity);
                if (barcodeValue.length() > 0)
                    intent.putExtra("barcodevalue", barcodeValue);
                startActivityForResult(intent, _GETAMOUNT);
            } else {
                getDeliveryDetail();
            }
        } else if (lList.size() > 1) {
            Intent intent = new Intent(this, SelectProductActivity.class);
            intent.putExtra("search", edFind.getText().toString());
            startActivityForResult(intent, _SELECTPRODUCT);
        }

//        gProduct = ProductDao.getProduct(0, edFind.getText().toString());
//        if (gProduct.getId() > 0) {
//            Intent intent = new Intent(this, StockEntryDetailAmountActivity.class);
//            startActivityForResult(intent, _GETAMOUNT);
//        } else {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setMessage(getString(R.string.error_couldnot_find_product));
//            builder.setCancelable(true);
//            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.cancel();
//                }
//            });
//            AlertDialog alertDialog = builder.create();
//            alertDialog.show();
//        }
        edFind.setText("");
    }


    private void getTotalAmount() {
        String lAmount = StockEntryDao.getTotalAmount(gStockEntryId);

        lblTotalAmount.setText(lAmount);
    }
    private void editProduct(int rowId) {
        gProduct = ProductDao.getProduct(Integer.parseInt(gList.get(lastPosition).get("productid")), "");
        String lExpirationDate = gList.get(lastPosition).get("expirationdate");
        String lPartnumber = gList.get(lastPosition).get("partnumber");
        Double lAmount = 0.0;
        Double lCostPrice = 0.0;
        try {
            lAmount = Variables.strToDouble(gList.get(lastPosition).get("amount"));
        } catch (Exception e) {
            lAmount = 1.0;
        }
        try {
            lCostPrice = Variables.strToDouble(gList.get(lastPosition).get("costprice").toString());
        } catch (Exception e) {
            lCostPrice = 0.0;
        }
        if (gProduct.getId() > 0) {
            Intent intent = new Intent(this, StockEntryDetailAmountActivity.class);
            intent.putExtra("expirationdate", lExpirationDate);
            intent.putExtra("partnumber", lPartnumber);
            intent.putExtra("amount", lAmount);
            intent.putExtra("costprice", lCostPrice);
            intent.putExtra("unitname", gProduct.getUniteName());
            intent.putExtra("rowid", rowId);
            startActivityForResult(intent, _CHANGEAMOUNT);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.error_couldnot_find_product));
            builder.setCancelable(true);
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        edFind.setText("");
    }

    private void getProduct(String partNumber, String expirationDate, Double amount, Double costPrice, boolean ischange, boolean isPackage) {
        //gProduct = ProductDao.getProduct(0, edFind.getText().toString());
        if (amount > 0) {
            if (gProduct.getId() > 0) {
                int selectedWarehouseId = SharedPrefUtil.getInt(SharedPrefUtil.KEY_SELECTED_DEPO_ID, 0);
                StockEntryDao.addProductToDeliverNote(gStockEntryId, gProduct.getId(), partNumber,
                        expirationDate, amount, costPrice, ischange, isPackage);
                getDeliveryDetail();
            }
        }
    }

    private void openBarcode() {
        IntentIntegrator scanIntegrator = new IntentIntegrator(NewStockEntryActivity.this);
        scanIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
        scanIntegrator.setOrientationLocked(true);
        scanIntegrator.initiateScan();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_stockentry_find:
                getProduct();
                break;

            case R.id.bt_stockentry_editdocument:
                createNewDelivery(gStockEntryId);
                break;
            case R.id.bt_stockentry_barcode:
                openBarcode();
                break;
        }
    }

    private void createSwipeListview() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                //create changeprice item
                SwipeMenuItem EditItem = new SwipeMenuItem(getApplicationContext());
                EditItem.setBackground(R.color.colorWhite);
                EditItem.setWidth(Variables.dp2px(90));
                EditItem.setIcon(R.mipmap.img_editproduct);
                EditItem.setTitle(R.string.editamount);
                EditItem.setTitleSize(12);
                EditItem.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(EditItem);


                //create printlabel item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(R.color.colorWhite);
                deleteItem.setWidth(Variables.dp2px(90));
                deleteItem.setIcon(R.mipmap.img_delete);
                deleteItem.setTitle(R.string.removefromdelivery);
                deleteItem.setTitleSize(12);
                deleteItem.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(deleteItem);

            }
        };
        lsList.setMenuCreator(creator);
    }

    @Override
    public void finish() {
        StockEntryDao.addEntryToStock(gStockEntryId);
        super.finish();
    }
}
