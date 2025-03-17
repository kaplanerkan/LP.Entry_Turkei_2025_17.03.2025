package com.eqpos.eqentry;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.eqpos.eqentry.DB.CustomerDao;
import com.eqpos.eqentry.tools.Variables;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomersActivity extends AppCompatActivity {

    private com.baoyz.swipemenulistview.SwipeMenuListView lsList;
    private EditText edSearch;
    private Button btSearch;
    private Button btNewCustomer;
    private ArrayList<HashMap<String, String>> gList;

    private int _NEWCUSTOMER = 50104;
    private int _SHOWCUSTOMER = 50105;
    private int _EDITCUSTOMER = 50106;
    private boolean isList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers);
        this.setTitle(R.string.customers);

        lsList = (com.baoyz.swipemenulistview.SwipeMenuListView) findViewById(R.id.lv_customers_list);
        edSearch = (EditText) findViewById(R.id.ed_customers_search);
        btSearch = (Button) findViewById(R.id.bt_customers_search);
        btNewCustomer = (Button) findViewById(R.id.bt_customers_new);

        getCustomerList();
        isList = false;
        Bundle extra = getIntent().getExtras();
        if (extra == null) {
            isList = true;
            createSwipeListview();
        }

        btSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCustomerList();
            }
        });
        btNewCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newCustomer();
            }
        });

        lsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (!isList) {
                    Intent out = new Intent();
                    out.putExtra("id", Integer.parseInt(gList.get(position).get("id")));
                    out.putExtra("customername", gList.get(position).get("customername"));
                    setResult(RESULT_OK, out);
                    finish();
                } else {
                    Intent intent = new Intent(view.getContext(), ShowCustomerActivity.class);
                    intent.putExtra("customerid", Integer.parseInt(gList.get(position).get("id")));
                    startActivityForResult(intent, _SHOWCUSTOMER);
                }
            }
        });

        lsList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                long lId = Long.parseLong(gList.get(position).get("id"));

                switch (index) {
                    case 0:
                        if (gList.get(position).get("isnew").equals("1")) {
                            editCustomer(lId);
                        }
                        break;
                    case 1:
                        Intent collect = new Intent(menu.getContext(), CollectActivity.class);
                        collect.putExtra("customerid", lId);
                        collect.putExtra("collectid", 0);
                        collect.putExtra("customername", gList.get(position).get("customername"));
                        startActivity(collect);
                        break;

                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getCustomerList();
    }

    private void getCustomerList() {
        gList = null;

        gList = CustomerDao.getCustomerList(edSearch.getText().toString());
        SimpleAdapter adp = new SimpleAdapter(this, gList, R.layout.select_customer_list_row,
                new String[]{"customername", "balance", "lastbalance", "subtotal", "discount", "lblYeniBakiye"},
                new int[]{R.id.lbl_select_customer_list_row_customername,
                        R.id.lbl_select_customer_list_row_balance,
                        R.id.lbl_select_customer_list_row_balance_last,
                        R.id.lbl_select_customer_list_row_subtotal,
                        R.id.lbl_select_customer_list_row_balance_discount,
                        R.id.lblYeniBakiye
                });
        try {
            lsList.setAdapter(adp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void newCustomer() {
        Intent intent = new Intent(this, NewCustomerActivity.class);
        startActivityForResult(intent, _NEWCUSTOMER);
    }

    private void editCustomer(long prCustomerId) {
        Intent intent = new Intent(this, NewCustomerActivity.class);
        intent.putExtra("customerid", prCustomerId);
        startActivityForResult(intent, _EDITCUSTOMER);
    }

    private void createSwipeListview() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                //create changeprice item
                SwipeMenuItem editItem = new SwipeMenuItem(getApplicationContext());
                editItem.setBackground(R.color.colorWhite);
                editItem.setWidth(Variables.dp2px(90));
                editItem.setIcon(R.mipmap.img_edit);
                editItem.setTitle(R.string.editcustomer);
                editItem.setTitleSize(12);
                editItem.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(editItem);

                //create changeprice item
                SwipeMenuItem collectionItem = new SwipeMenuItem(getApplicationContext());
                collectionItem.setBackground(R.color.colorWhite);
                collectionItem.setWidth(Variables.dp2px(90));
                collectionItem.setIcon(R.mipmap.img_collection);
                collectionItem.setTitle(R.string.collection);
                collectionItem.setTitleSize(12);
                collectionItem.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(collectionItem);
            }
        };
        lsList.setMenuCreator(creator);
    }
}
