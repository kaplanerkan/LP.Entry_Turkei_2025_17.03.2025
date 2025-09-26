package com.eqpos.eqentry;

import android.app.DatePickerDialog;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.ToggleButton;

import com.eqpos.eqentry.tools.CaptureActivityPortrait;
import com.eqpos.eqentry.tools.Variables;
import com.google.zxing.integration.android.IntentIntegrator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class StockEntryDetailAmountActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText edExpirationDate;
    private EditText edPartNumber;
    private EditText edAmount;
    private EditText edCostPrice;
    private EditText edAmountPerUnit;
    private LinearLayout lyPartNumber;
    private LinearLayout lyExpirationDate;
    private LinearLayout lyCostPrice;
    private ToggleButton btPackage;
    private Button btOk;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private Calendar myCalendar;

    private String expirationDate = "";
    private String partNumber = "";
    private Double amount = 1.0;
    private Double costPrice = 0.0;
    private int gRowId = 0;
    private boolean isPackage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_entry_detail_amount);
        this.setTitle(R.string.amount);


        myCalendar = Calendar.getInstance();
        edExpirationDate = (EditText) findViewById(R.id.ed_stockentrydetailamount_expirationdate);
        edPartNumber = (EditText) findViewById(R.id.ed_stockentrydetailamount_partnumber);
        edAmountPerUnit = (EditText) findViewById(R.id.ed_stockentrydetailamount_amountperunit);
        edAmount = (EditText) findViewById(R.id.ed_stockentrydetailamount_amount);
        edCostPrice = (EditText) findViewById(R.id.ed_stockentrydetailamount_costprice);
        btPackage = (ToggleButton) findViewById(R.id.bt_stockentrydetailamount_package);
        btOk = (Button) findViewById(R.id.bt_stockentrydetailamount_ok);
        lyPartNumber = (LinearLayout) findViewById(R.id.ly_stockentrydetailamount_partnumber);
        lyExpirationDate = (LinearLayout) findViewById(R.id.ly_stockentrydetailamount_expirationdate);
        lyCostPrice = (LinearLayout) findViewById(R.id.ly_stockentrydetailamount_costprice);

        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            boolean isTransfer = false;
            if (extra.containsKey("isTransfer")) {
                lyPartNumber.setVisibility(View.GONE);
                lyExpirationDate.setVisibility(View.GONE);
                lyCostPrice.setVisibility(View.GONE);
                isTransfer = true;
            }

            if (!isTransfer) {
                expirationDate = extra.getString("expirationdate");
                partNumber = extra.getString("partnumber");
            }
            btPackage.setTextOff(extra.getString("unitname"));
            boolean isQuantity = false;
            String barcodeValue = "";
            if (extra.containsKey("isquantity")) {
                isQuantity = extra.getBoolean("isquantity");
            }

            if (extra.containsKey("barcodevalue")) {
                barcodeValue = extra.getString("barcodevalue");
            }

            isPackage = (isQuantity && barcodeValue.length() > 0);

            try {
                amount = extra.getDouble("amount");
                if (extra.containsKey("barcodevalue") && isQuantity) {
                    amount = Variables.strToDouble(extra.getString("barcodevalue")) / 1000.0;
                }
                if (amount > 0)
                    edAmount.setText(Variables.doubleToStr(amount,0));
            }catch(Exception e) {
                Log.e("Convet Error", e.getMessage());
                amount = 1.0;
            }



            if (!isTransfer) {
                try {
                    costPrice = extra.getDouble("costprice");
                    if (extra.containsKey("barcodevalue") && !isQuantity) {
                        costPrice = Variables.strToDouble(extra.getString("barcodevalue")) / 100.0;
                    }
                    if (costPrice > 0)
                        edCostPrice.setText(Variables.doubleToStr(costPrice, 2));
                } catch (Exception e) {
                    Log.e("Convet Error", e.getMessage());
                    costPrice = 0.0;
                }
            }

            gRowId = 0;
            if (extra.containsKey("rowid")) {
                gRowId = extra.getInt("rowid");
            }
        }


        edExpirationDate.setText(expirationDate);
        edPartNumber.setText(partNumber);

        btOk.setOnClickListener(this);
        edExpirationDate.setOnClickListener(this);
        dateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateEdit();
        };
        btPackage.setChecked(false);
    }

    private void setAndFinishValues() {

        String partNumber = edPartNumber.getText().toString();
        if (partNumber == null)
            partNumber = "";
        String expirationDate = edExpirationDate.getText().toString();
        if (expirationDate == null)
            expirationDate = "";
        Double lAmount = 0.0;
        Double lAmountPerUnit = 1.0;
        Double lCostPrice = 0.0;
        try {
            lAmount = Variables.strToDouble(edAmount.getText().toString());
        } catch (ParseException e) {
            lAmount = 0.0;
            Log.e("Amount Error", e.getMessage());
        }
        try {
            lAmountPerUnit = Variables.strToDouble(edAmountPerUnit.getText().toString());
        } catch (ParseException e) {
            lAmountPerUnit = 1.0;
            Log.e("Amount Error", e.getMessage());
        }

        if (btPackage.isChecked()) {
            if (lAmountPerUnit <= 0.0)
                lAmountPerUnit = 1.0;
            lAmount *= lAmountPerUnit;
        }


        try {
            lCostPrice = Variables.strToDouble(edCostPrice.getText().toString());
        } catch (ParseException e) {
            lCostPrice = 0.0;
            Log.e("CostPrice Error", e.getMessage());
        }


        Intent out = new Intent();

        out.putExtra("partnumber", partNumber);
        out.putExtra("expirationdate", expirationDate);
        out.putExtra("amount", lAmount);
        out.putExtra("costprice", lCostPrice);
        out.putExtra("ispackage", isPackage);
        out.putExtra("rowid", gRowId);

        setResult(RESULT_OK, out);
        finish();
    }


    private void setExpirationDate() {
        new DatePickerDialog(StockEntryDetailAmountActivity.this, dateSetListener, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void updateEdit() {
        String myFormat = "yyyy.MM.dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        edExpirationDate.setText(sdf.format(myCalendar.getTime()));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_stockentrydetailamount_ok:
                setAndFinishValues();
                break;
            case R.id.ed_stockentrydetailamount_expirationdate:
                setExpirationDate();
                break;
        }
    }
}
