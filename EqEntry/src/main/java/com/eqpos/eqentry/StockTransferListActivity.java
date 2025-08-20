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
import com.eqpos.eqentry.db.StockTransferDao;
import com.eqpos.eqentry.tools.Variables;

import java.util.ArrayList;
import java.util.HashMap;

public class StockTransferListActivity extends AppCompatActivity  implements View.OnClickListener {
    private SwipeMenuListView lsList;
    private Button btNew;

    private ArrayList<HashMap<String, String>> gList;

    private int _STOCKTRANSFER = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_transfer_list);
        this.setTitle(R.string.stocktransfers);

        lsList = (SwipeMenuListView) findViewById(R.id.ls_stocktransferlist_list);
        btNew = (Button) findViewById(R.id.bt_stocktransferlist_new);
        btNew.setOnClickListener(this);



        createSwipeListview();


        lsList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                int lId = Integer.parseInt(gList.get(position).get("id"));
                switch (index) {
                    case 0:
                        editTransfer(lId);
                        break;
                    case 1:
                        StockTransferDao.removeTransfer(lId);
                        getTransferList();
                        break;

                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
        getTransferList();
    }

    private void getTransferList() {
        gList = StockTransferDao.getTransferList();
        if (gList != null) {
            SimpleAdapter adp = new SimpleAdapter(this, gList, R.layout.stock_entrylist_row,
                    new String[]{ "documentnumber", "", "warehousename"},
                    new int[]{R.id.lbl_stockentrylistrow_date, R.id.lbl_stockentrylistrow_number,
                            R.id.lbl_stockentrylistrow_suppliername});
            lsList.setAdapter(adp);
        }
    }


    private void editTransfer(int transferId) {

        Intent newStockTransfer = new Intent(this, NewStockTransferActivity.class);
        newStockTransfer.putExtra("transferid", transferId);
        startActivityForResult(newStockTransfer, _STOCKTRANSFER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == _STOCKTRANSFER) {
            getTransferList();
        }
    }
    private void createSwipeListview() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                SwipeMenuItem editItem = new SwipeMenuItem(getApplicationContext());
                editItem.setBackground(R.color.colorWhite);
                editItem.setWidth(Variables.dp2px(90));
                editItem.setIcon(R.mipmap.img_edit);
                editItem.setTitle(R.string.editdelivery);
                editItem.setTitleSize(12);
                editItem.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(editItem);

                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(R.color.colorWhite);
                deleteItem.setWidth(Variables.dp2px(90));
                deleteItem.setIcon(R.mipmap.img_delete);
                deleteItem.setTitle(R.string.removetransfer);
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
            case R.id.bt_stocktransferlist_new:
                editTransfer(0);
                break;
        }
    }
}
