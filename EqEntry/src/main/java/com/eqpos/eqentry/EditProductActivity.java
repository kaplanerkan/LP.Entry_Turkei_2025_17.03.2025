package com.eqpos.eqentry;

import static com.eqpos.eqentry.printing.PrintLabel.printLabel;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eqpos.eqentry.databinding.ActivityEditProductBinding;
import com.eqpos.eqentry.databinding.DialogEditBarcodePluBinding;
import com.eqpos.eqentry.db.ProductDao;
import com.eqpos.eqentry.db.SendDao;
import com.eqpos.eqentry.models.AddedVaryantsModel;
import com.eqpos.eqentry.models.Product;
import com.eqpos.eqentry.tools.CaptureActivityPortrait;
import com.eqpos.eqentry.tools.LoadingDialog;
import com.eqpos.eqentry.tools.SharedPrefUtil;
import com.eqpos.eqentry.tools.Variables;
import com.eqpos.eqentry.views.varyants_add_to_product.OnVaryantSilClickListener;
import com.eqpos.eqentry.views.varyants_add_to_product.VaryantEkleMainListAdapter;
import com.eqpos.eqentry.views.varyants_add_to_product.VaryantEklenenlerViewModel;
import com.eqpos.eqentry.views.varyants_add_to_product.VaryantsEkle;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditProductActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "EditProductActivity";
    private List<String> gTaxList;
    private List<String> gUnitList;
    private List<String> gDepositList;
    private List<String> gGroupList;
    private Button btKaydet;
    private Button btSend;
    private Button btPrint;
    private Button btBarcode;
    private Product gProduct;
    private int gProductId;
    private String barcode;
    private String varyanlarIcinSatisFiyati;

    private VaryantEklenenlerViewModel viewModel;
    private VaryantEkleMainListAdapter varyantEkleMainListAdapter;

    private ActivityEditProductBinding binding;
    private LoadingDialog loadingDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_edit_product);
        binding = ActivityEditProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        this.setTitle(R.string.editproduct);
        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            gProductId = extra.getInt("productId");
            barcode = extra.getString("barcode", "");
        } else {
            finish();
        }

        if (gProductId > 0)
            gProduct = ProductDao.getProduct(gProductId, "");
        else
            gProduct = new Product();


        btKaydet = (Button) findViewById(R.id.bt_editproduct_save);
        btSend = (Button) findViewById(R.id.bt_editproduct_save_and_send);
        btPrint = (Button) findViewById(R.id.bt_printproduct);
        btBarcode = (Button) findViewById(R.id.bt_editproduct_barcode);


        btKaydet.setOnClickListener(this);
        btSend.setOnClickListener(this);
        btPrint.setOnClickListener(this);
        btBarcode.setOnClickListener(this);


        listFields();

        binding.edEditproductProductname.setText(gProduct.getProductName());
        binding.edEditproductPlu.setText(gProduct.getPlu());
        binding.edEditproductOrigin.setText(gProduct.getOrigin());
        binding.edEditproductBarcode.setText(gProduct.getBarcode());
        if (!barcode.equals(""))
            binding.edEditproductBarcode.setText(barcode);

        binding.edEditproductCostprice.setText(Variables.doubleToStr(gProduct.getCostPrice(), 2));


        try {
            if (Variables.strToDouble((binding.edEditproductCostprice.getText().toString())) == 0.0) {
                binding.edEditproductCostprice.setText(null);
                binding.edEditproductCostprice.setHint(null);
            }
        } catch (ParseException e) {
            Log.e(TAG, "onCreate: 1 " + e.getMessage());
        }
        if (gProduct.getNewPrice().equals("")) {
            binding.edEditproductSellprice.setText(Variables.doubleToStr(gProduct.getPrice(), 2));
            try {
                if (Variables.strToDouble((binding.edEditproductSellprice.getText().toString())) == 0.0) {
                    binding.edEditproductSellprice.setText(null);
                    binding.edEditproductSellprice.setHint(null);
                }
            } catch (ParseException e) {
                Log.e(TAG, "onCreate: " + e.getMessage());
            }
        } else {
            binding.edEditproductSellprice.setText(Variables.doubleToStr(gProduct.getNewPrice(), 2));
            try {
                if (Variables.strToDouble((binding.edEditproductSellprice.getText().toString())) == 0.0) {
                    binding.edEditproductSellprice.setText(null);
                    binding.edEditproductSellprice.setHint(null);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        binding.edEditproductDescription.setText(gProduct.getDescription());
        binding.cmbEditproductUnitname.setSelection(gUnitList.indexOf(gProduct.getUniteName()));
        binding.cmbEditproductTaxname.setSelection(gTaxList.indexOf(gProduct.getTaxName()));
        binding.cmbEditproductGroupname.setSelection(gGroupList.indexOf(gProduct.getGroupName()));
        binding.cmbEditproductDeposite.setSelection(gDepositList.indexOf(gProduct.getDepositeName()));
        binding.cmbEditproductGramaj.setSelection(((ArrayAdapter<String>) binding.cmbEditproductGramaj.getAdapter()).getPosition(gProduct.getAmountUnit()));
        binding.edEditproductGramaj.setText(Variables.doubleToStr(gProduct.getUnitAmount(), 0));
        try {
            if (Variables.strToDouble((binding.edEditproductGramaj.getText().toString())) == 0.0) {
                binding.edEditproductGramaj.setText(null);
                binding.edEditproductGramaj.setHint(null);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


        btnAddVaryantsClickOlaylari();
    }

    /**
     * Add Varyants Button Click Events
     */
    private void btnAddVaryantsClickOlaylari() {


        initVaryantRecyclerView();

        binding.btnAddVaryants.setOnClickListener(view -> {
            if (binding.edEditproductProductname.getText().toString().isEmpty() || binding.cmbEditproductGroupname.getSelectedItemPosition() <= 0 ||
                    binding.cmbEditproductTaxname.getSelectedItemPosition() <= 0 || binding.cmbEditproductUnitname.getSelectedItemPosition() <= 0) {
                Toast.makeText(this, R.string.msg_dont_empty_name_group_tax, Toast.LENGTH_LONG).show();

            } else {

                varyanlarIcinSatisFiyati = binding.edEditproductSellprice.getText().toString().replace(",", ".");
                if (varyanlarIcinSatisFiyati.isEmpty()) {
                    SharedPrefUtil.putString(SharedPrefUtil.KEY_VARYANT_SATIS_FIYATI, "0.00");
                } else {
                    SharedPrefUtil.putString(SharedPrefUtil.KEY_VARYANT_SATIS_FIYATI, varyanlarIcinSatisFiyati);
                }

                Intent intent = new Intent(EditProductActivity.this, VaryantsEkle.class);
                // Bundle oluştur ve verileri ekle
                Bundle bundle = new Bundle();
                bundle.putInt("id", 0);
                bundle.putString("urunadi", binding.edEditproductProductname.getText().toString());
//                bundle.putInt("sira", varyant.getSira());
//                bundle.putString("tanim", varyant.getTanim());
//                bundle.putInt("rowcell", varyant.getRowcell());
//                bundle.putString("aciklama", varyant.getAciklama());
//                bundle.putInt("parentid", varyant.getParentid());
                // Bundle'ı Intent'e ekle
                intent.putExtras(bundle);
                // İkinci Activity'yi başlat
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Lifecycle :: EditProductActivity: onResume called");
        viewModel = null;

        initVaryantRecyclerView();
    }

    private void initVaryantRecyclerView() {
        try {
            viewModel = new ViewModelProvider(this).get(VaryantEklenenlerViewModel.class);

            // Secileni Silme/EDIT Listener'ini ayarla
            OnVaryantSilClickListener listener = new OnVaryantSilClickListener() {
                @Override
                public void onVaryantSil(AddedVaryantsModel varyant) {
                    // Sil butonuna tıklandığında
                    silmeIslemi(varyant);
                }

                @Override
                public void onEditVaryant(AddedVaryantsModel varyant) {
                    // Edit butonuna tıklandığında
                    // Burada edit işlemini yapın
                    showEditBarcodePluDialog(varyant);
                }
            };

            // Adapter'ı oluşturun
            varyantEkleMainListAdapter = new VaryantEkleMainListAdapter(
                    EditProductActivity.this,
                    new ArrayList<>(),
                    listener
            );

            binding.rvVaryantsListesi.setHasFixedSize(true);
            binding.rvVaryantsListesi.setLayoutManager(new LinearLayoutManager(this));
            binding.rvVaryantsListesi.setAdapter(varyantEkleMainListAdapter);


            // LiveData'yı gözlemle
            viewModel.getVaryantsAddedLiveData().observe(this, varyantList -> {
                varyantEkleMainListAdapter.updateList(varyantList);
                Log.d("Varyants", "Liste güncellendi, boyut: " + varyantList.size());
            });
        } catch (Exception ex) {
            Log.e("EditProductActivity", "initVaryantRecyclerView: " + ex.getMessage());
        }

    }

    /**
     * Varyant Silme İşlemi
     */
    private void silmeIslemi(AddedVaryantsModel varyant) {
        try {
            // AlertDialog oluşturma
            AlertDialog.Builder builder = new AlertDialog.Builder(EditProductActivity.this);
            builder.setTitle("Emin misiniz?");
            builder.setMessage(varyant.getUrunadi() + "\n\nBu varyantı silmek istediğinize emin misiniz?");
            builder.setPositiveButton("EVET SİL", (dialog, which) -> {
                viewModel.removeSelectedVaryant(varyant.getUrunadi());
            });
            // Hayır butonu
            builder.setNegativeButton("VAZGEÇ", (dialog, which) -> {
                dialog.dismiss(); // Diyalog kutusunu kapat
            });

            // İptal edilebilir mi?
            builder.setCancelable(false); // Dışarı tıklayınca kapanmasını engeller

            // AlertDialog'u göster
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } catch (Exception ex) {
            Log.e(TAG, "silmeIslemi: " + ex.getMessage());
        }

    }

    /**
     * Varyant Edit İşlemi
     */
    private void showEditBarcodePluDialog(AddedVaryantsModel varyant) {
        // ViewBinding ile dialog layout'u inflate et
        DialogEditBarcodePluBinding bindingPopup = DialogEditBarcodePluBinding.inflate(LayoutInflater.from(this));

        // Mevcut değerleri set et
        bindingPopup.tvPopupTitle.setText(varyant.getUrunadi());
        bindingPopup.etPopupBarcode.setText(varyant.getBarcode());
        bindingPopup.etPopupPlu.setText(String.valueOf(varyant.getPlu()));


        // Dialog oluştur
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(bindingPopup.getRoot())
                .setCancelable(false)
                .create();

        // Vazgeç butonu
        bindingPopup.btnPopupCancel.setOnClickListener(v -> dialog.dismiss());

        // Kaydet butonu
        bindingPopup.btnPopupSave.setOnClickListener(v -> {
            String newBarcode = bindingPopup.etPopupBarcode.getText().toString().trim();
            String newPluStr = bindingPopup.etPopupPlu.getText().toString().trim();
//
            // Validasyon
            if (newBarcode.isEmpty()) {
                newBarcode = "0";
            }

            if (newPluStr.isEmpty()) {
                newPluStr = "0";
            }

            try {
                int newPlu = Integer.parseInt(newPluStr);

                // ViewModel üzerinden güncelleme yap
                viewModel.updateVaryant(
                        varyant.getUrunadi(),
                        newBarcode,
                        newPlu
                );

                Toast.makeText(EditProductActivity.this, "Değişiklikler kaydedildi", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Geçerli bir PLU numarası girin", Toast.LENGTH_SHORT).show();
            }
        });

        // Dialogu göster
        dialog.show();

        // Dialog boyutunu ayarla
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            // Arka planı şeffaf yaparak köşeleri yuvarlak gösterebilirsiniz
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    /**
     * END: Add Varyants Button Click Events
     */


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            IntentResult scannResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

            if (scannResult != null) {
                String contents = data.getStringExtra("SCAN_RESULT");
                binding.edEditproductBarcode.setText(contents);
                binding.edEditproductProductname.performClick();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_editproduct_save:

                boolean isOncedenKayitliUrunmu = urunOncedenKayitliUrunmu();
                if (isOncedenKayitliUrunmu) {
                    Toast.makeText(EditProductActivity.this, R.string.product_already_exists, Toast.LENGTH_SHORT).show();
                } else {
                    saveProduct();
                }


                break;

            case R.id.bt_printproduct:
                try {
                    printLabel(gProduct.getProductName(),
                            gProduct.getPrice().toString(),
                            gProduct.getBarcode(),
                            gProduct.getUnitAmount().toString(),
                            gProduct.getAmountUnit());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //finish();
                break;

            case R.id.bt_editproduct_barcode:
                openBarcode();
                break;
            case R.id.bt_editproduct_save_and_send:
                Log.e(TAG, "onClick: 1");
                //saveProduct();
                // LoadingDialog'u göster


                loadingDialog = new LoadingDialog();
                loadingDialog.showLoading(EditProductActivity.this, "Checking Server");

                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    runOnUiThread(() -> {

                        boolean isOncedenKayitliUrunmu2 = urunOncedenKayitliUrunmu();
                        if (isOncedenKayitliUrunmu2) {
                            Toast.makeText(EditProductActivity.this, R.string.product_already_exists, Toast.LENGTH_SHORT).show();
                        } else {


                            // ana Product
                            saveProduct();
                            if (ProductDao.isThereNewOrUpdatedProduct())
                                SendDao.sendProducts();
                            SendDao.sendChangedPrices();

                            // Varyantlı Ürünler
                            List<AddedVaryantsModel> varyants = viewModel.getVaryantsAddedLiveData().getValue();
                            if (varyants != null && !varyants.isEmpty()) {
                                if (saveProductVaryants()) {         // VARYANTLARI KAYDET basarili ise
                                    //TODO: Kaydetme başarılı sil

                                } else {
                                    Toast.makeText(EditProductActivity.this, "Varyant kaydedilemedi!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }

                            if (!isFinishing()) {
                                Toast.makeText(EditProductActivity.this, R.string.update_was_done, Toast.LENGTH_SHORT).show();
                            }

                            if (varyantEkleMainListAdapter.getItemCount() > 0) {
                                Log.e(TAG, "onClick: Varyantlı Ürün Kaydedildi");
                            }
                            finish();


                        }
                    });
                });


                break;
        }
    }


    private boolean urunOncedenKayitliUrunmu() {
        String barcode = binding.edEditproductBarcode.getText().toString();
        String urunAdi = binding.edEditproductProductname.getText().toString();

        if (binding.edEditproductProductname.getText().toString().isEmpty() || binding.cmbEditproductGroupname.getSelectedItemPosition() <= 0 ||
                binding.cmbEditproductTaxname.getSelectedItemPosition() <= 0 || binding.cmbEditproductUnitname.getSelectedItemPosition() <= 0) {
            Toast.makeText(this, R.string.msg_dont_empty_name_group_tax, Toast.LENGTH_LONG).show();

            return false;
        }

        return ProductDao.checUrunMevcutmu(barcode, urunAdi);
    }


    private void saveProduct() {
        if (binding.edEditproductProductname.getText().toString().isEmpty() || binding.cmbEditproductGroupname.getSelectedItemPosition() <= 0 ||
                binding.cmbEditproductTaxname.getSelectedItemPosition() <= 0 || binding.cmbEditproductUnitname.getSelectedItemPosition() <= 0) {
            Toast.makeText(this, R.string.msg_dont_empty_name_group_tax, Toast.LENGTH_LONG).show();

            return;
        }

//        btPrint.setVisibility(View.VISIBLE);
//        btSend.setVisibility(View.VISIBLE);
//        btKaydet.setVisibility(View.INVISIBLE);


        gProduct.setId(gProductId);
        gProduct.setUniteName(binding.cmbEditproductUnitname.getSelectedItem().toString());
        gProduct.setUnitId(ProductDao.getUnitId(binding.cmbEditproductUnitname.getSelectedItem().toString()));
        if (binding.cmbEditproductDeposite.getSelectedItemPosition() >= 0) {
            gProduct.setDepositeName(binding.cmbEditproductDeposite.getSelectedItem().toString());
            gProduct.setDepositId(ProductDao.getDepositId(binding.cmbEditproductDeposite.getSelectedItem().toString()));
        } else {
            gProduct.setDepositeName("");
            gProduct.setDepositId(-1);
        }
        gProduct.setGroupName(binding.cmbEditproductGroupname.getSelectedItem().toString());
        gProduct.setGroupId(ProductDao.getGroupId(binding.cmbEditproductGroupname.getSelectedItem().toString()));
        gProduct.setTaxName(binding.cmbEditproductTaxname.getSelectedItem().toString());
        gProduct.setTaxId(ProductDao.getTaxId(binding.cmbEditproductTaxname.getSelectedItem().toString()));
        gProduct.setProductName(binding.edEditproductProductname.getText().toString());
        gProduct.setBarcode(binding.edEditproductBarcode.getText().toString());
        gProduct.setPlu(binding.edEditproductPlu.getText().toString());
        gProduct.setOrigin(binding.edEditproductOrigin.getText().toString());
        gProduct.setDescription(binding.edEditproductDescription.getText().toString());
        gProduct.setAmountUnit(binding.cmbEditproductGramaj.getSelectedItem().toString());

        if (binding.edEditproductCostprice.getText().toString() != "")
            try {
                gProduct.setCostPrice(Variables.strToDouble((binding.edEditproductCostprice.getText().toString())));
            } catch (ParseException e) {
                gProduct.setCostPrice(0.0);
                Log.e(TAG, "saveProduct: 0 " + e.getMessage());
            }
        else
            gProduct.setCostPrice(0.0);

        if (binding.edEditproductSellprice.getText().toString() != "")
            try {
                String sellprice = binding.edEditproductSellprice.getText().toString();
                gProduct.setPrice(Variables.strToDouble(sellprice));
            } catch (ParseException e) {
                gProduct.setPrice(0.0);
                Log.e(TAG, "saveProduct: 1 " + e.getMessage());
            }
        else
            gProduct.setPrice(0.0);

        if (binding.edEditproductGramaj.getText().toString() != "")
            try {
                gProduct.setUnitAmount(Variables.strToDouble((binding.edEditproductGramaj.getText().toString())));
            } catch (ParseException e) {
                gProduct.setUnitAmount(0.0);
                Log.e(TAG, "saveProduct: 2 " + e.getMessage());
            }
        else
            gProduct.setUnitAmount(0.0);

        gProduct.setPrintLabel(1);
        gProduct.setChanged(1);

        try {
            ProductDao.saveProduct(gProduct);


            Toast.makeText(EditProductActivity.this, R.string.update_was_done, Toast.LENGTH_SHORT).show();

            Intent out = new Intent();
            out.putExtra("productname", gProduct.getProductName());
            setResult(RESULT_OK, out);

        } catch (Exception e) {
            Log.e("editproduct", e.getMessage());
        }

    }


    private boolean saveProductVaryants() {
        if (binding.edEditproductProductname.getText().toString().isEmpty() || binding.cmbEditproductGroupname.getSelectedItemPosition() <= 0 ||
                binding.cmbEditproductTaxname.getSelectedItemPosition() <= 0 || binding.cmbEditproductUnitname.getSelectedItemPosition() <= 0) {
            Toast.makeText(this, R.string.msg_dont_empty_name_group_tax, Toast.LENGTH_LONG).show();

            return false;
        }


        gProduct.setUniteName(binding.cmbEditproductUnitname.getSelectedItem().toString());
        gProduct.setUnitId(ProductDao.getUnitId(binding.cmbEditproductUnitname.getSelectedItem().toString()));
        if (binding.cmbEditproductDeposite.getSelectedItemPosition() >= 0) {
            gProduct.setDepositeName(binding.cmbEditproductDeposite.getSelectedItem().toString());
            gProduct.setDepositId(ProductDao.getDepositId(binding.cmbEditproductDeposite.getSelectedItem().toString()));
        } else {
            gProduct.setDepositeName("");
            gProduct.setDepositId(-1);
        }
        gProduct.setGroupName(binding.cmbEditproductGroupname.getSelectedItem().toString());
        gProduct.setGroupId(ProductDao.getGroupId(binding.cmbEditproductGroupname.getSelectedItem().toString()));
        gProduct.setTaxName(binding.cmbEditproductTaxname.getSelectedItem().toString());
        gProduct.setTaxId(ProductDao.getTaxId(binding.cmbEditproductTaxname.getSelectedItem().toString()));

        gProduct.setProductName(binding.edEditproductProductname.getText().toString());
        gProduct.setBarcode(binding.edEditproductBarcode.getText().toString());
        gProduct.setPlu(binding.edEditproductPlu.getText().toString());

        gProduct.setOrigin(binding.edEditproductOrigin.getText().toString());
        gProduct.setDescription(binding.edEditproductDescription.getText().toString());
        gProduct.setAmountUnit(binding.cmbEditproductGramaj.getSelectedItem().toString());

        if (binding.edEditproductCostprice.getText().toString() != "")
            try {
                gProduct.setCostPrice(Variables.strToDouble((binding.edEditproductCostprice.getText().toString())));
            } catch (ParseException e) {
                gProduct.setCostPrice(0.0);
                Log.e(TAG, "saveProduct: 0 " + e.getMessage());
            }
        else
            gProduct.setCostPrice(0.0);

        if (binding.edEditproductSellprice.getText().toString() != "")
            try {
                String sellprice = binding.edEditproductSellprice.getText().toString();
                gProduct.setPrice(Variables.strToDouble(sellprice));
            } catch (ParseException e) {
                gProduct.setPrice(0.0);
                Log.e(TAG, "saveProduct: 1 " + e.getMessage());
            }
        else
            gProduct.setPrice(0.0);

        if (binding.edEditproductGramaj.getText().toString() != "")
            try {
                gProduct.setUnitAmount(Variables.strToDouble((binding.edEditproductGramaj.getText().toString())));
            } catch (ParseException e) {
                gProduct.setUnitAmount(0.0);
                Log.e(TAG, "saveProduct: 2 " + e.getMessage());
            }
        else {
            gProduct.setUnitAmount(0.0);
        }

        gProduct.setPrintLabel(1);
        gProduct.setChanged(1);


        try {

            List<AddedVaryantsModel> varyants = viewModel.getVaryantsAddedLiveData().getValue();
            if (varyants != null && !varyants.isEmpty()) {
                for (AddedVaryantsModel varyant : varyants) {
                    try {

                        gProduct.setId(0);
                        gProduct.setProductName(varyant.getUrunadi());
                        gProduct.setBarcode(varyant.getBarcode());
                        gProduct.setPlu(String.valueOf(varyant.getPlu()));

                        gProduct.setVaryant_anagrupid(varyant.getAnagrupid());
                        gProduct.setVaryant_alt_grupid(varyant.getAltgrupid());

                        try {

                            Product newVaryantProduct = gProduct;
                            ProductDao.saveProduct(newVaryantProduct);
                            Log.e(TAG, "saveProductVaryants: Varyantlı Ürün Kaydedildi: " + newVaryantProduct.getProductName());

                            if (SendDao.sendProductsVaryants()) {
                                viewModel.removeSelectedVaryant(varyant.getUrunadi());
                            }

                            Log.e(TAG, "saveProductVaryants: 10 sn bekle");

                            Thread.sleep(1000);
                            Log.e(TAG, "onClick: 8");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "saveProductVaryants: " + ex.getMessage());
                        runOnUiThread(loadingDialog::dismiss);
                        return false;
                    }
                }

                // KAydetme basariliysa
                SendDao.sendChangedPrices();
                runOnUiThread(loadingDialog::dismiss);
                return true;

            } else {
                Log.e(TAG, "saveProductVaryants: Kaydedilecek varyant yok.");
                runOnUiThread(loadingDialog::dismiss);
                return false;
            }


        } catch (Exception e) {
            Log.e("editproduct", e.getMessage());
        } finally {
            runOnUiThread(loadingDialog::dismiss);
        }
        return false;
    }


    private void openBarcode() {
        IntentIntegrator scanIntegrator = new IntentIntegrator(EditProductActivity.this);
        scanIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
        scanIntegrator.setOrientationLocked(true);
        scanIntegrator.initiateScan();
    }

    private void listFields() {
        gGroupList = ProductDao.getGroupListForSpinner(" ");
        gTaxList = ProductDao.getTaxListForSpinner(" ");
        gDepositList = ProductDao.getDepositListForSpinner(" ");
        gUnitList = ProductDao.getUnitListForSpinner(" ");

        //"All" listeden siliniyor
//        gTaxList.remove(0);
//        gUnitList.remove(0);
//        gGroupList.set(0, "");
//        gTaxList.set(0, "");
//        gDepositList.set(0, "");

        if (gGroupList != null) {
            ArrayAdapter<String> adp = new ArrayAdapter<>(this, R.layout.spinner_row, gGroupList);
            binding.cmbEditproductGroupname.setAdapter(adp);
        } else {
            binding.cmbEditproductGroupname.setAdapter(null);
        }
        if (gTaxList != null) {
            ArrayAdapter<String> adp = new ArrayAdapter<>(this, R.layout.spinner_row, gTaxList);
            binding.cmbEditproductTaxname.setAdapter(adp);
        } else {
            binding.cmbEditproductTaxname.setAdapter(null);
        }
        if (gDepositList != null) {
            ArrayAdapter<String> adp = new ArrayAdapter<>(this, R.layout.spinner_row, gDepositList);
            binding.cmbEditproductDeposite.setAdapter(adp);
        } else {
            binding.cmbEditproductDeposite.setAdapter(null);
        }
        if (gUnitList != null) {
            ArrayAdapter<String> adp = new ArrayAdapter<>(this, R.layout.spinner_row, gUnitList);
            binding.cmbEditproductUnitname.setAdapter(adp);
        } else {
            binding.cmbEditproductUnitname.setAdapter(null);
        }
    }


}
