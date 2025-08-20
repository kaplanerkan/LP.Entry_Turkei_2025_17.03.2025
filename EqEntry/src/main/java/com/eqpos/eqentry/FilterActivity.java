package com.eqpos.eqentry;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.eqpos.eqentry.db.ProductDao;

import java.util.List;

public class FilterActivity extends AppCompatActivity {
    private Button btOk;
    private Spinner cmbGroup;
    private Spinner cmbTax;
    private Spinner cmbSortField;
    private Spinner cmbSortType;
    private EditText edStock;

    private List<String> gGroupList;
    private List<String> gTaxList;

    private String gGroupName;
    private String gTaxName;
    private String gSortField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        this.setTitle(R.string.filter);

        btOk = (Button) findViewById(R.id.bt_filter_sortok);
        cmbGroup = (Spinner) findViewById(R.id.cmb_filter_productgroup);
        cmbTax = (Spinner) findViewById(R.id.cmb_filter_taxname);
        cmbSortField = (Spinner) findViewById(R.id.cmb_filter_sortfield);
        cmbSortType = (Spinner) findViewById(R.id.cmb_filter_sorttype);

        this.getGroups();
        this.getTaxes();

        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setValuesAndFinish();
            }
        });
    }

    private void getGroups() {
        gGroupList = ProductDao.getGroupListForSpinner(getString(R.string.all));
        if (gGroupList != null) {
            ArrayAdapter<String> adp = new ArrayAdapter(this, android.R.layout.simple_spinner_item, gGroupList);
            cmbGroup.setAdapter(adp);
        } else {
            cmbGroup.setAdapter(null);
        }
    }

    private void getTaxes() {
        gTaxList = ProductDao.getTaxListForSpinner(getString(R.string.all));
        if (gTaxList != null) {
            ArrayAdapter<String> adp = new ArrayAdapter(this, android.R.layout.simple_spinner_item, gTaxList);
            cmbTax.setAdapter(adp);
        } else {
            cmbTax.setAdapter(null);
        }
    }

    private void setValuesAndFinish() {
        Intent out = new Intent();
        if (cmbGroup.getSelectedItemPosition() > 0)
            out.putExtra("groupname", cmbGroup.getSelectedItem().toString());
        else
            out.putExtra("groupname", "");

        if (cmbTax.getSelectedItemPosition() > 0)
            out.putExtra("taxname", cmbTax.getSelectedItem().toString());
        else
            out.putExtra("taxname", "");


        out.putExtra("sortfield", cmbSortField.getSelectedItem().toString());
        switch (cmbSortField.getSelectedItemPosition()) {
            case 0: out.putExtra("sortfield", "productname"); break;
            case 1: out.putExtra("sortfield", "sellprice"); break;
            case 2: out.putExtra("sortfield", "stock"); break;
            case 3: out.putExtra("sortfield", "groupname"); break;
        }
        if (cmbSortType.getSelectedItemPosition() == 0) {
            out.putExtra("sorttype", "asc");
        } else {
            out.putExtra("sorttype", "desc");
        }

        setResult(RESULT_OK, out);
        finish();
    }
}
