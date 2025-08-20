package com.eqpos.eqentry;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.eqpos.eqentry.db.ProductDao;
import com.eqpos.eqentry.db.SendDao;
import com.eqpos.eqentry.models.Product;
import com.eqpos.eqentry.tools.CaptureActivityPortrait;
import com.eqpos.eqentry.tools.Variables;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.ParseException;
import java.util.List;

import static com.eqpos.eqentry.printing.PrintLabel.printLabel;

public class EditProductActivity extends AppCompatActivity implements View.OnClickListener {
    private List<String> gTaxList;
    private List<String> gUnitList;
    private List<String> gDepositList;
    private List<String> gGroupList;
    private EditText edProductName;
    private EditText edBarcode;
    private EditText edPlu;
    private EditText edOrigin;
    private EditText edCostPrice;
    private EditText edSellPrice;
    private EditText edDescription;
    private EditText edGramaj;
    private Spinner cmbUnit;
    private Spinner cmbGroup;
    private Spinner cmbTax;
    private Spinner cmbDeposit;
    private Spinner cmbGramajUnit;
    private Button btKaydet;
    private Button btSend;
    private Button btPrint;
    private Button btBarcode;

    private Product gProduct;
    private int gProductId;
    private String barcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);
        this.setTitle(R.string.editproduct);
        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            gProductId = extra.getInt("productId");
            barcode = extra.getString("barcode","");
        } else {
            finish();
        }

        if (gProductId > 0)
            gProduct = ProductDao.getProduct(gProductId, "");

        else
            gProduct = new Product();


        edBarcode = (EditText) findViewById(R.id.ed_editproduct_barcode);
        edPlu = (EditText) findViewById(R.id.ed_editproduct_plu);
        edOrigin = (EditText) findViewById(R.id.ed_editproduct_origin);
        edProductName = (EditText) findViewById(R.id.ed_editproduct_productname);

        edCostPrice = (EditText) findViewById(R.id.ed_editproduct_costprice);
       // if(edCostPrice.toString() != "0") edCostPrice = null;
        edSellPrice = (EditText) findViewById(R.id.ed_editproduct_sellprice);


        edDescription = (EditText) findViewById(R.id.ed_editproduct_description);
        edGramaj = (EditText) findViewById(R.id.ed_editproduct_gramaj);

        cmbUnit = (Spinner) findViewById(R.id.cmb_editproduct_unitname);
        cmbTax = (Spinner) findViewById(R.id.cmb_editproduct_taxname);
        cmbGroup = (Spinner) findViewById(R.id.cmb_editproduct_groupname);
        cmbDeposit = (Spinner) findViewById(R.id.cmb_editproduct_deposite);
        cmbGramajUnit = (Spinner) findViewById(R.id.cmb_editproduct_gramaj);
        btKaydet = (Button) findViewById(R.id.bt_editproduct_save);
        btSend = (Button) findViewById(R.id.bt_editproduct_save_and_send);
        btPrint = (Button) findViewById(R.id.bt_printproduct);
        btBarcode = (Button) findViewById(R.id.bt_editproduct_barcode);



        btKaydet.setOnClickListener(this);
        btSend.setOnClickListener(this);
        btPrint.setOnClickListener(this);
        btBarcode.setOnClickListener(this);


        listFields();

        edProductName.setText(gProduct.getProductName());
        edPlu.setText(gProduct.getPlu());
        edOrigin.setText(gProduct.getOrigin());
        edBarcode.setText(gProduct.getBarcode());
        if (!barcode.equals(""))
            edBarcode.setText(barcode);

        edCostPrice.setText(Variables.doubleToStr(gProduct.getCostPrice(), 2));


        try {
            if(Variables.strToDouble((edCostPrice.getText().toString()))== 0.0) {
                edCostPrice.setText(null);
                edCostPrice.setHint(null);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (gProduct.getNewPrice().equals("")) {
            edSellPrice.setText(Variables.doubleToStr(gProduct.getPrice(), 2));
            try {
                if(Variables.strToDouble((edSellPrice.getText().toString()))== 0.0) {
                    edSellPrice.setText(null);
                    edSellPrice.setHint(null);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            edSellPrice.setText(Variables.doubleToStr(gProduct.getNewPrice(), 2));
            try {
                if(Variables.strToDouble((edSellPrice.getText().toString()))== 0.0) {
                    edSellPrice.setText(null);
                    edSellPrice.setHint(null);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        edDescription.setText(gProduct.getDescription());
        cmbUnit.setSelection(gUnitList.indexOf(gProduct.getUniteName()));
        cmbTax.setSelection(gTaxList.indexOf(gProduct.getTaxName()));
        cmbGroup.setSelection(gGroupList.indexOf(gProduct.getGroupName()));
        cmbDeposit.setSelection(gDepositList.indexOf(gProduct.getDepositeName()));
        cmbGramajUnit.setSelection(((ArrayAdapter<String>)cmbGramajUnit.getAdapter()).getPosition(gProduct.getAmountUnit()));
        edGramaj.setText(Variables.doubleToStr(gProduct.getUnitAmount(),0));
        try {
            if(Variables.strToDouble((edGramaj.getText().toString()))== 0.0) {
                edGramaj.setText(null);
                edGramaj.setHint(null);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            IntentResult scannResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            if (scannResult != null) {
                String contents = data.getStringExtra("SCAN_RESULT");
                edBarcode.setText(contents);
                edProductName.performClick();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_editproduct_save:

                boolean isOncedenKayitliUrunmu =urunOncedenKayitliUrunmu();
                if (isOncedenKayitliUrunmu) {
                    Toast.makeText(EditProductActivity.this, R.string.product_already_exists, Toast.LENGTH_SHORT).show();
                }else {
                    saveProduct();

//                    btPrint.setVisibility(View.VISIBLE);
//                    btSend.setVisibility(View.VISIBLE);
//                    btKaydet.setVisibility(View.INVISIBLE);
                }


                break;

            case R.id.bt_printproduct:
                try {
                    printLabel(gProduct.getProductName(),
                            gProduct.getPrice().toString(),
                            gProduct.getBarcode(),
                            gProduct.getUnitAmount().toString(),
                            gProduct.getAmountUnit());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //finish();
                break;

            case R.id.bt_editproduct_barcode:
                openBarcode();
                break;
            case R.id.bt_editproduct_save_and_send:
                //saveProduct();

                boolean isOncedenKayitliUrunmu2 =urunOncedenKayitliUrunmu();
                if (isOncedenKayitliUrunmu2) {
                    Toast.makeText(EditProductActivity.this, R.string.product_already_exists, Toast.LENGTH_SHORT).show();
                }else {
                    saveProduct();
                    if (ProductDao.isThereNewOrUpdatedProduct())
                        SendDao.sendProducts();
                    SendDao.sendChangedPrices();

                    if(!isFinishing()) {
                        Toast.makeText(EditProductActivity.this, R.string.update_was_done, Toast.LENGTH_SHORT).show();
                    }

                    finish();


                }


                break;
        }
    }


    private boolean urunOncedenKayitliUrunmu() {
        String barcode = edBarcode.getText().toString();
        String urunAdi = edProductName.getText().toString();

        if (edProductName.getText().toString().isEmpty() || cmbGroup.getSelectedItemPosition()<=0 ||
                cmbTax.getSelectedItemPosition()<=0 || cmbUnit.getSelectedItemPosition()<=0) {
            Toast.makeText(this, R.string.msg_dont_empty_name_group_tax, Toast.LENGTH_LONG).show();

        return false;
        }

        return ProductDao.checUrunMevcutmu(barcode, urunAdi);
    }


    private void saveProduct() {
        if (edProductName.getText().toString().isEmpty() || cmbGroup.getSelectedItemPosition()<=0 ||
                cmbTax.getSelectedItemPosition()<=0 || cmbUnit.getSelectedItemPosition()<=0) {
            Toast.makeText(this, R.string.msg_dont_empty_name_group_tax, Toast.LENGTH_LONG).show();

            return;
        }

//        btPrint.setVisibility(View.VISIBLE);
//        btSend.setVisibility(View.VISIBLE);
//        btKaydet.setVisibility(View.INVISIBLE);


        gProduct.setId(gProductId);
        gProduct.setUniteName(cmbUnit.getSelectedItem().toString());
        gProduct.setUnitId(ProductDao.getUnitId(cmbUnit.getSelectedItem().toString()));
        if (cmbDeposit.getSelectedItemPosition() >= 0) {
            gProduct.setDepositeName(cmbDeposit.getSelectedItem().toString());
            gProduct.setDepositId(ProductDao.getDepositId(cmbDeposit.getSelectedItem().toString()));
        } else {
            gProduct.setDepositeName("");
            gProduct.setDepositId(-1);
        }
        gProduct.setGroupName(cmbGroup.getSelectedItem().toString());
        gProduct.setGroupId(ProductDao.getGroupId(cmbGroup.getSelectedItem().toString()));
        gProduct.setTaxName(cmbTax.getSelectedItem().toString());
        gProduct.setTaxId(ProductDao.getTaxId(cmbTax.getSelectedItem().toString()));
        gProduct.setProductName(edProductName.getText().toString());
        gProduct.setBarcode(edBarcode.getText().toString());
        gProduct.setPlu(edPlu.getText().toString());
        gProduct.setOrigin(edOrigin.getText().toString());
        gProduct.setDescription(edDescription.getText().toString());
        gProduct.setAmountUnit(cmbGramajUnit.getSelectedItem().toString());

        if (edCostPrice.getText().toString() != "")
            try {
                gProduct.setCostPrice(Variables.strToDouble((edCostPrice.getText().toString())));
            } catch (ParseException e) {
                gProduct.setCostPrice(0.0);
                e.printStackTrace();
            }
        else
            gProduct.setCostPrice(0.0);

        if (edSellPrice.getText().toString() != "")
            try {
                String sellprice = edSellPrice.getText().toString();
                gProduct.setPrice(Variables.strToDouble(sellprice));
            } catch (ParseException e) {
                gProduct.setPrice(0.0);
                e.printStackTrace();
            }
        else
            gProduct.setPrice(0.0);

        if (edGramaj.getText().toString() != "")
            try {
                gProduct.setUnitAmount(Variables.strToDouble((edGramaj.getText().toString())));
            } catch (ParseException e) {
                gProduct.setUnitAmount(0.0);
                e.printStackTrace();
            }
        else
            gProduct.setUnitAmount(0.0);

        gProduct.setPrintLabel(1);
        gProduct.setChanged(1);

        try {
            ProductDao.saveProduct(gProduct);


            Toast.makeText(EditProductActivity.this, R.string.update_was_done, Toast.LENGTH_SHORT).show();

            Intent out = new Intent();
            out.putExtra("productname", gProduct.getProductName());
            setResult(RESULT_OK, out);

        } catch (Exception e) {
            Log.e("editproduct", e.getMessage());
        }

    }

    private void openBarcode() {
        IntentIntegrator scanIntegrator = new IntentIntegrator(EditProductActivity.this);
        scanIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
        scanIntegrator.setOrientationLocked(true);
        scanIntegrator.initiateScan();
    }

    private void listFields() {
        gGroupList = ProductDao.getGroupListForSpinner(" ");
        gTaxList = ProductDao.getTaxListForSpinner(" ");
        gDepositList = ProductDao.getDepositListForSpinner(" ");
        gUnitList = ProductDao.getUnitListForSpinner(" ");

        //"All" listeden siliniyor
//        gTaxList.remove(0);
//        gUnitList.remove(0);
//        gGroupList.set(0, "");
//        gTaxList.set(0, "");
//        gDepositList.set(0, "");

        if (gGroupList != null) {
            ArrayAdapter<String> adp = new ArrayAdapter(this, R.layout.spinner_row, gGroupList);
            cmbGroup.setAdapter(adp);
        } else {
            cmbGroup.setAdapter(null);
        }
        if (gTaxList != null) {
            ArrayAdapter<String> adp = new ArrayAdapter(this, R.layout.spinner_row, gTaxList);
            cmbTax.setAdapter(adp);
        } else {
            cmbTax.setAdapter(null);
        }
        if (gDepositList != null) {
            ArrayAdapter<String> adp = new ArrayAdapter(this, R.layout.spinner_row, gDepositList);
            cmbDeposit.setAdapter(adp);
        } else {
            cmbDeposit.setAdapter(null);
        }
        if (gUnitList != null) {
            ArrayAdapter<String> adp = new ArrayAdapter(this, R.layout.spinner_row, gUnitList);
            cmbUnit.setAdapter(adp);
        } else {
            cmbUnit.setAdapter(null);
        }
    }


}
