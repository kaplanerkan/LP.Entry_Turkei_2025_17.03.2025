package com.eqpos.eqentry;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.eqpos.eqentry.DB.ProductDao;
import com.eqpos.eqentry.tools.Variables;

public class GetAmountActivity extends AppCompatActivity implements View.OnClickListener {
    private int gProductId;
    private String gProductName;
    private String gUnit;

    private EditText edAmount;
    private ToggleButton btPackage;
    private Button btOk;
    private TextView lblProductName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_amount);
        this.setTitle(R.string.amount);

        edAmount = (EditText) findViewById(R.id.ed_getamount_amount);
        btOk = (Button) findViewById(R.id.bt_getamount_ok);
        btPackage = (ToggleButton) findViewById(R.id.bt_getamount_package);
        lblProductName = (TextView) findViewById(R.id.lbl_getamount_productname);

        gProductId = 0;
        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            gProductId = extra.getInt("productId");
            gProductName = extra.getString("productname");
            gUnit = extra.getString("unitename");
            btPackage.setTextOff(gUnit);
        }

        lblProductName.setText(gProductName);
        btOk.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_getamount_ok:
                setValuesAndFinish();
                break;
        }
    }

    private void setValuesAndFinish() {
        Intent out = new Intent();
        Double lAmount = 0.0;
        try {
            lAmount = Variables.strToDouble(edAmount.getText().toString());
        } catch (Exception e) {
            lAmount = 0.0;
        }

        out.putExtra("productid", gProductId);
        out.putExtra("amount", lAmount);
        if (btPackage.isChecked())
            out.putExtra("unitename", getString(R.string._package));
        else
            out.putExtra("unitename", gUnit);

        setResult(RESULT_OK, out);
        finish();
    }
}
