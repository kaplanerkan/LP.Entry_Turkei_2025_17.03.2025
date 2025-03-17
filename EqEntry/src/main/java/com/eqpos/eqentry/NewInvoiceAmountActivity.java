package com.eqpos.eqentry;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.eqpos.eqentry.tools.Variables;

import java.text.ParseException;

public class NewInvoiceAmountActivity extends AppCompatActivity implements TextWatcher {

    private EditText edQuantity;
    private EditText edUnitPrice;
    private EditText edDiscount;
    private EditText edDiscountRate;
    private EditText edTotal;
    private EditText edSubTotal;
    private EditText edTaxRate;
    private EditText edTax;
    private TextView lblProductName;
    private Button btOk;

    private double gUnitPrice = 0.0;
    private double gQuantity = 1.0;
    private double gDiscount = 0.0;
    private int gDiscountRate = 0;
    private double gTaxRate = 0.0;
    private double gTaxAmount = 0.0;
    private int gProductId = 0;
    private boolean isPackage = false;
    private boolean isTaxInclude = false;
    private String gProductName = "";
    private int gRowId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_invoice_amount);
        this.setTitle(R.string.amount);


        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            lblProductName = (TextView) findViewById(R.id.lbl_newinvoice_get_row_productname);
            gProductName = extra.getString("productname");
            lblProductName.setText(gProductName);
            gProductId = extra.getInt("productid");

            gUnitPrice = extra.getDouble("unitprice");
            try {
                gQuantity = extra.getDouble("amount");
            } catch (Exception e) {
                Log.e("Convert Error", e.getMessage());
                gQuantity = 0.0;
            }
            try {
                gDiscount = extra.getDouble("discount");
            } catch (Exception e) {
                Log.e("Convert Error", e.getMessage());
                gDiscount = 0.0;
            }
            try {
                gTaxRate = extra.getDouble("taxrate");
            } catch (Exception e) {
                Log.e("Convert Error", e.getMessage());
                gTaxRate = 0.0;
            }
            isPackage = false;
            if (extra.containsKey("ispackage")) {
                isPackage = extra.getBoolean("ispackage");
            }
            isTaxInclude = false;
            if (extra.containsKey("istaxinclude")) {
                isTaxInclude = extra.getBoolean("istaxinclude");
            }

            if (extra.containsKey("rowid")) {
                gRowId = extra.getInt("rowid");
            } else {
                gRowId = 0;
            }

        }

        edQuantity = (EditText) findViewById(R.id.ed_newinvoice_get_row_amount);
        edUnitPrice = (EditText) findViewById(R.id.ed_newinvoice_get_row_unitprice);
        edDiscount = (EditText) findViewById(R.id.ed_newinvoice_get_row_discount);
        edDiscountRate = (EditText) findViewById(R.id.ed_newinvoice_get_row_discount_rate);
        edTotal = (EditText) findViewById(R.id.ed_newinvoice_get_row_total);
        edSubTotal = (EditText) findViewById(R.id.ed_newinvoice_get_row_subtotal);
        edTaxRate = (EditText) findViewById(R.id.ed_newinvoice_get_row_taxrate);
        edTax = (EditText) findViewById(R.id.ed_newinvoice_get_row_tax);

        btOk = (Button) findViewById(R.id.bt_newinvoice_get_row_ok);

        edTaxRate.setText(Variables.doubleToStr(gTaxRate, 0));
        if (gQuantity > 0)
            edQuantity.setText(Variables.doubleToStr(gQuantity, 0));

        edUnitPrice.setText(Variables.doubleToStr(gUnitPrice, 2));

        if (gDiscount > 0) {
            edDiscount.setText(Variables.doubleToStr(gDiscount, 2));
            edDiscountRate.setText(Integer.toString((int)Variables.roundTo(gDiscount / (gQuantity * gUnitPrice)*100, 0)));
        }
        edQuantity.addTextChangedListener(this);
        edUnitPrice.addTextChangedListener(this);
        edDiscount.addTextChangedListener(this);
        edDiscountRate.addTextChangedListener(this);
        edTotal.addTextChangedListener(this);
        edTaxRate.addTextChangedListener(this);

        //Sub total ve Tax hesaplanması için buraya alındı
        edSubTotal.setText(Variables.doubleToStr((gQuantity * gUnitPrice) - gDiscount, 2));
        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAndFinishValues();
            }
        });

        calcTotals();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        try {
            gQuantity = Variables.strToDouble(edQuantity.getText().toString());
        } catch (ParseException ex) {
            gQuantity = 0.0;
        }

        if (getCurrentFocus() == edDiscountRate){
            edDiscount.removeTextChangedListener(this);

            try {
                gDiscountRate = (int)Variables.strToDouble(edDiscountRate.getText().toString());
                gDiscount = gQuantity*gUnitPrice / 100 * gDiscountRate;
                edDiscount.setText(Variables.doubleToStr(gDiscount, 2));
                calcTotals();
                edDiscountRate.setSelection(edDiscountRate.getText().length());
            } catch (ParseException e) {
                edDiscount.setText("0.0");
            }
        } else {
           calcTotals();
        }
    }

    private void calcTotals() {
        double  lUnitPrice, lTotal, lEditTotal;

        edQuantity.removeTextChangedListener(this);
        edUnitPrice.removeTextChangedListener(this);
        edDiscount.removeTextChangedListener(this);
        edDiscountRate.removeTextChangedListener(this);
        edTotal.removeTextChangedListener(this);
        edTaxRate.removeTextChangedListener(this);

        try {
            gTaxRate = Variables.strToDouble(edTaxRate.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            lUnitPrice = Variables.strToDouble(edUnitPrice.getText().toString());
        } catch (ParseException ex) {
            lUnitPrice = 0.0;
        }

        try {
            gDiscount = Variables.strToDouble(edDiscount.getText().toString());
            gDiscountRate = (int)((gDiscount * 100) / (gQuantity * lUnitPrice));
        } catch (ParseException ex) {
            gDiscount = 0.0;
            gDiscountRate = 0;
        }

        lTotal = Variables.roundTo((gQuantity * lUnitPrice), 2);

        if (gDiscountRate > 100 || gDiscount > lTotal) {
            gDiscount = 0.0;
            gDiscountRate = 0;
            edDiscount.setText("0");
        }

        edDiscountRate.setText(Integer.toString((int)Variables.roundTo(gDiscountRate, 0)));

        lTotal = Variables.roundTo(lTotal - gDiscount, 2);

        gTaxAmount = lTotal * (gTaxRate / 100);

        edTax.setText(Variables.doubleToStr(gTaxAmount, 2));

        if (isTaxInclude) {
            edSubTotal.setText(Variables.doubleToStr(lTotal - gTaxAmount, 2));
            edTotal.setText(Variables.doubleToStr(lTotal, 2));
        } else {
            edSubTotal.setText(Variables.doubleToStr(lTotal, 2));
            edTotal.setText(Variables.doubleToStr(lTotal + gTaxAmount, 2));
        }

        edQuantity.addTextChangedListener(this);
        edUnitPrice.addTextChangedListener(this);
        edDiscount.addTextChangedListener(this);
        edDiscountRate.addTextChangedListener(this);
        edTotal.addTextChangedListener(this);
        edTaxRate.addTextChangedListener(this);
    }

    private void setAndFinishValues() {

        Double lQuantity = 0.0;
        Double lUnitPrice = 1.0;
        Double lDiscount = 0.0;
        Double lTotal = 0.0;
        Double lTax = 0.0;
        Double lTaxRate = 0.0;
        try {
            lQuantity = Variables.strToDouble(edQuantity.getText().toString());
        } catch (ParseException e) {
            lQuantity = 0.0;
            Log.e("Amount Error", e.getMessage());
        }
        try {
            lUnitPrice = Variables.strToDouble(edUnitPrice.getText().toString());
        } catch (ParseException e) {
            lUnitPrice = 1.0;
            Log.e("Unit Price Error", e.getMessage());
        }

        try {
            lDiscount = Variables.strToDouble(edDiscount.getText().toString());
        } catch (ParseException e) {
            lDiscount = 0.0;
            Log.e("Discount Error", e.getMessage());
        }
        try {
            lTotal = Variables.strToDouble(edTotal.getText().toString());
        } catch (ParseException e) {
            lTotal = 0.0;
            Log.e("Total Error", e.getMessage());
        }
        try {
            lTax = Variables.strToDouble(edTax.getText().toString());
        } catch (ParseException e) {
            lTax = 0.0;
            Log.e("Tax Error", e.getMessage());
        }
        try {
            lTaxRate = Variables.strToDouble(edTaxRate.getText().toString());
        } catch (ParseException e) {
            lTaxRate = 0.0;
            Log.e("Tax Error", e.getMessage());
        }


        Intent out = new Intent();

        out.putExtra("productid", gProductId);
        out.putExtra("amount", lQuantity);
        out.putExtra("unitprice", lUnitPrice);
        out.putExtra("discount", lDiscount);
        out.putExtra("total", lTotal);
        out.putExtra("tax", lTax);
        out.putExtra("taxrate", lTaxRate);
        out.putExtra("ispackage", isPackage);
        out.putExtra("rowid", gRowId);

        setResult(RESULT_OK, out);
        finish();
    }
}
