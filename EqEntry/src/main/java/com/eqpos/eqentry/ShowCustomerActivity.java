package com.eqpos.eqentry;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.eqpos.eqentry.DB.CustomerDao;
import com.eqpos.eqentry.DB.InvoiceDao;
import com.eqpos.eqentry.Models.Customer;
import com.eqpos.eqentry.Printing.PrintCollect;
import com.eqpos.eqentry.tools.Variables;

import java.util.ArrayList;
import java.util.HashMap;

public class ShowCustomerActivity extends AppCompatActivity {
    private Button btCollection;
    private Button btEdit, btnTahsilatlariYazdir;
    private TextView lblCustomerCode;
    private TextView lblCustomerName;
    private TextView lblPhone1;
    private TextView lblPhone2;
    private TextView lblAddress;
    private TextView lblEmail;
    private TextView lblPreviousBalance;
    private TextView lblNewBalance;
    private TextView lblTaxId;
    private TextView lblTaxOffice;
    private TextView lblTotalBalance;

    private long gCustomerId;
    private int _EDITCUSTOMER = 9090;
    private int _COLLECT = 9091;
    private SwipeMenuListView lvList;
    private ArrayList<HashMap<String, String>> gList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_customer);
        this.setTitle(R.string.customer);

        btCollection = (Button) findViewById(R.id.bt_showcustomer_collection);
        btEdit = (Button) findViewById(R.id.bt_showcustomer_edit);
        lblCustomerCode = (TextView) findViewById(R.id.lbl_showcustomer_customercode);
        lblCustomerName = (TextView) findViewById(R.id.lbl_showcustomer_customername);
        lblPhone1 = (TextView) findViewById(R.id.lbl_showcustomer_phone1);
        lblPhone2 = (TextView) findViewById(R.id.lbl_showcustomer_phone2);
        lblAddress = (TextView) findViewById(R.id.lbl_showcustomer_address);
        lblEmail = (TextView) findViewById(R.id.lbl_showcustomer_email);
        lblPreviousBalance = (TextView) findViewById(R.id.lbl_showcustomer_previousbalance);
        lblNewBalance = (TextView) findViewById(R.id.lbl_showcustomer_newbalance);
        lblTotalBalance = (TextView) findViewById(R.id.lblTotalBalance);
        lblTaxId = (TextView) findViewById(R.id.lbl_showcustomer_taxid);
        lblTaxOffice = (TextView) findViewById(R.id.lbl_showcustomer_taxoffice);
        lvList = (SwipeMenuListView) findViewById(R.id.lv_showcustomer_list);

        btnTahsilatlariYazdir = (Button) findViewById(R.id.btnTahsilatlariYazdir);

        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            gCustomerId = extra.getInt("customerid");
        } else {
            finish();
        }

        fillDatas();
        getCollects();

        btCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editCollect(0);
            }
        });

        btEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editCustomer();
            }
        });



        lvList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                int lId = Integer.parseInt(gList.get(position).get("id"));

                switch (index) {
                    case 0:
                        editCollect(lId);
                        break;
                    case 1:
                        CustomerDao.deleteCollect(lId);
                        getCollects();
                        fillDatas();
                        break;
                    case 2:
                        printCollect(lId);
                        break;

                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
        createSwipeListview();

        btnTahsilatlariYazdirOlaylari();
    }

    private void btnTahsilatlariYazdirOlaylari() {
        btnTahsilatlariYazdir.setOnClickListener(v -> {
            if (gList != null && gList.size() > 0) {
                PrintCollect.printAllCollects(gCustomerId);
            } else {
                Toast.makeText(ShowCustomerActivity.this, "Tahsilat Bulunamadi !", Toast.LENGTH_SHORT).show();
            }
        });
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == _EDITCUSTOMER) {
            fillDatas();
        } else if (resultCode == RESULT_OK && requestCode == _COLLECT){
            fillDatas();
            getCollects();
        }
    }

    private void fillDatas() {
        Customer cus = CustomerDao.getCustomer(gCustomerId);
        lblCustomerCode.setText(String.valueOf(cus.getCode()));
        lblCustomerName.setText(cus.getName());
        lblAddress.setText(cus.getAddress());
        lblPhone1.setText(cus.getPhone1());
        lblPhone2.setText(cus.getPhone2());
        lblEmail.setText(cus.getEmail());
        lblTaxId.setText(cus.getTaxId());
        lblTaxOffice.setText(cus.getTaxOffice());

        //lblPreviousBalance.setText(Variables.doubleToStr(cus.getOldBalance()+cus.getCollects(),2));               // DEPRACED: 30.07.2025 ERKAN
        lblPreviousBalance.setText(Variables.doubleToStr(cus.getOldBalance(),2));

        lblNewBalance.setText(Variables.doubleToStr(cus.getNewBalance()+cus.getCollects(),2));

        //double getTotalBalance = cus.getOldBalance()+cus.getCollects() + cus.getNewBalance()+cus.getCollects();    // DEPRACED: 30.07.2025 ERKAN
        double getTotalBalance = cus.getOldBalance()+cus.getCollects() + cus.getNewBalance();
        lblTotalBalance.setText(Variables.doubleToStr(getTotalBalance,2));

        if (cus.getIsNew()==0)
            btEdit.setVisibility(View.INVISIBLE);
        cus = null;
    }


    private void getCollects() {
        gList = null;

        gList = CustomerDao.getCollectList(gCustomerId);
        SimpleAdapter adp = new SimpleAdapter(this, gList, R.layout.collectlist_row,
                new String[]{"collectdate", "documentnumber", "amount", "description", "paymenttype"},
                new int[]{R.id.lbl_collectlist_row_date, R.id.lbl_collectlist_row_number,
                        R.id.lbl_collectlist_row_amount, R.id.lbl_collectlist_row_description,
                        R.id.lbl_collectlist_row_paymenttype});
        try {
            lvList.setAdapter(adp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void editCustomer() {
        Intent intent = new Intent(this, NewCustomerActivity.class);
        intent.putExtra("customerid", gCustomerId);
        startActivityForResult(intent, _EDITCUSTOMER);
    }


    private void editCollect(int id) {
        Intent intent = new Intent(this, CollectActivity.class);
        intent.putExtra("customerid", gCustomerId);
        intent.putExtra("collectid", id);
        intent.putExtra("customername", lblCustomerName.getText().toString());

        // 30.07.2025 ERKAN
        intent.putExtra("yeni_bakiye",lblTotalBalance.getText().toString());

        startActivityForResult(intent, _COLLECT);
    }

    private void printCollect(int id){
        //PrintCollect.printInvoice(id);
        PrintCollect.printInvoiceERKAN(id);
    }

    private void createSwipeListview() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                //create edit item
                SwipeMenuItem editItem = new SwipeMenuItem(getApplicationContext());
                editItem.setBackground(R.color.colorWhite);
                editItem.setWidth(Variables.dp2px(90));
                editItem.setIcon(R.mipmap.img_edit);
                editItem.setTitle(R.string.editcollect);
                editItem.setTitleSize(12);
                editItem.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(editItem);

                //create edit item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(R.color.colorWhite);
                deleteItem.setWidth(Variables.dp2px(90));
                deleteItem.setIcon(R.mipmap.img_delete);
                deleteItem.setTitle(R.string.delete);
                deleteItem.setTitleSize(12);
                deleteItem.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(deleteItem);

                //create print item
                SwipeMenuItem collectionItem = new SwipeMenuItem(getApplicationContext());
                collectionItem.setBackground(R.color.colorWhite);
                collectionItem.setWidth(Variables.dp2px(90));
                collectionItem.setIcon(R.mipmap.img_label);
                collectionItem.setTitle(R.string.print);
                collectionItem.setTitleSize(12);
                collectionItem.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(collectionItem);
            }
        };
        lvList.setMenuCreator(creator);
    }
}
