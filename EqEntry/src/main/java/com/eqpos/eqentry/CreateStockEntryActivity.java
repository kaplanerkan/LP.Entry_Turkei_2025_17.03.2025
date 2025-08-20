package com.eqpos.eqentry;

import android.app.DatePickerDialog;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.eqpos.eqentry.db.StockEntryDao;
import com.eqpos.eqentry.models.Delivery;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class CreateStockEntryActivity extends AppCompatActivity implements View.OnClickListener {
    private ArrayList<String> gSupplierNameList;
    private ArrayList<Integer> gSupplierIdList;
    private EditText edDocumentNumber;
    private EditText edDocumentDate;
    private EditText edReceiver;
    private AutoCompleteTextView edSupplier;
    private Button btOk;

    private long gStockEntryId = 0;
    private int gSupplierId = 0;
    private Delivery gDelivery;
    private Calendar myCalendar;
    DatePickerDialog.OnDateSetListener dateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_stock_entry);
        this.setTitle(R.string.newstockentry);


        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            gStockEntryId = extra.getInt("deliveryid");
        }

        myCalendar = Calendar.getInstance();
        edDocumentDate = (EditText) findViewById(R.id.ed_createstockentry_documentdate);
        edDocumentNumber = (EditText) findViewById(R.id.ed_createstockentry_documentnumber);
        edSupplier = (AutoCompleteTextView) findViewById(R.id.ed_createstockentry_supplier);
        edReceiver = (EditText) findViewById(R.id.ed_createstockentry_receiver);
        btOk = (Button) findViewById(R.id.bt_createstockentry_ok);

        btOk.setOnClickListener(this);
        edDocumentDate.setOnClickListener(this);
        dateListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateEdit();
            }
        };

        if (gStockEntryId > 0) {
            gDelivery = StockEntryDao.getDelivery((int) gStockEntryId);
            if (gDelivery.getId() > 0) {
                edDocumentNumber.setText(gDelivery.getNumber());
                edDocumentDate.setText(gDelivery.getDate());
                edSupplier.setText(gDelivery.getSupplierName());
                gSupplierId = gDelivery.getSupplierId();
                edReceiver.setText(gDelivery.getReceiverName());
            }
            gStockEntryId = gDelivery.getId();
        } else {
            gSupplierId = 0;
            gDelivery = new Delivery();
        }

        getSupplierList();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_createstockentry_ok:
                setValuesAndFinish();

                break;

            case R.id.ed_createstockentry_documentdate:
                setDocumentDate();
                break;
        }
    }

    private void getSupplierList() {
        gSupplierNameList = StockEntryDao.getSupplierNameList();
        gSupplierIdList = StockEntryDao.getSupplierIdList();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item, gSupplierNameList);
        edSupplier.setAdapter(adapter);
    }

    private void setDocumentDate() {
        new DatePickerDialog(CreateStockEntryActivity.this, dateListener, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    private void updateEdit() {
        String myFormat = "yyyy.MM.dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        edDocumentDate.setText(sdf.format(myCalendar.getTime()));
    }

    private void setValuesAndFinish() {
        Intent out = new Intent();

        int lSupplierId = gSupplierId;
        try {
            lSupplierId = gSupplierIdList.get(gSupplierNameList.indexOf(edSupplier.getText().toString()));
        } catch (Exception e) {
            lSupplierId = 0;
            edSupplier.setText("");
            edSupplier.requestFocus();
            return;
        }
        gStockEntryId = StockEntryDao.saveDeliveryNote((int) gStockEntryId, edDocumentNumber.getText().toString(),
                edDocumentDate.getText().toString(), lSupplierId, edReceiver.getText().toString());

        out.putExtra("deliveryid", gStockEntryId);

        setResult(RESULT_OK, out);
        finish();
    }
}
