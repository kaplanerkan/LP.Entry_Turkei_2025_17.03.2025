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

import com.eqpos.eqentry.adapters.ProductListAdapter;
import com.eqpos.eqentry.db.ProductDao;

import java.util.ArrayList;
import java.util.HashMap;

public class SelectProductActivity extends AppCompatActivity {
    ArrayList<HashMap<String, String>> gProductList = null;
    private ListView lsList;
    private Button btNewProduct;
    private Button btFind;
    private EditText edFind;
    String gSearch="";

    int gFilterType=0;
    String gGroupName="";
    private int _SELECTGROUP = 10220;
    private int _NEWPRODUCT = 10232;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_product);
        this.setTitle(R.string.productlist);

        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            gSearch = extra.getString("search");
            gFilterType = extra.getInt("filtertype");
        }

        lsList = (ListView) findViewById(R.id.ls_selectproduct_list);
        btNewProduct = (Button) findViewById(R.id.bt_selectproduct_newproduct);
        btFind = (Button) findViewById(R.id.bt_selectproduct_find);
        edFind = (EditText) findViewById(R.id.ed_selectproduct_find);

        btNewProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newProduct();
            }
        });

        btFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gSearch = edFind.getText().toString();
                listProducts();
            }
        });
        lsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int priceorder = Integer.parseInt(gProductList.get(position).get("priceorder"));

                Intent out = new Intent();
                out.putExtra("id", Integer.parseInt(gProductList.get(position).get("id")));
                out.putExtra("priceorder",Integer.parseInt(gProductList.get(position).get("priceorder")));
                setResult(RESULT_OK, out);
                finish();
            }
        });

        if (gSearch.length() > 0)
            listProducts();
        else
            selectGroup();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == _SELECTGROUP) {
            gGroupName = data.getStringExtra("groupname");
            listProducts();
        } if (resultCode == RESULT_OK && requestCode == _NEWPRODUCT) {
            edFind.setText(data.getStringExtra("productname"));
            gSearch = edFind.getText().toString();
            listProducts();
        } else {
            gGroupName = "";
        }

    }

    private void newProduct() {
        Intent intent = new Intent(this, EditProductActivity.class);
        intent.putExtra("productId", 0);
        startActivityForResult(intent, _NEWPRODUCT);
    }
    private void selectGroup() {
        Intent intent = new Intent(this, SelectGroupActivity.class);
        startActivityForResult(intent, _SELECTGROUP);
    }

    private void listProducts() {

        gProductList = ProductDao.getProductList(gSearch, gFilterType, gGroupName, "", "productname", "asc","");
        ProductListAdapter adp = new ProductListAdapter(this, gProductList, R.layout.productlist_row,
                new String[]{"productname", "barcode", "plu", "taxname", "groupname", "costprice",
                        "sellprice", "depositprice", "stock", "unitename"},
                new int[]{R.id.lbl_row_products_productname, R.id.lbl_row_products_barcode,
                        R.id.lbl_row_products_plu, R.id.lbl_row_products_taxname,
                        R.id.lbl_row_products_group, R.id.lbl_row_products_costprice,
                        R.id.lbl_row_products_sellprice, R.id.lbl_row_products_depositname,
                        R.id.lbl_row_products_stock, R.id.lbl_row_products_unitename});
        lsList.setAdapter(adp);
    }
}
