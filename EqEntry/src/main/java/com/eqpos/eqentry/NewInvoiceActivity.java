package com.eqpos.eqentry;

import android.app.DatePickerDialog;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.eqpos.eqentry.DB.BarcodeDao;
import com.eqpos.eqentry.DB.InvoiceDao;
import com.eqpos.eqentry.DB.ProductDao;
import com.eqpos.eqentry.Models.BarcodeSettings;
import com.eqpos.eqentry.Models.Product;
import com.eqpos.eqentry.tools.CaptureActivityPortrait;
import com.eqpos.eqentry.tools.Variables;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class NewInvoiceActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btCustomer;
    private Button btSearch;
    private Button btOrigin;
    private Button btBarcode;
    private TextView lblCustomer;
    private TextView lblTotal;
    private TextView lblDiscount;
    private TextView lblSubTotal;
    private TextView lblTaxAmount;
    private EditText edSearch;
    private EditText edInvoiceNumber;
    private EditText edInvoiceDate;
    private TextView lblTotalAmount;
    private SwipeMenuListView lvList;
    private Switch swIncludeTax;

    private ArrayList<HashMap<String, String>> gList;
    private int gLastPosition = 0;
    private int gCustomerId = 0;
    private long gInvoiceId = 0;
    private double gInvoiceOldAmount=0;
    Product gProduct;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private Calendar myCalendar;

    private int _SELECTCUSTOMER = 6000;
    private int _GETAMOUNT = 7000;
    private int _CHANGEAMOUNT = 9000;
    private int _SELECTPRODUCT = 8000;
    private BarcodeSettings barcodeSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_invoice);
        this.setTitle(R.string.newinvoice);

        edInvoiceDate = (EditText) findViewById(R.id.ed_newinvoice_date);
        edInvoiceNumber = (EditText) findViewById(R.id.ed_newinvoice_number);
        btSearch = (Button) findViewById(R.id.bt_newinvoice_search);
        btOrigin = (Button) findViewById(R.id.bt_newinvoice_origin);
        btBarcode = (Button) findViewById(R.id.bt_newinvoice_barcode);
        btCustomer = (Button) findViewById(R.id.bt_newinvoice_select_customer);
        edSearch = (EditText) findViewById(R.id.ed_newinvoice_search);
        lblCustomer = (TextView) findViewById(R.id.lbl_newinvoice_customer);
        lblTotal = (TextView) findViewById(R.id.lbl_newinvoice_total);
        lblDiscount = (TextView) findViewById(R.id.lbl_newinvoice_discount);
        lblSubTotal = (TextView) findViewById(R.id.lbl_newinvoice_subtotal);
        lblTaxAmount = (TextView) findViewById(R.id.lbl_newinvoice_taxamount);
        lvList = (SwipeMenuListView) findViewById(R.id.lv_newinvoice_list);
        lblTotalAmount = (TextView) findViewById(R.id.lbl_newinvoice_total_amount);
        swIncludeTax = (Switch) findViewById(R.id.sw_newinvoice_includetax);

        barcodeSettings = BarcodeDao.getBarcodeSettings();

        myCalendar = Calendar.getInstance();
        updateEdit();

        Bundle extra = getIntent().getExtras();

        if (extra != null) {

            gInvoiceId = extra.getLong("invoiceid", 0);
            gInvoiceOldAmount = extra.getDouble("subtotal", 0);

            if (gInvoiceId == 0) {
                String invoiceNumber = InvoiceDao.getRandomInvoiceNumber();
                edInvoiceNumber.setText(invoiceNumber);
                gInvoiceId = InvoiceDao.createInvoice(invoiceNumber, edInvoiceDate.getText().toString(), 0, 0, 0, swIncludeTax.isChecked());
            } else {
                edInvoiceNumber.setText(extra.getString("invoicenumber", ""));
                edInvoiceDate.setText(extra.getString("invoicedate", ""));
            }
            gCustomerId = extra.getInt("customerid", 0);
            lblCustomer.setText(extra.getString("customername", ""));

            boolean isTaxInclude = false;
            if (extra.containsKey("includetax"))
                isTaxInclude = extra.getInt("includetax") == 1;
            swIncludeTax.setChecked(isTaxInclude);
        }

        btBarcode.setOnClickListener(this);
        btSearch.setOnClickListener(this);
        btOrigin.setOnClickListener(this);
        btCustomer.setOnClickListener(this);

        getProductList();
        createSwipeListview();

        lvList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                int lProductId = Integer.parseInt(gList.get(position).get("productid"));
                gLastPosition = position;
                switch (index) {
                    case 0:
                        InvoiceDao.deleteProductFromInvoice(gInvoiceId, lProductId);
                        InvoiceDao.setAktueDetaylariInInvoice(gInvoiceId);  // 30.07.2024 Erkan ben ekledim
                        getProductList();
                        break;
                    case 1://edit

                        Intent intent = new Intent(menu.getContext(), NewInvoiceAmountActivity.class);
                        intent.putExtra("productid", Integer.parseInt(gList.get(position).get("productid")));
                        intent.putExtra("productname", gList.get(position).get("productname"));
                        try {
                            intent.putExtra("unitprice", Variables.strToDouble(gList.get(position).get("unitprice")));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        try {
                            intent.putExtra("amount", Variables.strToDouble(gList.get(position).get("amount")));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        try {
                            intent.putExtra("taxrate", Variables.strToDouble(gList.get(position).get("taxrate")));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        try {
                            intent.putExtra("discount", Variables.strToDouble(gList.get(position).get("discount1")));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        intent.putExtra("istaxinclude", swIncludeTax.isChecked());
                        intent.putExtra("rowid", Integer.parseInt( gList.get(position).get("id")));
                        startActivityForResult(intent, _CHANGEAMOUNT);
                        break;

                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        edInvoiceDate.setOnClickListener(this);
        dateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateEdit();
            }
        };

        edSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    edSearch.clearFocus();
                    btSearch.performClick();
                    edSearch.requestFocus();
                    return true;
                }
                return false;
            }
        });

        lvList.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                edSearch.requestFocus();
                edSearch.performClick();
            }
        });

        swIncludeTax.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                InvoiceDao.changeTaxStatus(gInvoiceId, isChecked);
                getInvoiceTotals();
                getProductList();

                InvoiceDao.updateInvoiceAuto(gInvoiceId, gInvoiceOldAmount, gCustomerId, edInvoiceNumber.getText().toString(), edInvoiceDate.getText().toString());
                gInvoiceOldAmount = InvoiceDao.getInvoiceTotal(gInvoiceId);
            }
        });

        edSearch.requestFocus();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == _SELECTCUSTOMER) {
            //Burada seçilen müşterinin bilgileri yazılacak
            gCustomerId = data.getIntExtra("id", 0);
            lblCustomer.setText(data.getStringExtra("customername"));
            InvoiceDao.updateInvoiceAuto(gInvoiceId,gInvoiceOldAmount, gCustomerId, edInvoiceNumber.getText().toString(), edInvoiceDate.getText().toString());
        } else if (resultCode == RESULT_OK && requestCode == _SELECTPRODUCT) {
            //Burada Eklenecek ürün için Birim Fiyat, Miktar, İndirim vs istenecek
            int lProductId;
            lProductId = data.getIntExtra("id", 0);

            // 30.07.2024 Erkan
            //gProduct = ProductDao.getProduct(lProductId, "");
            int priceOrder = data.getIntExtra("priceorder", 1);
            gProduct = ProductDao.getProductErkan(lProductId, "", priceOrder);

            Intent intent = new Intent(this, NewInvoiceAmountActivity.class);
            intent.putExtra("productid", gProduct.getId());
            intent.putExtra("productname", gProduct.getProductName());
            if (!swIncludeTax.isChecked()) {
                if (gProduct.getNewPrice() > 0) {
                    intent.putExtra("unitprice", Variables.calcNet(gProduct.getPrice(), gProduct.getTaxOut()));
                } else {
                    intent.putExtra("unitprice", Variables.calcNet(gProduct.getNewPrice(), gProduct.getTaxOut()));
                }
            } else {
                if (gProduct.getNewPrice() > 0) {
                    intent.putExtra("unitprice", gProduct.getPrice());
                } else {
                    intent.putExtra("unitprice", gProduct.getNewPrice());
                }
            }
            intent.putExtra("amount", 0.0);
            intent.putExtra("discount", 0.0);
            intent.putExtra("taxrate", gProduct.getTaxOut());
            intent.putExtra("istaxinclude", swIncludeTax.isChecked());
            startActivityForResult(intent, _GETAMOUNT);
            edSearch.requestFocus();
            edSearch.performClick();

        } else if (resultCode == RESULT_OK && requestCode == _GETAMOUNT) {

            // Burada alınan birimfiyat, indirim, miktar vs kaydedilecek
            gLastPosition = lvList.getSelectedItemPosition();

            int lProductId = 0;
            double lAmount, lUnitPrice, lDiscount, lTax, lTaxRate = 0.0;

            lAmount = data.getDoubleExtra("amount", 0.0);
            lUnitPrice = data.getDoubleExtra("unitprice", 0.0);
            lDiscount = data.getDoubleExtra("discount", 0.0);
            lProductId = data.getIntExtra("productid", 0);
            lTax = data.getDoubleExtra("tax", 0.0);
            lTaxRate = data.getDoubleExtra("taxrate", 0.0);
            boolean isPackage = data.getBooleanExtra("ispackage", false);
            int rowId = data.getIntExtra("rowid", 0);

            if (lAmount > 0) {
                double productAmount =
                     InvoiceDao.addProductToInvoice(gInvoiceId, lProductId, lUnitPrice, lAmount, lDiscount, 0.0, 0.0, 0.0, lTaxRate, lTax,
                        true, isPackage, swIncludeTax.isChecked(), rowId);

                InvoiceDao.updateInvoiceAuto(gInvoiceId, gInvoiceOldAmount, gCustomerId, edInvoiceNumber.getText().toString(), edInvoiceDate.getText().toString());
                gInvoiceOldAmount += productAmount;
                getProductList();
            }
            edSearch.requestFocus();
            edSearch.performClick();

        } else if (resultCode == RESULT_OK && requestCode == _CHANGEAMOUNT) {

            // Burada alınan birimfiyat, indirim, miktar vs kaydedilecek
            gLastPosition = lvList.getSelectedItemPosition();

            int lProductId = 0;
            double lAmount, lUnitPrice, lDiscount, lTax, lTaxRate = 0.0;

            lAmount = data.getDoubleExtra("amount", 0.0);     // adet
            lUnitPrice = data.getDoubleExtra("unitprice", 0.0);  // birim fiyari
            lDiscount = data.getDoubleExtra("discount", 0.0);   // indirim
            lProductId = data.getIntExtra("productid", 0);
            lTax = data.getDoubleExtra("tax", 0.0);
            lTaxRate = data.getDoubleExtra("taxrate", 0.0);
            int rowId = data.getIntExtra("rowid", 0);

            if (lAmount > 0) {
                double subTotal = InvoiceDao.addProductToInvoice(gInvoiceId, lProductId, lUnitPrice, lAmount,
                        lDiscount, 0.0, 0.0, 0.0, lTaxRate, lTax,
                        false, false, swIncludeTax.isChecked(), rowId);

//                InvoiceDao.updateInvoiceAuto2(gInvoiceId, gInvoiceOldAmount, gCustomerId,
//                        edInvoiceNumber.getText().toString(), edInvoiceDate.getText().toString());
                InvoiceDao.updateInvoiceAuto2(gInvoiceId, gInvoiceOldAmount, gCustomerId,
                        edInvoiceNumber.getText().toString(), edInvoiceDate.getText().toString());

                getProductList();
            }
            edSearch.requestFocus();
            edSearch.performClick();

        } else if (resultCode == RESULT_OK) {
            IntentResult scannResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            if (scannResult != null) {
                String contents = data.getStringExtra("SCAN_RESULT");
                edSearch.setText(contents);
                getProduct(0);
                edSearch.requestFocus();
                edSearch.performClick();
            }
        }

    }

    private void setInvoiceDate() {
        new DatePickerDialog(NewInvoiceActivity.this, dateSetListener, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void updateEdit() {
        String myFormat = "yyyy.MM.dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        edInvoiceDate.setText(sdf.format(myCalendar.getTime()));
    }

    private void getProductList() {
        gList = null;

        gList = InvoiceDao.getInvoiceDetail(gInvoiceId);
        SimpleAdapter adp = new SimpleAdapter(this, gList, R.layout.new_invoice_row,
                new String[]{"productname", "packageamount", "amount", "unitename", "unitprice", "total", "discount1",
                        "subtotal"},
                new int[]{R.id.lbl_newinvoice_row_productname, R.id.lbl_newinvoice_row_packageamount, R.id.lbl_newinvoice_row_amount,
                        R.id.lbl_newinvoice_row_unit, R.id.lbl_newinvoice_row_unitprice,
                        R.id.lbl_newinvoice_row_total, R.id.lbl_newinvoice_row_discount,
                        R.id.lbl_newinvoice_row_subtotal});
        try {
            lvList.setAdapter(adp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        getInvoiceTotals();
        edSearch.requestFocus();
        edSearch.performClick();
    }


    private void getProduct(int filterType) {
        String barcode = edSearch.getText().toString().trim().toUpperCase();
        String plu = barcode;
        String barcodeValue = "";
        String lPre = "";
        int lFilterType = filterType;
        if (plu.length() > 2)
            lPre = plu.substring(0, 2);
        boolean isQuantity = true;
        if (plu.length() >= 12 && lFilterType==0) {
            if (barcodeSettings.getQ_Prefix().contains(lPre) && barcodeSettings.getQ_Prefix().length() > 0) {
                int lStart = 2;
                int lEnd = lStart + barcodeSettings.getQ_PLU();
                plu = barcode.substring(lStart, lEnd);

                lStart = lEnd;
                lEnd = lStart + barcodeSettings.getQ_Value();

                barcodeValue = barcode.substring(lStart, lEnd);
                lFilterType = 2;
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

        if (lList.size() == 1) {
            edSearch.clearFocus();
            gProduct = ProductDao.getProduct(Integer.parseInt(lList.get(0).get("id")), "");

            double lAmount, lUnitPrice, lDiscount, lTaxRate = 0.0;

            try {
                lUnitPrice = Variables.strToDouble(lList.get(0).get("sellprice"));
            } catch (Exception e) {
                lUnitPrice = 0.0;
            }
            try {
                lTaxRate = Double.valueOf(lList.get(0).get("taxout"));
            } catch (Exception e) {
                lTaxRate = 0.0;
            }
            lAmount = 0.0;
            if (barcodeValue.length() > 0 && isQuantity) {
                try {
                    lAmount = Variables.strToDouble(barcodeValue) / 1000.0;
                } catch (Exception e) {
                    lAmount = 0.0;
                }
            } else if (barcodeValue.length() > 0 && !isQuantity) {
                try {
                    lAmount = 1;
                    lUnitPrice = Variables.strToDouble(barcodeValue) / 100.0;
                } catch (Exception e) {
                    lUnitPrice = 0.0;
                }
            }
            lDiscount = 0.0;

            /*
                Eger urun zaten eklenmişse miktarını değiştirecek sadece
            */

            if (!InvoiceDao.addProductAmount(gInvoiceId, gProduct.getId(), lAmount, (isQuantity && barcodeValue.length() > 0), swIncludeTax.isChecked())) {

                //Ürün zaten listede var ise miktarını artırmak gerekiyor

                Intent intent = new Intent(this, NewInvoiceAmountActivity.class);
                intent.putExtra("productid", gProduct.getId());
                intent.putExtra("productname", gProduct.getProductName());
                if (!swIncludeTax.isChecked())
                    intent.putExtra("unitprice", Variables.calcNet(lUnitPrice, lTaxRate));
                else
                    intent.putExtra("unitprice", lUnitPrice);

                intent.putExtra("taxrate", lTaxRate);
                intent.putExtra("discount", lDiscount);
                intent.putExtra("amount", lAmount);
                intent.putExtra("ispackage", (isQuantity && barcodeValue.length() > 0));
                intent.putExtra("istaxinclude", swIncludeTax.isChecked());

                startActivityForResult(intent, _GETAMOUNT);
            } else {
                edSearch.clearFocus();
                InvoiceDao.updateInvoiceAuto(gInvoiceId, gInvoiceOldAmount, gCustomerId, edInvoiceNumber.getText().toString(), edInvoiceDate.getText().toString());
                getProductList();
                edSearch.requestFocus();
                edSearch.performClick();
            }

        } else if (lList.size() > 1) {
            Intent intent = new Intent(this, SelectProductActivity.class);
            intent.putExtra("search", edSearch.getText().toString().trim().toUpperCase() );
            intent.putExtra("filtertype",lFilterType);
            startActivityForResult(intent, _SELECTPRODUCT);
        }

        edSearch.clearFocus();
        edSearch.setText("");
        edSearch.requestFocus();
        edSearch.performClick();
    }

    private void openBarcode() {
        IntentIntegrator scanIntegrator = new IntentIntegrator(NewInvoiceActivity.this);
        scanIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
        scanIntegrator.setOrientationLocked(true);
        scanIntegrator.initiateScan();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_newinvoice_search:
                if (musteriIsmiBosmu()){
                    Toast.makeText(this, R.string._select_customer, Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    edSearch.clearFocus();
                    getProduct(0);
                    edSearch.requestFocus();
                }
                break;

            case R.id.bt_newinvoice_barcode:
                openBarcode();
                break;

            case R.id.bt_newinvoice_origin:
                edSearch.clearFocus();
                getProduct(4);
                edSearch.requestFocus();
                break;

            case R.id.bt_newinvoice_select_customer:
                Intent intent = new Intent(this, CustomersActivity.class);
                intent.putExtra("invoiceid", gInvoiceId);
                startActivityForResult(intent, _SELECTCUSTOMER);
                break;

            case R.id.ed_newinvoice_date:
                setInvoiceDate();
                break;
        }
    }

    private boolean musteriIsmiBosmu() {
        return lblCustomer.getText().toString().isEmpty();
    }


    @Override
    protected void onDestroy() {

        if (gList.isEmpty()) {
            InvoiceDao.deleteInvoice(gInvoiceId, gCustomerId);
        } else {
            if (edInvoiceNumber.getText().toString().isEmpty()) {
                edInvoiceNumber.setText(InvoiceDao.getRandomInvoiceNumber());
            }
            gInvoiceOldAmount = InvoiceDao.getInvoiceTotal(gInvoiceId);

            // InvoiceDao.updateInvoiceAuto(gInvoiceId, gInvoiceOldAmount, gCustomerId, edInvoiceNumber.getText().toString(), edInvoiceDate.getText().toString());
        }
        setResult(RESULT_OK);
        super.onDestroy();
    }


    private void getTotalAmount() {
        String lAmount = InvoiceDao.getTotalAmount(gInvoiceId);

        lblTotalAmount.setText(lAmount);
    }

    private void getInvoiceTotals() {
        HashMap<String, Double> map = InvoiceDao.getInvoiceTotals(gInvoiceId);


        lblTotal.setText(Variables.doubleToStr(map.get("total"), 2));
        lblDiscount.setText(Variables.doubleToStr(map.get("discount"), 2));
        lblSubTotal.setText(Variables.doubleToStr(map.get("subtotal"), 2));
        lblTaxAmount.setText(Variables.doubleToStr(map.get("taxamount"), 2));

        String lAmount = InvoiceDao.getTotalAmount(gInvoiceId);
        lblTotalAmount.setText(lAmount);
    }

    private void createSwipeListview() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                SwipeMenuItem deleteProduct = new SwipeMenuItem(getApplicationContext());
                deleteProduct.setBackground(R.color.colorWhite);
                deleteProduct.setWidth(Variables.dp2px(90));
                deleteProduct.setIcon(R.mipmap.img_delete);
                deleteProduct.setTitle(R.string.delete);
                deleteProduct.setTitleSize(12);
                deleteProduct.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(deleteProduct);


                SwipeMenuItem changePrice = new SwipeMenuItem(getApplicationContext());
                changePrice.setBackground(R.color.colorWhite);
                changePrice.setWidth(Variables.dp2px(90));
                changePrice.setIcon(R.mipmap.img_changeprice);
                changePrice.setTitle(R.string.editamount);
                changePrice.setTitleSize(12);
                changePrice.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(changePrice);
            }
        };
        lvList.setMenuCreator(creator);
    }
}
