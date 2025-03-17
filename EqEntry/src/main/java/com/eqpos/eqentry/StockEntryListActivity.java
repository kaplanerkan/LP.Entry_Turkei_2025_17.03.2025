/*
*  Bu sınıfta oluşturulmuş ama sunucuya gönderilmemiş irsaliyeler listelenecek.
*  Fonksiyonlar;
*  1- Yeni irsaliye oluşturmak
*  2- İrsaliye bilgilerini düzeltmek
*  3- İrsaliyeyi silmek
*  4- irsaliyedeki ürünleri düzenlemek
 */

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
import com.eqpos.eqentry.DB.StockEntryDao;
import com.eqpos.eqentry.Printing.PrintStockEntry;
import com.eqpos.eqentry.tools.Variables;

import java.util.ArrayList;
import java.util.HashMap;

public class StockEntryListActivity extends AppCompatActivity implements View.OnClickListener {
    private SwipeMenuListView lsList;
    private Button btNew;

    private ArrayList<HashMap<String, String>> gList;

    private int _STOCKENTRY = 5000;
    private int lastPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_entry_list);
        this.setTitle(R.string.stockentry);

        lsList = (SwipeMenuListView) findViewById(R.id.ls_stockentrylist_list);
        btNew = (Button) findViewById(R.id.bt_stockentrylist_new);
        btNew.setOnClickListener(this);



        createSwipeListview();

        lsList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                int lId = Integer.parseInt(gList.get(position).get("id"));

                lastPosition = position;
                switch (index) {
                    case 0:
                        //changeprice
                        editDelivery(lId);
                        break;
                    case 1:
                        //Print invoice
                        PrintStockEntry.printDelivery(lId);
                        break;
                    case 2:
                        //PrintLabel
                        StockEntryDao.removeDelivery(lId);
                        getDeliveryList();
                        break;

                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
        getDeliveryList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == _STOCKENTRY) {
            getDeliveryList();
        }
    }

    private void getDeliveryList() {
        gList = StockEntryDao.getDeliveryList();
        if (gList != null) {
            SimpleAdapter adp = new SimpleAdapter(this, gList, R.layout.stock_entrylist_row,
                    new String[]{"documentdate", "documentnumber", "suppliername"},
                    new int[]{R.id.lbl_stockentrylistrow_date, R.id.lbl_stockentrylistrow_number,
                            R.id.lbl_stockentrylistrow_suppliername});
            lsList.setAdapter(adp);
            lsList.setSelection(lastPosition);
        }
    }

    private void editDelivery(int deliveryId) {

        Intent stockEntryIntent = new Intent(this, NewStockEntryActivity.class);
        stockEntryIntent.putExtra("deliveryid", deliveryId);
        startActivityForResult(stockEntryIntent, _STOCKENTRY);
    }

    private void createSwipeListview() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                //create changeprice item
                SwipeMenuItem EditItem = new SwipeMenuItem(getApplicationContext());
                EditItem.setBackground(R.color.colorWhite);
                EditItem.setWidth(Variables.dp2px(90));
                EditItem.setIcon(R.mipmap.img_editproduct);
                EditItem.setTitle(R.string.editdelivery);
                EditItem.setTitleSize(12);
                EditItem.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(EditItem);

                //create printlabel item
                SwipeMenuItem printInvoice = new SwipeMenuItem(getApplicationContext());
                printInvoice.setBackground(R.color.colorWhite);
                printInvoice.setWidth(Variables.dp2px(90));
                printInvoice.setIcon(R.mipmap.img_label);
                printInvoice.setTitle(R.string.print);
                printInvoice.setTitleSize(12);
                printInvoice.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(printInvoice);

                //create printlabel item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(R.color.colorWhite);
                deleteItem.setWidth(Variables.dp2px(90));
                deleteItem.setIcon(R.mipmap.img_delete);
                deleteItem.setTitle(R.string.removedelivery);
                deleteItem.setTitleSize(12);
                deleteItem.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(deleteItem);

            }
        };
        lsList.setMenuCreator(creator);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_stockentrylist_new:
                editDelivery(0);
                break;
        }
    }
}
