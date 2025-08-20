package com.eqpos.eqentry;

import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.eqpos.eqentry.db.CustomerDao;
import com.eqpos.eqentry.models.Customer;
import com.eqpos.eqentry.tools.Variables;

public class NewCustomerActivity extends AppCompatActivity {

    private EditText edName;
    private EditText edPhone1;
    //private EditText edPhone2;
    private EditText edAddress;
    private EditText edEmail;
    private EditText edTaxId;
    private EditText edTaxBuro;
    private Button btSave;

    private long gCustomerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_customer);
        this.setTitle(R.string.newcustomer);

        gCustomerId = 0;
        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            gCustomerId = extra.getLong("customerid");
        }

        edName = (EditText) findViewById(R.id.ed_newcustomer_name);
        edPhone1 = (EditText) findViewById(R.id.ed_newcustomer_phone1);
        //edPhone2 = (EditText) findViewById(R.id.ed_newcustomer_phone2);
        edAddress = (EditText) findViewById(R.id.ed_newcustomer_address);
        edEmail = (EditText) findViewById(R.id.ed_newcustomer_email);
        edTaxId = (EditText) findViewById(R.id.ed_newcustomer_taxid);
        edTaxBuro = (EditText) findViewById(R.id.ed_newcustomer_taxburo);
        btSave = (Button) findViewById(R.id.bt_newcustomer_save);

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCustomer(Variables.isTurkey);
            }
        });

        fillDatas();
    }


    private void fillDatas() {
        if (gCustomerId > 0) {
            Customer cus = CustomerDao.getCustomer(gCustomerId);
            edName.setText(cus.getName());
            edAddress.setText(cus.getAddress());
            edPhone1.setText(cus.getPhone1());
            //edPhone2.setText(cus.getPhone2());
            edEmail.setText(cus.getEmail());
            edTaxId.setText(cus.getTaxId());
            edTaxBuro.setText(cus.getTaxOffice());

            cus = null;
        }
    }


    private void saveCustomer(boolean isTurkey) { //Türkiyede vergi kimlik numarası olduğu için eklendi NHT

        CustomerDao.saveCustomer(
                gCustomerId,
                edName.getText().toString(),
                0,
                edPhone1.getText().toString(), null, null, //edPhone2.getText().toString()
                edAddress.getText().toString(), null, null,
                0.0, 0.0, 0.0,
                edEmail.getText().toString(),
                isTurkey? edTaxId.getText().toString(): null,
                isTurkey? edTaxBuro.getText().toString():null,
                1);

        setResult(RESULT_OK);
        finish();
    }
}
