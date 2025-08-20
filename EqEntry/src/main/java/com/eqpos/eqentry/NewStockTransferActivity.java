package com.eqpos.eqentry;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.eqpos.eqentry.db.BarcodeDao;
import com.eqpos.eqentry.db.InvoiceDao;
import com.eqpos.eqentry.db.ProductDao;
import com.eqpos.eqentry.db.StockTransferDao;
import com.eqpos.eqentry.models.BarcodeSettings;
import com.eqpos.eqentry.models.Product;
import com.eqpos.eqentry.models.StockTransfer;
import com.eqpos.eqentry.tools.CaptureActivityPortrait;
import com.eqpos.eqentry.tools.Variables;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.HashMap;

public class NewStockTransferActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btBarcode;
    private Button btFind;
    private Spinner cmbSelectWarehouse;
    private EditText edFind;
    private EditText edDocumentNumber;
    private SwipeMenuListView lsList;
    private TextView lblTotalAmount;
    private ArrayList<HashMap<String, String>> gList;
    private ArrayList<HashMap<String, String>> gWareList;
    private BarcodeSettings barcodeSettings;

    private StockTransfer gTransfer;
    private int lastPosition=0;
    private long gStockTransferId = 0;
    private int gTargetWareId=0;
    private Product gProduct;

    private int _GETAMOUNT = 7000;
    private int _CHANGEAMOUNT = 9000;
    private int _SELECTPRODUCT = 8000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_stock_transfer);
        this.setTitle(R.string.newstocktransfer);

        gStockTransferId = 0;
        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            gStockTransferId = extra.getInt("transferid");
        }

        btBarcode = (Button) findViewById(R.id.bt_stocktransfer_barcode);
        btFind = (Button) findViewById(R.id.bt_stocktransfer_find);
        cmbSelectWarehouse = (Spinner) findViewById(R.id.cmb_stocktransfer_target);
        edFind = (EditText) findViewById(R.id.ed_stocktransfer_find);
        edDocumentNumber = (EditText) findViewById(R.id.ed_stocktransfer_documentnumber);
        lsList = (SwipeMenuListView) findViewById(R.id.ls_stocktransfer_list);
        lblTotalAmount = (TextView) findViewById(R.id.lbl_stocktransfer_total_amount);
        edDocumentNumber.setText(InvoiceDao.getRandomInvoiceNumber());

        barcodeSettings = BarcodeDao.getBarcodeSettings();

        btBarcode.setOnClickListener(this);
        btFind.setOnClickListener(this);
        cmbSelectWarehouse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StockTransferDao.changeWareId(gStockTransferId, Long.parseLong( gWareList.get(cmbSelectWarehouse.getSelectedItemPosition()).get("id")));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        listWarehouses();
        if (gStockTransferId > 0) {
            getStockTransfer();
            getStockTransferDetail();
        } else {
            createNewStockTransfer();
        }

        createSwipeListview();


        lsList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                int lId = Integer.parseInt(gList.get(position).get("id"));

                lastPosition = position;
                switch (index) {
                    case 0:
                        //changeprice
                        editProduct(lId);
                        break;
                    case 1:
                        //Transfer
                        StockTransferDao.removeProductFromTransfer(lId);
                        getStockTransferDetail();
                        break;

                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
    }

    private void fillFields() {
        if (gTransfer.getId() > 0) {
            gStockTransferId = gTransfer.getId();
            edDocumentNumber.setText(gTransfer.getNumber());
            for (int i = 0; i <= gWareList.size()-1; i++) {
                int lwid = Integer.parseInt( gWareList.get(i).get("id") );
                if (lwid == gTransfer.getTargetWarehouseId()) {
                    cmbSelectWarehouse.setSelection(i);
                    break;
                }
            }
        } else {
            gStockTransferId = 0;
            edDocumentNumber.setText("");
            cmbSelectWarehouse.setSelection(-1);

            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == _SELECTPRODUCT) {
            int lProductId;
            lProductId = data.getIntExtra("id", 0);

            gProduct = ProductDao.getProduct(lProductId,"");
            Intent intent = new Intent(this, StockEntryDetailAmountActivity.class);
            intent.putExtra("isTransfer", true);
            intent.putExtra("productId", gProduct.getId());
            intent.putExtra("productname", gProduct.getProductName());
            intent.putExtra("unitename", gProduct.getUniteName());
            startActivityForResult(intent, _GETAMOUNT);

        } else if (resultCode == RESULT_OK && requestCode == _GETAMOUNT) {
            Double lAmount;

            try {
                lAmount = data.getDoubleExtra("amount", 0);
            } catch (Exception e) {
                lAmount = 0.0;
                Log.e("get amount", e.getMessage());
            }

            boolean isPackage;
            if (data.getBooleanExtra("ispackage", false)) isPackage = true;
            else isPackage = false;

            getProduct(lAmount, false, isPackage);

        }else if (resultCode == RESULT_OK && requestCode == _CHANGEAMOUNT) {

            Double lAmount;
            try {
                lAmount = data.getDoubleExtra("amount", 0);
            } catch (Exception e) {
                lAmount = 0.0;
                Log.e("get amount", e.getMessage());
            }

            getProduct(lAmount, true, false);

        } else if (resultCode == RESULT_OK) {
            IntentResult scannResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            if (scannResult != null) {
                String contents = data.getStringExtra("SCAN_RESULT");
                edFind.setText(contents);
                getProduct();
            }
        }

    }

    private void listWarehouses() {
        gWareList = StockTransferDao.getWarehouseList();
        ArrayList<String> lWareList = new ArrayList<String>();

        for (int i=0; i<= gWareList.size()-1; i++) {
            lWareList.add(gWareList.get(i).get("warehousename"));
        }

        if (gWareList != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, lWareList);
            cmbSelectWarehouse.setAdapter(adapter);
            //cmbSelectWarehouse.setSelection(0);
        }
    }

    private void getStockTransfer() {
        gTransfer = StockTransferDao.getStockTransfer(gStockTransferId);
        fillFields();
    }

    private void createNewStockTransfer() {
        gStockTransferId = StockTransferDao.createNewStockTransfer(0, edDocumentNumber.getText().toString(), gTargetWareId);
        gTransfer = new StockTransfer();
        gTransfer.setNumber(edDocumentNumber.getText().toString());
        gTransfer.setTargetWarehouseId(gTargetWareId);
        if (cmbSelectWarehouse.getSelectedItemPosition() >= 0) {
            gTransfer.setTargetWarehouseName(gWareList.get(cmbSelectWarehouse.getSelectedItemPosition()).get("warehousename"));
        }
        gTransfer.setId(gStockTransferId);

        fillFields();
    }

    private void getStockTransferDetail() {
        gList = StockTransferDao.getStockTransferDetail(gStockTransferId);

        if (gList != null) {
            SimpleAdapter adp = new SimpleAdapter(this, gList, R.layout.stock_entry_detail_row,
                    new String[]{"productname", "", "", "packageamount", "amount", "unitename"},
                    new int[]{R.id.lbl_stockentry_detail_row_productname, R.id.lbl_stockentry_detail_row_packageamount,R.id.lbl_stockentry_detail_row_amount,
                            R.id.lbl_stockentry_detail_row_unitename, R.id.lbl_stockentry_detail_row_costprice,
                            R.id.lbl_stockentry_detail_row_total});
            lsList.setAdapter(adp);
            lsList.setSelection(lastPosition);
        }
    }


    private void getProduct(Double amount, boolean ischange, boolean isPackage) {
        //gProduct = ProductDao.getProduct(0, edFind.getText().toString());
        if (amount > 0) {
            if (gProduct.getId() > 0) {
                StockTransferDao.addProductToTransfer(gStockTransferId, gProduct.getId(), amount, ischange, isPackage);
                getStockTransferDetail();
            }
        }
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
            if (!StockTransferDao.addProductAmount(gStockTransferId, gProduct.getId(), lAmount, (isQuantity && barcodeValue.length() > 0))) {

                Intent intent = new Intent(this, StockEntryDetailAmountActivity.class);
                intent.putExtra("isTransfer", true);
                intent.putExtra("productId", gProduct.getId());
                intent.putExtra("productname", gProduct.getProductName());
                intent.putExtra("unitename", gProduct.getUniteName());
                startActivityForResult(intent, _GETAMOUNT);
            } else {
                getStockTransferDetail();
            }
        } else if (lList.size() > 1) {
            Intent intent = new Intent(this, SelectProductActivity.class);
            intent.putExtra("search", edFind.getText().toString());
            startActivityForResult(intent, _SELECTPRODUCT);
        }

        edFind.setText("");
    }

    private void openBarcode() {
        IntentIntegrator scanIntegrator = new IntentIntegrator(NewStockTransferActivity.this);
        scanIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
        scanIntegrator.setOrientationLocked(true);
        scanIntegrator.initiateScan();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_stocktransfer_find:
                getProduct();
                break;

            case R.id.bt_stocktransfer_barcode:
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


                //create transfer item
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


    private void editProduct(int rowId) {
        gProduct = ProductDao.getProduct(Integer.parseInt(gList.get(lastPosition).get("productid")), "");
        Double lAmount = 0.0;
        try {
            lAmount = Variables.strToDouble(gList.get(lastPosition).get("amount"));
        } catch (Exception e) {
            lAmount = 1.0;
        }
        if (gProduct.getId() > 0) {
            Intent intent = new Intent(this, StockEntryDetailAmountActivity.class);
            intent.putExtra("isTransfer", true);
            intent.putExtra("amount", lAmount);
            intent.putExtra("productname", gProduct.getProductName());
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
                StockTransferDao.addProductToTransfer(gStockTransferId, gProduct.getId(), amount, ischange, isPackage);
                getStockTransferDetail();
            }
        }
    }

    @Override
    public void finish() {
        if (gList.size() == 0) {
            StockTransferDao.removeTransfer(gStockTransferId);
        } else {
            StockTransferDao.changeWareId(gStockTransferId, Long.parseLong( gWareList.get(cmbSelectWarehouse.getSelectedItemPosition()).get("id")));
        }
        super.finish();
    }
}
