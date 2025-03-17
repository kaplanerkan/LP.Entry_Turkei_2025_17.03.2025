package com.eqpos.eqentry;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.eqpos.eqentry.Adapters.ProductListAdapter;
import com.eqpos.eqentry.DB.ProductDao;
import com.eqpos.eqentry.Models.Product;
import com.eqpos.eqentry.tools.CaptureActivityPortrait;
import com.eqpos.eqentry.tools.Variables;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

public class ChangePriceActivity extends AppCompatActivity implements View.OnClickListener, View.OnCreateContextMenuListener {
    private ArrayList<HashMap<String, String>> gProductList;
    private SwipeMenuListView lsList;
    private Button btChange;
    private Button btFind;
    private Button btBarcode;
    private TextView lblProductName;
    private TextView lblOldPrice;
    private EditText edNewPrice;
    private EditText edFind;
    private EditText edSearch;

    private String gFilterGroupName = "";
    private String gFilterTaxName = "";
    private String gSortField = "productname";
    private String gSortType = "asc";

    private int lastPosition;
    private int _EDITPRODUCT = 3000;

    boolean isAutoClose = false;
    int gProductId;
    Product gProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_price);

        this.setTitle(R.string.changeprice);

        gProductId = 0;
        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            gProductId = extra.getInt("productId");

            if (gProductId > 0) {
                isAutoClose = true;
                gProduct = ProductDao.getProduct(gProductId, "");
            }
        }

        edNewPrice = (EditText) findViewById(R.id.ed_changeprice_newprice);
        edFind = (EditText) findViewById(R.id.ed_changeprice_find);
        btFind = (Button) findViewById(R.id.bt_changeprice_find);
        btBarcode = (Button) findViewById(R.id.bt_changeprice_barcode);
        btChange = (Button) findViewById(R.id.bt_changeprice_change);
        lblProductName = (TextView) findViewById(R.id.lbl_changeprice_productname);
        lblOldPrice = (TextView) findViewById(R.id.lbl_changeprice_currentprice);
        lsList = (SwipeMenuListView) findViewById(R.id.ls_changeprices_list);
        edSearch = (EditText) findViewById(R.id.ed_changeprice_search);

        lblProductName.setText("");
        lblOldPrice.setText("");

        btFind.setOnClickListener(this);
        btChange.setOnClickListener(this);
        btBarcode.setOnClickListener(this);
        if (gProductId > 0) {
            this.getProduct();
        } else {
            clearFields();
        }


        registerForContextMenu(lsList);
        lsList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                if (v.getId() == R.id.ls_changeprices_list) {
                    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                    menu.setHeaderTitle(gProductList.get(info.position).get("productname")); // Context Menünün başlığını seçilen verinin metini olarak ayarladık.

                    menu.add(0, 0, 0, getString(R.string.cancelchangedprice));
                    menu.add(0, 1, 0, getString(R.string.changeprice));

                    if (Integer.parseInt(gProductList.get(info.position).get("printlabel")) == 1) {
                        menu.add(0, 2, 0, getString(R.string.removeprintlabel));
                    } else {
                        menu.add(0, 2, 0, getString(R.string.printlabel));
                    }
                    menu.add(0, 3, 0, getString(R.string.editproduct));
                }
            }
        });
        createSwipeListview();

        lsList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                int lProductId = Integer.parseInt(gProductList.get(position).get("id"));
                boolean isChangedPrice = Integer.parseInt(gProductList.get(position).get("ischangedprice")) == 1;
                int lPrintLabel = Integer.parseInt(gProductList.get(position).get("printlabel"));
                int lPriceOrder = Integer.parseInt(gProductList.get(position).get("priceorder"));
                lastPosition = position;
                switch (index) {
                    case 0:
                        //changeprice
                        changePrice(lProductId);
                        break;
                    case 1:
                        //PrintLabel
                        if (lPrintLabel == 1) {
                            removeFromLabelList(lProductId, lPriceOrder);
                        } else {
                            addToPrintLabel(lProductId, lPriceOrder);
                        }
                        break;
                    case 2:
                        editProduct(lProductId);
                        break;
                    case 3:
                        cancelChangedPrice(lProductId);
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });


        edFind.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    btFind.performClick();
                    return true;
                }
                return false;
            }
        });

        getProductList();
        listProducts();
    }

    private void clearFields() {
        gProductId = 0;
        gProduct = null;
        lblProductName.setText("");
        lblOldPrice.setText("");
        edNewPrice.setText("");
        edFind.performClick();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK && requestCode == _EDITPRODUCT) {
            getProductList();
            listProducts();
            lsList.setSelection(lastPosition);
        } else if (resultCode == RESULT_OK) {
            IntentResult scannResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            if (scannResult != null) {
                String contents = data.getStringExtra("SCAN_RESULT");
                edFind.setText(contents);
                getProduct();
            }
        }
    }

    private void getProduct() {
        gProduct = ProductDao.getProduct(gProductId, edFind.getText().toString());
        if (gProduct.getId() > 0) {
            lblProductName.setText(gProduct.getProductName());
            try {
                lblOldPrice.setText(Variables.doubleToStr(gProduct.getPrice(),2));
            } catch (Exception e) {
                lblOldPrice.setText("");
            }
            try {
                edNewPrice.setText(Variables.doubleToStr(gProduct.getNewPrice(),2));
            } catch (Exception e) {
                edNewPrice.setText("");
            }
            edNewPrice.performClick();
            edNewPrice.selectAll();
        } else {
            lblProductName.setText("");
            lblOldPrice.setText("");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.error_couldnot_find_product));
            builder.setCancelable(true);
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    private void openBarcode() {
        IntentIntegrator scanIntegrator = new IntentIntegrator(ChangePriceActivity.this);
        scanIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
        scanIntegrator.setOrientationLocked(true);
        scanIntegrator.initiateScan();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_changeprice_find:
                getProduct();
                break;
            case R.id.bt_changeprice_change:
                if (gProduct.getId() > 0) {
                    Double newPrice = 0.0;
                    try {
                        newPrice = Variables.strToDouble(edNewPrice.getText().toString());
                        ProductDao.changePrice(gProduct.getId(), 1, Variables.strToDouble((edNewPrice.getText().toString())));
                        if (isAutoClose) {
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            clearFields();
                            getProductList();
                            listProducts();
                        }
                    } catch (Exception e) {
                        Log.d("Change Price", e.getMessage());
                    }
                }
                break;
            case R.id.bt_changeprice_barcode:
                openBarcode();
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
        gProductList = ProductDao.getProductList(edSearch.getText().toString(), 0, gFilterGroupName, gFilterTaxName, gSortField, gSortType, " pr.price<>pr.newprice and newprice>0");
    }

    private void changePrice(int productId) {
        gProductId = productId;
        getProduct();
    }

    private void editProduct(int productId) {
        Intent editProductIntent = new Intent(this, EditProductActivity.class);
        editProductIntent.putExtra("productId", productId);
        startActivityForResult(editProductIntent, _EDITPRODUCT);
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
        ProductDao.removeFromLabelList(productId,priceOrder);
        getProductList();
        listProducts();
        lsList.setSelection(lastPosition);
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
                    //editproduct
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


                //create printlabel item
                SwipeMenuItem printLabelItem = new SwipeMenuItem(getApplicationContext());
                printLabelItem.setBackground(R.color.colorWhite);
                printLabelItem.setWidth(Variables.dp2px(90));
                printLabelItem.setIcon(R.mipmap.img_addlabel);
                printLabelItem.setTitle(R.string.printlabel);
                printLabelItem.setTitleSize(12);
                printLabelItem.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(printLabelItem);

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
                deleteItem.setIcon(R.mipmap.img_delete);
                deleteItem.setTitleSize(12);
                deleteItem.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(deleteItem);
            }
        };
        lsList.setMenuCreator(creator);
    }
}
