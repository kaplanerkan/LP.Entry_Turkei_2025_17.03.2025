package com.eqpos.eqentry;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.eqpos.eqentry.DB.CustomerDao;

import java.util.ArrayList;
import java.util.HashMap;

public class SelectSupplierActivity extends AppCompatActivity {

    private ArrayList<HashMap<String, String>> gList;
    private EditText edSearch;
    private Button btSearch;
    private ListView lsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_supplier);
        this.setTitle(getString(R.string.supplier));

        btSearch = (Button) findViewById(R.id.bt_selectsupplier_search);
        edSearch = (EditText) findViewById(R.id.ed_selectsupplier_search);
        lsList = (ListView) findViewById(R.id.ls_selectsupplier_list);

        lsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent out = new Intent();
                out.putExtra("id", Integer.parseInt(gList.get(position).get("id")));
                out.putExtra("suppliername", gList.get(position).get("suppliername"));
                setResult(RESULT_OK, out);
                finish();
            }
        });

        getSupplierList();
    }


    private void getSupplierList() {
        gList = null;

        gList = CustomerDao.getSupplierList(edSearch.getText().toString());
        SimpleAdapter adp = new SimpleAdapter(this, gList, R.layout.select_customer_list_row,
                new String[]{"suppliername", ""},
                new int[]{R.id.lbl_select_customer_list_row_customername, R.id.lbl_select_customer_list_row_balance});
        try {
            lsList.setAdapter(adp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
