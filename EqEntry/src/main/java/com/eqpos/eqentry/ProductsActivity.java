package com.eqpos.eqentry;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.eqpos.eqentry.adapters.ProductListAdapter;
import com.eqpos.eqentry.db.CustomerDao;
import com.eqpos.eqentry.db.OrderDao;
import com.eqpos.eqentry.db.ProductDao;
import com.eqpos.eqentry.db.SendDao;
import com.eqpos.eqentry.tools.CaptureActivityPortrait;
import com.eqpos.eqentry.tools.Variables;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.eqpos.eqentry.printing.PrintLabel.*;

public class ProductsActivity extends AppCompatActivity implements View.OnClickListener, View.OnCreateContextMenuListener {
    private ArrayList<HashMap<String, String>> gProductList;
    private SwipeMenuListView lsList;
    private Button btFilter;
    private Button btOptions;
    private Button btBarcode;
    private Button btVarcode;
    private Button btNewProduct;
    private Button btSend;
    private EditText edSearch;

    private Spinner cmbFilterField; //FKILIC
    private List<String> gFilterList;

    private int _FILTERRESULT = 1000;
    private int _CHANGEPRICE = 2000;
    private int _EDITPRODUCT = 3000;
    private int _ADDTOORDERLIST = 4000;

    private String gFilterGroupName = "";
    private String gFilterTaxName = "";
    private String gSortField = "productname";
    private String gSortType = "asc";
    private int lastPosition = 0;
    private int filitretipi = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);
        this.setTitle(R.string.productlist);

        btFilter = (Button) findViewById(R.id.bt_products_search);
        btOptions = (Button) findViewById(R.id.bt_products_options);
        btBarcode = (Button) findViewById(R.id.bt_products_barcode);
        btVarcode = (Button) findViewById(R.id.bt_products_varcode);
        btNewProduct = (Button) findViewById(R.id.bt_products_newproduct);
        btSend = (Button) findViewById(R.id.bt_products_send);
        lsList = (SwipeMenuListView) findViewById(R.id.ls_products_list);
        edSearch = (EditText) findViewById(R.id.ed_products_search);
        //cmbFilterField = (Spinner) findViewById(R.id.cmb_filterfield); //FKILIC

        btOptions.setOnClickListener(this);
        btFilter.setOnClickListener(this);
        btBarcode.setOnClickListener(this);
        btVarcode.setOnClickListener(this);
        btNewProduct.setOnClickListener(this);
        btSend.setOnClickListener(this);


        /*cmbFilterField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });


        gFilterList = new ArrayList<String>(); //FKILIC
        gFilterList.add("All");                //FKILIC
        gFilterList.add("Barkod");             //FKILIC
        gFilterList.add("PLU");                //FKILIC
        gFilterList.add("VariantCode");        //FKILIC */

        registerForContextMenu(lsList);
        lsList.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                if (v.getId() == R.id.ls_products_list) {
                    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                    menu.setHeaderTitle(gProductList.get(info.position).get("productname")); // Context Menünün başlığını seçilen verinin metini olarak ayarladık.

                    if (Integer.parseInt(gProductList.get(info.position).get("ischangedprice")) == 1) {
                        menu.add(0, 0, 0, getString(R.string.cancelchangedprice));
                    } else {
                        menu.add(0, 0, 0, getString(R.string.changeprice));
                    }

                    if (Integer.parseInt(gProductList.get(info.position).get("printlabel")) == 1) {
                        menu.add(0, 1, 0, getString(R.string.removeprintlabel));
                    } else {
                        menu.add(0, 1, 0, getString(R.string.printlabel));
                    }
                    menu.add(0, 2, 0, getString(R.string.editproduct));
                    menu.add(0, 3, 0, getString(R.string.addtoorderlist));
                    menu.add(0, 4, 0, getString(R.string.print));
                    menu.add(0, 5, 0, getString(R.string.senddatas));
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
                        if (isChangedPrice) {
                            cancelChangedPrice(lProductId);
                        } else {
                            changePrice(lProductId);
                        }
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
                        //Edit Product
                        editProduct(lProductId);
                        break;

                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

      //  13.08.2024: Erkan  Otokatik olarak FilterActivity penceresinin acilmasini engelliyoruz
     //   btOptions.performClick();

        List<String> gGroupList = ProductDao.getGroupListForSpinner(getString(R.string.all));
        if (gGroupList.size() > 2) {
            gFilterGroupName =  gGroupList.get(1); // data.getStringExtra("groupname");
            //edSearch.setText(gFilterGroupName);
            gFilterTaxName = ""; // data.getStringExtra("taxname");
            gSortField = "productname"; // data.getStringExtra("sortfield");
            gSortType = "asc"; // data.getStringExtra("sorttype");
            this.getProductList(0);
            this.listProducts();
        }


//        edSearch.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//
//                    if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
//                            (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_SEARCH)) {
//                        boolean result = getProductList(1);
//                        edSearch.requestFocus();
//
//
//                    if(result) {
//                        //listProducts();
//                        btFilter.performClick();
//                        edSearch.requestFocus();
//                        return true;
//                    }
//                    else {
//                        addProductBarcode(edSearch.getText().toString());
//
//                    }
//
//                    }
//
//                return false;
//            }
//        });
//event.getKeyCode() == KeyEvent.KEYCODE_ENTER
        edSearch.setOnEditorActionListener((v, actionId, event) -> {
            // Android 13 de bu kisim calisiyor
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_ACTION_NEXT
                    || (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

//                if (actionId == EditorInfo.IME_ACTION_NEXT
//                        || event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                try {
                    boolean result = getProductList_Erkan(0);
                    edSearch.requestFocus();


                    if (result) {
                        //listProducts();
                        btFilter.performClick();
                        edSearch.setText("");
                        edSearch.requestFocus();
                        return true;
                    } else {
                        addProductBarcode(edSearch.getText().toString());

                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }


            }
            return true;
        });
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                edSearch.setText("");
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void listProducts() {
        if (gProductList != null) {
            ProductListAdapter adp = new ProductListAdapter(this, gProductList, R.layout.productlist_row,
                    new String[]{"productname", "barcode", "plu", "variantcode", "taxname", "groupname",
                            "costprice", "sellprice", "depositprice", "stock", "unitename"},
                    new int[]{R.id.lbl_row_products_productname, R.id.lbl_row_products_barcode,
                            R.id.lbl_row_products_plu, R.id.lbl_row_products_variantcode, R.id.lbl_row_products_taxname,
                            R.id.lbl_row_products_group, R.id.lbl_row_products_costprice,
                            R.id.lbl_row_products_sellprice, R.id.lbl_row_products_depositname,
                            R.id.lbl_row_products_stock, R.id.lbl_row_products_unitename});
            lsList.setAdapter(adp);

        }

    }

    private boolean getProductList(int filterType) {


        gProductList = ProductDao.getProductList(edSearch.getText().toString().trim(), filterType, gFilterGroupName,
                gFilterTaxName, gSortField, gSortType, "");

        this.filitretipi = 0;

       if(gProductList.isEmpty()) return false ;

       return true;
    }

    private boolean getProductList_Erkan(int filterType) {


        gProductList = ProductDao.getProductList_Erkan(edSearch.getText().toString().trim(), filterType, gFilterGroupName,
                gFilterTaxName, gSortField, gSortType, "");

        this.filitretipi = 0;

        if(gProductList.isEmpty()) return false ;

        return true;
    }



    private void editProduct(int productId) {
        /* productId 0 (sıfır) ise yeni ürün kayit işlemi */
        Intent editProductIntent = new Intent(this, EditProductActivity.class);
        editProductIntent.putExtra("productId", productId);
        startActivityForResult(editProductIntent, _EDITPRODUCT);
    }

    private void addProductBarcode(String barcode) {
        /* productId 0 (sıfır) ise yeni ürün kayit işlemi */
        Intent editProductIntent = new Intent(this, EditProductActivity.class);
        editProductIntent.putExtra("barcode", barcode);
        startActivityForResult(editProductIntent, _EDITPRODUCT);
    }

    private void changePrice(int productId) {
        Intent changePriceIntent = new Intent(this, ChangePriceActivity.class);
        changePriceIntent.putExtra("productId", productId);
        startActivityForResult(changePriceIntent, _CHANGEPRICE);
    }

    private void cancelChangedPrice(int productId) {
        ProductDao.cancelChangedPrice(productId);
        getProductList(0);
        listProducts();
        lsList.setSelection(lastPosition);
    }

    private void addToPrintLabel(int productId, int priceOrder) {
        ProductDao.addToLabelList(productId, priceOrder);
        getProductList(0);
        listProducts();
        lsList.setSelection(lastPosition);
    }

    private void removeFromLabelList(int productId, int priceOrder) {
        ProductDao.removeFromLabelList(productId,priceOrder);
        getProductList(0);
        listProducts();
        lsList.setSelection(lastPosition);
    }

    private void printLabelDirectly(String productname,String price,String barcode, String unitamount, String amountunite) throws ParseException {
        printLabel(productname, price, barcode, unitamount, amountunite );
        getProductList(0);
        listProducts();
        lsList.setSelection(lastPosition);
    }

    private void sendProduct() {
        if (ProductDao.isThereNewOrUpdatedProduct())
            SendDao.sendProducts();

        SendDao.sendChangedPrices();
        //lsList.setSelection(lastPosition);
    }


    private void addToOrderList(int productId, String productName, String unitname) {
        Intent getAmountIntent = new Intent(this, GetAmountActivity.class);
        getAmountIntent.putExtra("productId", productId);
        getAmountIntent.putExtra("productname", productName);
        getAmountIntent.putExtra("unitename", unitname);
        startActivityForResult(getAmountIntent, _ADDTOORDERLIST);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_products_options:
                Intent filterIntent = new Intent(this, FilterActivity.class);
                startActivityForResult(filterIntent, _FILTERRESULT);
                break;
            case R.id.bt_products_search:
                // 22.08.2024: Ilk vErsion
                //this.getProductList(this.filitretipi);
                try {
                    getProductList_Erkan(0);
                    this.listProducts();
                }catch (Exception ex){
                    Log.e("Error", ex.getMessage());
                }


                break;
            case R.id.bt_products_barcode:
                this.filitretipi = 1;
                openBarcode();
                break;

            case R.id.bt_products_send:
                if (ProductDao.isThereNewOrUpdatedProduct())
                    SendDao.sendProducts();
                SendDao.sendChangedPrices();
                if(!isFinishing())
                {
                    Toast.makeText(ProductsActivity.this, R.string.update_was_done, Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.bt_products_varcode:
                this.filitretipi = 3;
                openBarcode();
                break;
            case R.id.bt_products_newproduct:
                editProduct(0);
                break;
        }
    }

    @Override
    public void onBackPressed() {

        Intent siparis = new Intent(this, MainMenu.class);
        startActivity(siparis);
        finish();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

            }
        }, 2000);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == _FILTERRESULT) {
            if (data != null) {
                gFilterGroupName = data.getStringExtra("groupname");
                gFilterTaxName = data.getStringExtra("taxname");
                gSortField = data.getStringExtra("sortfield");
                gSortType = data.getStringExtra("sorttype");
                this.getProductList(0);
                this.listProducts();
            }
        } else if (resultCode == RESULT_OK && (requestCode == _CHANGEPRICE || requestCode == _EDITPRODUCT)) {
            getProductList(0);
            listProducts();
            lsList.setSelection(lastPosition);
        } else if (resultCode == RESULT_OK && requestCode == _ADDTOORDERLIST) {

            Double lAmount = data.getDoubleExtra("amount", 1);
            int lProductId = data.getIntExtra("productid", 0);
            String lUnit = data.getStringExtra("unitename");

            OrderDao.addProductToOrder(CustomerDao.getProductSupplierId(lProductId), lProductId, lAmount, lUnit);
            Toast.makeText(this, getString(R.string.msg_added_to_list), Toast.LENGTH_LONG).show();

        } else if (resultCode == RESULT_OK) {
            IntentResult scannResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            if (scannResult != null) {
                String contents = data.getStringExtra("SCAN_RESULT");
                edSearch.setText(contents);
                getProductList(this.filitretipi);

                boolean result =  this.getProductList(this.filitretipi);
                if(!result){
                    addProductBarcode(edSearch.getText().toString());
                }else {
                    listProducts();
                }
            }
        }
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

                    if (isChangedPrice) {
                        cancelChangedPrice(lProductId);
                    } else {
                        changePrice(lProductId);
                    }

                    break;
                case 1:
                    //printlabel
                    if (lPrintLabel == 0) {
                        addToPrintLabel(lProductId, lPriceOrder);
                    } else {
                        removeFromLabelList(lProductId, lPriceOrder);
                    }
                    break;
                case 2:
                    //editproduct
                    editProduct(lProductId);
                    break;
                case 3:
                    //addtoorderlist
                    addToOrderList(lProductId, gProductList.get(position).get("productname"),
                            gProductList.get(position).get("unitename"));
                    break;

                case 4:
                    //print Label  Directly
                    printLabelDirectly(
                            gProductList.get(position).get("productname"),
                            gProductList.get(position).get("newprice"),
                            gProductList.get(position).get("barcode"),
                            gProductList.get(position).get("unitamount"), //200
                            gProductList.get(position).get("amountunite")
                    );
                    break;

                case 5:
                    sendProduct();
                    if(!isFinishing())
                    {
                        Toast.makeText(ProductsActivity.this, R.string.update_was_done, Toast.LENGTH_SHORT).show();
                    }
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


    private void openBarcode() {
        IntentIntegrator scanIntegrator = new IntentIntegrator(ProductsActivity.this);
        scanIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
        scanIntegrator.setOrientationLocked(true);
        scanIntegrator.initiateScan();
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

                SwipeMenuItem editProductItem = new SwipeMenuItem((getApplicationContext()));
                editProductItem.setBackground(R.color.colorWhite);
                editProductItem.setWidth(Variables.dp2px(90));
                editProductItem.setIcon(R.mipmap.img_editproduct);
                editProductItem.setTitle(R.string.editproduct);
                editProductItem.setTitleSize(12);
                editProductItem.setTitleColor(R.color.colorBlack);
                menu.addMenuItem(editProductItem);
            }
        };
        lsList.setMenuCreator(creator);
    }
}
