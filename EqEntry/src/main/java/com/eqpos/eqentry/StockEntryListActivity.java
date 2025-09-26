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
import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.eqpos.eqentry.databinding.ActivityStockEntryListBinding;
import com.eqpos.eqentry.db.StockEntryDao;
import com.eqpos.eqentry.db.WarehouseDao;
import com.eqpos.eqentry.models.DepoModel;
import com.eqpos.eqentry.printing.PrintStockEntry;
import com.eqpos.eqentry.tools.SharedPrefUtil;
import com.eqpos.eqentry.tools.Variables;
import com.eqpos.eqentry.views.depo_secimi.DepoDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StockEntryListActivity extends AppCompatActivity {

    private final int _STOCKENTRY = 5000;
    private ArrayList<HashMap<String, String>> gList;
    private int lastPosition;

    private ActivityStockEntryListBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_stock_entry_list);
        binding = ActivityStockEntryListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setTitle(R.string.stockentry);


        createSwipeListview();

        binding.lsStockentrylistList.setOnMenuItemClickListener((position, menu, index) -> {
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
        });

        getDeliveryList();

        initViews();


    }

    private void initViews() {
        binding.btnStockentrylistNew.setOnClickListener(v -> {

            // Depo listesini hazırla (örnek veri, sen DB'den çek)
            List<DepoModel> depoList = WarehouseDao.getAllWarehouses();

            if (depoList.size() == 1) {
                SharedPrefUtil.putInt(SharedPrefUtil.KEY_SELECTED_DEPO_ID, depoList.get(0).getId());
                editDelivery(0);
            } else if (depoList.size() > 1) {
                // Dialog'ı aç ve depo listesini geçir
                DepoDialogFragment dialog = new DepoDialogFragment((depoId, depoIsmi) -> {
                    Log.e("DepoDialog", "Seçilen Depo ID: " + depoId + ", Depo İsmi: " + depoIsmi);
                    SharedPrefUtil.putInt(SharedPrefUtil.KEY_SELECTED_DEPO_ID, depoId );
                    editDelivery(0);
                }, depoList);
                dialog.setCancelable(false);
                dialog.show(getSupportFragmentManager(), "depo_dialog");
            }else {
                Toast.makeText(StockEntryListActivity.this, R.string.depo_bulunamadi, Toast.LENGTH_SHORT).show();
            }
        });
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
            binding.lsStockentrylistList.setAdapter(adp);
            binding.lsStockentrylistList.setSelection(lastPosition);
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
        binding.lsStockentrylistList.setMenuCreator(creator);
    }


}
