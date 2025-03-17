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
import android.widget.TextView;

import com.eqpos.eqentry.DB.CustomerDao;
import com.eqpos.eqentry.Models.Collect;
import com.eqpos.eqentry.tools.Variables;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CollectActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btCash;
    private Button btCard;
    private EditText edNumber;
    private EditText edDate;
    private EditText edAmount;
    private EditText edDescription;
    private TextView lblCustomer;

    private long gCustomerId;
    private int gCollectId;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private Calendar myCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);
        this.setTitle(R.string.collection);

        gCollectId = 0;
        gCustomerId = 0;

        btCash = (Button) findViewById(R.id.bt_collect_cash);
        btCard = (Button) findViewById(R.id.bt_collect_creditcard);
        edNumber = (EditText) findViewById(R.id.ed_collect_number);
        edDate = (EditText) findViewById(R.id.ed_collect_date);
        edAmount = (EditText) findViewById(R.id.ed_collect_amount);
        edDescription = (EditText) findViewById(R.id.ed_collect_description);
        lblCustomer = (TextView) findViewById(R.id.lbl_collect_customer);
        myCalendar = Calendar.getInstance();

        Bundle extra = getIntent().getExtras();
        if (extra!=null) {
            gCustomerId = extra.getLong("customerid");
            gCollectId = extra.getInt("collectid");
            lblCustomer.setText(extra.getString("customername"));
        } else {
            finish();
        }

        if (gCollectId > 0) {
            getCollect();
        } else {
            updateEdit();
        }

        btCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCollect(0);
            }
        });

        if (Variables.isCreditCardActive) {

            btCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveCollect(1);
                }
            });
        } else {
            btCard.setVisibility(View.GONE);
        }

        edDate.setOnClickListener(this);
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
    }

    private void saveCollect(int paymentType) {
        double lAmount = 0.0;
        try {
            lAmount = Variables.strToDouble(edAmount.getText().toString());
        } catch (Exception ex) {
            Log.e("Save collect", ex.getMessage());
        }
        CustomerDao.addCollect(gCollectId, gCustomerId, edNumber.getText().toString(), edDate.getText().toString(),
                paymentType, lAmount, edDescription.getText().toString());

        setResult(RESULT_OK);
        finish();
    }

    private void getCollect() {
        Collect collect = CustomerDao.getCollect(gCollectId);

        if (collect.getAmount() > 0)
            edAmount.setText(Variables.doubleToStr(collect.getAmount(),2));
        edDate.setText(collect.getDate());
        edDescription.setText(collect.getDescription());
        edNumber.setText(collect.getNumber());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ed_collect_date:
                setDocumentDate();
                break;
        }
    }

    private void setDocumentDate() {
        new DatePickerDialog(CollectActivity.this, dateSetListener, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();

    }
    private void updateEdit() {
        String myFormat = "yyyy.MM.dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        edDate.setText(sdf.format(myCalendar.getTime()));
    }

}
