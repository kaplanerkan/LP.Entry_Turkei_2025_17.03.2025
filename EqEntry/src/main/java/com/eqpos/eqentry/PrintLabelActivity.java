package com.eqpos.eqentry;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
//import android.support.annotation.Nullable;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.eqpos.eqentry.adapters.ProductListAdapter;
import com.eqpos.eqentry.db.ProductDao;
import com.eqpos.eqentry.tools.CaptureActivityPortrait;
import com.eqpos.eqentry.tools.Variables;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import com.eqpos.eqentry.printing.PrintLabel;

public class PrintLabelActivity extends AppCompatActivity implements View.OnClickListener {

    private ArrayList<HashMap<String, String>> gProductList;
    private SwipeMenuListView lsList;
    private Button btFilter;
    private Button btBarcode;
    private EditText edSearch;
    private int lastPosition = 0;
    private int _CHANGEPRICE = 2000;
    private int _EDITPRODUCT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_label);
        this.setTitle(R.string.printlabel);

        btFilter = (Button) findViewById(R.id.bt_printlabel_search);
        btBarcode = (Button) findViewById(R.id.bt_printlabel_barcode);
        lsList = (SwipeMenuListView) findViewById(R.id.ls_printlabel_list);
        edSearch = (EditText) findViewById(R.id.ed_printlabel_search);

        btFilter.setOnClickListener(this);
        btBarcode.setOnClickListener(this);

        registerForContextMenu(lsList);
        lsList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                if (v.getId() == R.id.ls_printlabel_list) {
                    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

                    try {
                        PrintLabel.printLabel(
                                gProductList.get(info.position).get("productname"),
                                gProductList.get(info.position).get("newprice"),
                                gProductList.get(info.position).get("barcode"),
                                gProductList.get(info.position).get("unitamount"), //200
                                gProductList.get(info.position).get("amountunite")); // gr
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    int lProductId = Integer.parseInt(gProductList.get(info.position).get("id"));
                    int lPriceOrder = Integer.parseInt(gProductList.get(info.position).get("priceorder"));
                    removeFromLabelList(lProductId,lPriceOrder);

                    //menu.setHeaderTitle(gProductList.get(info.position).get("productname")); // Context Menünün başlığını seçilen verinin metini olarak ayarladık.

                    //if (Integer.parseInt(gProductList.get(info.position).get("ischangedprice")) == 1) {
                    //  menu.add(0, 0, 0, getString(R.string.cancelchangedprice));
                    //}

                    //menu.add(0, 1, 0, getString(R.string.changeprice));

                    //if (Integer.parseInt(gProductList.get(info.position).get("printlabel")) == 1) {
                    //  menu.add(0, 2, 0, getString(R.string.removeprintlabel));
                    //} else {
                    //  menu.add(0, 2, 0, getString(R.string.printlabel));
                    //}
                    //menu.add(0, 3, 0, getString(R.string.editproduct));
                }
            }
        });

        createSwipeListview();

        lsList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                int lProductId = Integer.parseInt(gProductList.get(position).get("id"));
                int lPriceOrder = Integer.parseInt(gProductList.get(position).get("priceorder"));
                boolean isChangedPrice = Integer.parseInt(gProductList.get(position).get("ischangedprice")) == 1;
                int lPrintLabel = Integer.parseInt(gProductList.get(position).get("printlabel"));
                lastPosition = position;
                switch (index) {
                    case 0:
                        //changeprice
                        changePrice(lProductId);
                        break;
                    case 1:
                        cancelChangedPrice(lProductId);
                        break;
                    case 2:
                        editProduct(lProductId);
                        break;
                    case 3:
                        removeFromLabelList(lProductId, lPriceOrder);
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        getProductList();
        listProducts();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && (requestCode == _CHANGEPRICE || requestCode == _EDITPRODUCT)) {
            getProductList();
            listProducts();
            lsList.setSelection(lastPosition);
        } else if (resultCode == RESULT_OK) {
            IntentResult scannResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            if (scannResult != null) {
                String contents = data.getStringExtra("SCAN_RESULT");
                edSearch.setText(contents);
                getProductList();
                listProducts();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_printlabel_barcode:
                openBarcode();
                break;

            case R.id.bt_printlabel_search:
                getProductList();
                listProducts();
                break;
        }
    }

    private void listProducts() {
        if (gProductList != null) {
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

    private void getProductList() {
        gProductList = ProductDao.getProductList(edSearch.getText().toString(), 0,"", "", "productname", "asc", " pr.printlabel=1");
    }


    private void editProduct(int productId) {
        Intent editProductIntent = new Intent(this, EditProductActivity.class);
        editProductIntent.putExtra("productId", productId);
        startActivityForResult(editProductIntent, _EDITPRODUCT);
    }

    private void changePrice(int productId) {
        Intent changePriceIntent = new Intent(this, ChangePriceActivity.class);
        changePriceIntent.putExtra("productId", productId);
        startActivityForResult(changePriceIntent, _CHANGEPRICE);
    }

    private void cancelChangedPrice(int productId) {
        ProductDao.cancelChangedPrice(productId);
        getProductList();
        listProducts();
        lsList.setSelection(lastPosition);
    }

    private void addToPrintLabel(int productId, int priceOrder) {
        ProductDao.addToLabelList(productId, priceOrder);
        getProductList();
        listProducts();
        lsList.setSelection(lastPosition);
    }

    private void removeFromLabelList(int productId, int priceOrder) {
        ProductDao.removeFromLabelList(productId, priceOrder);
        getProductList();
        listProducts();
        lsList.setSelection(lastPosition);
    }
    private void openBarcode() {
        IntentIntegrator scanIntegrator = new IntentIntegrator(PrintLabelActivity.this);
        scanIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
        scanIntegrator.setOrientationLocked(true);
        scanIntegrator.initiateScan();
    }

    public boolean onContextItemSelected(MenuItem item) {
        /*
        0 : changeprice
        1 : printlabel;
        2 : editproduct;
        */

        try {
            int lProductId = 0;// = Integer.parseInt(gProductList.get(lsList.getSelectedItemPosition()).get("id"));
            final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            int position = (int) info.id;
            lProductId = Integer.parseInt(gProductList.get(position).get("id"));
            boolean isChangedPrice = Integer.parseInt(gProductList.get(position).get("ischangedprice")) == 1;
            int lPrintLabel = Integer.parseInt(gProductList.get(position).get("printlabel"));
            int lPriceOrder = Integer.parseInt(gProductList.get(position).get("priceorder"));

            lastPosition = position;
            boolean result = true;
            switch (item.getItemId()) {
                case 0:
                    cancelChangedPrice(lProductId);
                    break;
                case 1:
                    changePrice(lProductId);
                    break;
                case 2:
                    //printlabel
                    if (lPrintLabel == 0) {
                        addToPrintLabel(lProductId, lPriceOrder);
                    } else {
                        removeFromLabelList(lProductId, lPriceOrder);
                    }
                    break;
                case 3:
                    // editProduct
                    editProduct(lProductId);
                    break;
                default:
                    result = false;
                    break;
            }
            return result;
        } catch (Exception e) {
            return false;
        }
    }

    private void createSwipeListview() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                //create changeprice item
                SwipeMenuItem changePriceItem = new SwipeMenuItem(getApplicationContext());
                changePriceItem.setBackground(R.color.colorWhite);
                changePriceItem.setWidth(Variables.dp2px(90));
                changePriceItem.setIcon(R.mipmap.img_changeprice);
                changePriceItem.setTitle(R.string.changeprice);
                changePriceItem.setTitleSize(12);
                changePriceItem.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(changePriceItem);


                //create cancelChangedPrice item
                SwipeMenuItem cancelChangedPriceItem = new SwipeMenuItem(getApplicationContext());
                cancelChangedPriceItem.setBackground(R.color.colorWhite);
                cancelChangedPriceItem.setWidth(Variables.dp2px(90));
                cancelChangedPriceItem.setIcon(R.mipmap.img_delete);
                cancelChangedPriceItem.setTitle(R.string.cancelchangedprice);
                cancelChangedPriceItem.setTitleSize(12);
                cancelChangedPriceItem.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(cancelChangedPriceItem);


                //create editProduct item
                SwipeMenuItem editProductItem = new SwipeMenuItem(getApplicationContext());
                editProductItem.setBackground(R.color.colorWhite);
                editProductItem.setWidth(Variables.dp2px(90));
                editProductItem.setIcon(R.mipmap.img_editproduct);
                editProductItem.setTitle(R.string.editproduct);
                editProductItem.setTitleSize(12);
                editProductItem.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(editProductItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                deleteItem.setBackground(R.color.colorWhite);
                deleteItem.setWidth(Variables.dp2px(90));
                deleteItem.setIcon(R.mipmap.img_removelabel);
                deleteItem.setTitle(R.string.cancel);
                deleteItem.setTitleSize(12);
                deleteItem.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(deleteItem);
            }
        };
        lsList.setMenuCreator(creator);
    }

}
