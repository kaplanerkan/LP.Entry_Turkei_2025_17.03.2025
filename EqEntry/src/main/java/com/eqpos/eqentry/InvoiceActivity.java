package com.eqpos.eqentry;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleAdapter;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.eqpos.eqentry.db.InvoiceDao;
import com.eqpos.eqentry.printing.PrintInvoice;
import com.eqpos.eqentry.tools.Variables;

import java.util.ArrayList;
import java.util.HashMap;

public class InvoiceActivity extends AppCompatActivity implements View.OnClickListener {

    private Button newInvoice;
    private SwipeMenuListView lvList;

    private ArrayList<HashMap<String, String>> gList;
    int lastPosition = 0;
    int _NEWINVOICE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);
        this.setTitle(R.string.invoice);

        newInvoice = (Button) findViewById(R.id.bt_invoice_new);
        lvList = (SwipeMenuListView) findViewById(R.id.lv_invoice_list);

        newInvoice.setOnClickListener(this);

        getInvoiceList();
        createSwipeListview();

        lvList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                long lId = Long.parseLong(gList.get(position).get("id"));
                long customerid = Long.parseLong(gList.get(position).get("customerid"));
                Intent newInvoiceIntent;

                lastPosition = position;
                switch (index) {
                    case 0:
                        InvoiceDao.deleteInvoice(lId, customerid);
                        getInvoiceList();
                        break;
                    case 1:
                        //Print invoice
                        PrintInvoice.printInvoice(lId);
                        break;
                    case 2:
                        String subtotal = gList.get(position).get("subtotal").replace(".","");
                        subtotal = subtotal.replace(",",".");
                        newInvoiceIntent = new Intent(menu.getContext(), NewInvoiceActivity.class);
                        newInvoiceIntent.putExtra("invoiceid", lId);
                        newInvoiceIntent.putExtra("customerid", Integer.parseInt(gList.get(position).get("customerid")));
                        newInvoiceIntent.putExtra("customername", gList.get(position).get("customername"));
                        newInvoiceIntent.putExtra("invoicenumber", gList.get(position).get("invoicenumber"));
                        newInvoiceIntent.putExtra("invoicedate",  gList.get(position).get("invoicedate"));
                        newInvoiceIntent.putExtra("includetax",  Integer.parseInt(gList.get(position).get("includetax")));
                        newInvoiceIntent.putExtra("subtotal",  Double.parseDouble(subtotal));
                        startActivityForResult(newInvoiceIntent, _NEWINVOICE);
                        break;

                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == _NEWINVOICE) {
            getInvoiceList();
        }
    }

    private void getInvoiceList() {
        gList = null;

        gList = InvoiceDao.getInvoiceList();
        SimpleAdapter adp = new SimpleAdapter(this, gList, R.layout.invoicelist_row,
                new String[]{"customername", "invoicedate", "invoicenumber", "subtotal"},
                new int[]{R.id.lbl_invoicelist_row_customername, R.id.lbl_invoicelist_row_date,
                        R.id.lbl_invoicelist_row_number, R.id.lbl_invoicelist_row_subtotal});
        try {
            lvList.setAdapter(adp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_invoice_new:
                Intent newInvoiceIntent = new Intent(this, NewInvoiceActivity.class);
                newInvoiceIntent.putExtra("invoiceid", 0);
                startActivityForResult(newInvoiceIntent, _NEWINVOICE);
                break;
        }
    }


    private void createSwipeListview() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                //create changeprice item
                SwipeMenuItem deleteInvoice = new SwipeMenuItem(getApplicationContext());
                deleteInvoice.setBackground(R.color.colorWhite);
                deleteInvoice.setWidth(Variables.dp2px(90));
                deleteInvoice.setIcon(R.mipmap.img_delete);
                deleteInvoice.setTitle(R.string.deleteinvoice);
                deleteInvoice.setTitleSize(12);
                deleteInvoice.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(deleteInvoice);


                //create printlabel item
                SwipeMenuItem printInvoice = new SwipeMenuItem(getApplicationContext());
                printInvoice.setBackground(R.color.colorWhite);
                printInvoice.setWidth(Variables.dp2px(90));
                printInvoice.setIcon(R.mipmap.img_label);
                printInvoice.setTitle(R.string.print);
                printInvoice.setTitleSize(12);
                printInvoice.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(printInvoice);

                SwipeMenuItem editInvoice = new SwipeMenuItem((getApplicationContext()));
                editInvoice.setBackground(R.color.colorWhite);
                editInvoice.setWidth(Variables.dp2px(90));
                editInvoice.setIcon(R.mipmap.img_editproduct);
                editInvoice.setTitle(R.string.editinvoice);
                editInvoice.setTitleSize(12);
                editInvoice.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(editInvoice);
            }
        };
        lvList.setMenuCreator(creator);
    }
}
