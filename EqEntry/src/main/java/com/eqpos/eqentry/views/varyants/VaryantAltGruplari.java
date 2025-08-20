package com.eqpos.eqentry.views.varyants;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.eqpos.eqentry.databinding.ActivityVaryantAltGruplariBinding;
import com.eqpos.eqentry.db.VaryantsDao;

import java.util.List;
import java.util.Locale;

public class VaryantAltGruplari extends AppCompatActivity {
    private int mainParentGrupId = 0;
    private String mainGrupIsmi = "";
    private ActivityVaryantAltGruplariBinding binding;
    private VaryantAdapterAltGruplar adapter;
    private VaryantViewModelAltGruplar viewModel;
    private int sira = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //setContentView(R.layout.activity_varyant_alt_gruplari);
        binding = ActivityVaryantAltGruplariBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        this.setTitle(String.format(Locale.getDefault(), "Varyant Alt Grup ve Türleri"));
        initViews();

    }


    private void initViews() {
        getBundles();
        initRecyclerView();
        btnAddVaryantGrupOlaylari();
        btnGeriOlaylari();

        otomatikVeriDoldurmaIsmeleri();
    }


    private void otomatikVeriDoldurmaIsmeleri() {
        // Veritabanından çekilen verileri simüle eden bir liste
        List<String> veriListesi = VaryantsDao.getAllGrupTurleri(2, mainParentGrupId);  // 2, alt grup türlerini temsil ediyor

        // ArrayAdapter oluştur ve verileri bağla
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                veriListesi
        );

        // AutoCompleteTextView'a adapter'ı ata
        binding.tvAltGrupTuru.setAdapter(adapter);

        // Otomatik tamamlama için minimum karakter sayısı (isteğe bağlı)
        binding.tvAltGrupTuru.setThreshold(1); // 1 karakterden itibaren önerileri göster


    }


    private void btnGeriOlaylari() {
        binding.btnAltGrupGeri.setOnClickListener(view -> finish());
    }


    private void btnAddVaryantGrupOlaylari() {
        binding.btnAddVaryantAltGruplari.setOnClickListener(view -> {
            String grupIsmi = binding.tvAltGrupIsmi.getText().toString();
            String grupTuru = binding.tvAltGrupTuru.getText().toString();

            if (grupIsmi.isEmpty()) {
                binding.tilTanim.setError("Grup ismi boş olamaz");

            } else if (grupTuru.isEmpty()) {
                binding.tilTanim.setError("Türü ismi boş olamaz");
            } else {
                // String tanim, int sira, String aciklama, int parentid
                viewModel.addNewAltGrupVaryant(grupTuru, sira, grupIsmi, mainParentGrupId);
                Toast.makeText(VaryantAltGruplari.this, "Yeni varyant alt grubu eklendi: " + grupIsmi, Toast.LENGTH_SHORT).show();
                binding.tvAltGrupIsmi.setText("");
            }

        });
    }

    private void initRecyclerView() {
        binding.rvVaryantGruplari.setHasFixedSize(true);
        binding.rvVaryantGruplari.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        viewModel = new VaryantViewModelAltGruplar(getApplication(), mainParentGrupId);

        viewModel.getVaryantsAltGruplarLiveData().observe(this, varyants -> {
            adapter = new VaryantAdapterAltGruplar(this, varyants);
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

            mainParentGrupId = extras.getInt("parentid");

            String mainIsim = extras.getString("aciklama") + (" - " + mainParentGrupId);
            mainGrupIsmi = mainIsim != null ? mainIsim : "";

            binding.tvMainAltGrupIsmi.setText(mainGrupIsmi);
        }
    }


}