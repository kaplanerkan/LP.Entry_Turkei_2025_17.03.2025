package com.eqpos.eqentry.views.varyants_add_to_product;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eqpos.eqentry.databinding.ActivityVaryantsEkleAltGruplariBinding;
import com.eqpos.eqentry.models.VaryantModel;
import com.eqpos.eqentry.tools.SharedPrefUtil;
import com.eqpos.eqentry.views.varyants.VaryantViewModelAltGruplar;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class VaryantsEkleAltGruplari extends AppCompatActivity {

    private int mainParentGrupId = 0;
    private String mainGrupIsmi = "";
    private int sira = 0;
    private String urunAdi = "";
    private String oncekiAciklama = "";
    private int anagrupId = 0;
    private ActivityVaryantsEkleAltGruplariBinding binding;
    private VaryantViewModelAltGruplar viewModel;
    private VaryantEkleAdapterAltGruplar adapter;

    private VaryantEkleViewModel viewmodel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //setContentView(R.layout.activity_varyants_ekle_alt_gruplari);
        binding = ActivityVaryantsEkleAltGruplariBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewmodel = new ViewModelProvider(this).get(VaryantEkleViewModel.class);

        this.setTitle(String.format(Locale.getDefault(), "Varyant Alt Grup ve Türleri"));
        initViews();

    }

    private void initViews() {
        getBundles();
        initRecyclerView();

        btnVaryantslariEkleClickOlaylari();
        btnGeriClickOlaylari();
        btnHepsiniEkleClickOlaylari();
        btnHepsiniKaldirClickOlaylari();
    }

    private void btnHepsiniEkleClickOlaylari() {
        binding.btnHepsiniEkle.setOnClickListener(view -> {
            if (adapter != null) {
                adapter.selectAll();
            }
        });
    }

    private void btnHepsiniKaldirClickOlaylari() {
        binding.btnHepsiniKaldir.setOnClickListener(view -> {
            if (adapter != null) {
                adapter.deselectAll();
            }
        });
    }


    private void btnGeriClickOlaylari() {
        binding.btnAltGrupGeri.setOnClickListener(view -> finish());
    }


    private void btnVaryantslariEkleClickOlaylari() {


        binding.btnVaryantAddNow.setOnClickListener(view -> {
            try {
                showEminMisinizDialog();
            } catch (Exception ex) {
                Log.e("NumberFormatError", "Fiyat parse edilirken hata: " + ex.getMessage());
            }
        });
    }


    private void showEminMisinizDialog() {
        try {
            final String edPrefix = binding.edPrefix.getText().toString().trim();
            final boolean barcodeOlusturulsunMu = binding.cbBarcodeOlustur.isChecked();
            final boolean pluOlusturulsunMu = binding.cbPluOlustur.isChecked();

            final String bodyMsg = String.format(Locale.getDefault(),
                    "Ön Ek: %s\nBarkod Oluşturulsun mu: %s\nPLU Oluşturulsun mu: %s",
                    edPrefix,
                    barcodeOlusturulsunMu ? "Evet" : "Hayır",
                    pluOlusturulsunMu ? "Evet" : "Hayır"
            );

            // AlertDialog oluşturma
            AlertDialog.Builder builder = new AlertDialog.Builder(VaryantsEkleAltGruplari.this);
            builder.setTitle("Emin misiniz?");
            builder.setMessage(bodyMsg);

            // Evet butonu
            builder.setPositiveButton("Evet", (dialog, which) -> {
                try {
                    // SharedPreferences'tan fiyatı al
                    String fiyati = String.format(Locale.getDefault(), "%.2f",
                            Double.parseDouble(SharedPrefUtil.getString(SharedPrefUtil.KEY_VARYANT_SATIS_FIYATI, "0.00")));

                    // NumberFormat ile cihazın yerel ayarlarına göre parse et
                    NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
                    Number parsedNumber = numberFormat.parse(fiyati);
                    double varyatlarIcinFiyati = parsedNumber.doubleValue();

                    // Seçili varyantları al
                    List<VaryantModel> selectedVaryants = adapter.getSelectedVaryants();

                    // Logcat'e yazdır
                    for (VaryantModel varyant : selectedVaryants) {
                        String eklenecekUrunIsmi = String.format(Locale.getDefault(), "%s %s %s %s",
                                urunAdi.toUpperCase(),
                                oncekiAciklama,
                                mainGrupIsmi,
                                varyant.getAciklama()
                        );
                        String barcode = "0";
                        int plu = 0;
                        if (pluOlusturulsunMu) {
                            plu = Integer.parseInt(generatePLU(edPrefix));
                        }
                        if (barcodeOlusturulsunMu) {
                            barcode =  generateEAN13Barcode(edPrefix);
                        }

                        viewmodel.addNewAddedSelected(0, barcode, eklenecekUrunIsmi, varyatlarIcinFiyati, anagrupId, varyant.getId(), plu);
                        Log.d("SelectedVaryant", eklenecekUrunIsmi);

                    }


                    finish();
                } catch (Exception ex) {
                    Log.e("NumberFormatError", "Fiyat parse edilirken hata: " + ex.getMessage());
                }

            });

            // Hayır butonu
            builder.setNegativeButton("Hayır", (dialog, which) -> {
                dialog.dismiss(); // Diyalog kutusunu kapat
            });

            // İptal edilebilir mi?
            builder.setCancelable(false); // Dışarı tıklayınca kapanmasını engeller

            // AlertDialog'u göster
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        } catch (Exception ex) {
            Log.e("DialogError", "Dialog gösterilirken hata: " + ex.getMessage());
        }
    }

    private String generateEAN13Barcode(String prefix) {
        Random random = new Random();
        StringBuilder barcode = new StringBuilder();

        // Ön eki ekle (maksimum 5 hane)
        if (prefix != null && !prefix.trim().isEmpty()) {
            String cleanPrefix = prefix.replaceAll("[^0-9]", ""); // Sadece rakamları al
            if (cleanPrefix.length() > 5) {
                cleanPrefix = cleanPrefix.substring(0, 5); // Maksimum 5 hane
            }
            barcode.append(cleanPrefix);
        }

        // Kalan haneleri rastgele rakamlarla doldur (toplam 12 hane olacak şekilde)
        int remainingDigits = 12 - barcode.length();
        for (int i = 0; i < remainingDigits; i++) {
            barcode.append(random.nextInt(10)); // 0-9 arası rastgele rakam
        }

        // Kontrol hanesini hesapla ve ekle
        int checksum = calculateEAN13Checksum(barcode.toString());
        barcode.append(checksum);

        return barcode.toString();
    }

    private int calculateEAN13Checksum(String barcode) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(barcode.charAt(i));
            // EAN-13 standardı:
            // 1. indeksten başlayarak (yani ilk rakam) çift sıradakiler x1, tek sıradakiler x3
            // Bu yüzden i % 2 == 0 olanlar aslında 1., 3., 5. ... haneler oluyor
            sum += (i % 2 == 0) ? digit * 1 : digit * 3;
        }

        // Kontrol hanesi: 10'a tamamlayacak şekilde
        int remainder = sum % 10;
        return (remainder == 0) ? 0 : 10 - remainder;
    }


    private String generatePLU(String prefix) {
        Random random = new Random();
        StringBuilder plu = new StringBuilder();

        // Ön eki ekle (maksimum 5 hane)
        if (prefix != null && !prefix.trim().isEmpty()) {
            String cleanPrefix = prefix.replaceAll("[^0-9]", ""); // Sadece rakamları al
            if (cleanPrefix.length() > 5) {
                cleanPrefix = cleanPrefix.substring(0, 5); // Maksimum 5 hane
            }
            plu.append(cleanPrefix);
        }

        // Eğer ön ek yoksa veya 5 haneden azsa, rastgele rakamlarla tamamla
        int remainingDigits = 5 - plu.length();
        for (int i = 0; i < remainingDigits; i++) {
            plu.append(random.nextInt(10)); // 0-9 arası rastgele rakam
        }

        return plu.toString();
    }


    private void initRecyclerView() {
        binding.rvVaryantGruplari.setHasFixedSize(true);
        binding.rvVaryantGruplari.setLayoutManager(new LinearLayoutManager(this));


        viewModel = new VaryantViewModelAltGruplar(getApplication(), mainParentGrupId);

        viewModel.getVaryantsAltGruplarLiveData().observe(this, varyants -> {
            // Varsayılan olarak tüm varyantları seçili değil yap (gerekirse)
            for (VaryantModel varyant : varyants) {
                varyant.setSelected(false);
            }
            adapter = new VaryantEkleAdapterAltGruplar(this, urunAdi, varyants);
            binding.rvVaryantGruplari.setAdapter(adapter);
            sira = varyants.size() + 1;
        });
    }

    private void getBundles() {
        /*
            Bundle bundle = new Bundle();
            bundle.putInt("id", varyant.getId());
            bundle.putInt("sira", varyant.getSira());
            bundle.putInt("tanim", varyant.getTanim());
            bundle.putString("aciklama", varyant.getAciklama());
            bundle.putInt("parentid", varyant.getParentid());
         */
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            anagrupId = extras.getInt("id");
            mainParentGrupId = extras.getInt("parentid");

            oncekiAciklama = extras.getString("maingrupismi");
            String mainIsim = extras.getString("aciklama");
            mainGrupIsmi = mainIsim != null ? mainIsim : "";

            binding.tvMainAltGrupIsmi.setText(mainGrupIsmi);

            urunAdi = extras.getString("urunadi") != null ? extras.getString("urunadi") : "";
            binding.tvTitel.setText(urunAdi);
        }
    }

}