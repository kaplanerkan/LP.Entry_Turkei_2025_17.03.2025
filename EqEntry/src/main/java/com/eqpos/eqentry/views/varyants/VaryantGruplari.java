package com.eqpos.eqentry.views.varyants;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.eqpos.eqentry.databinding.ActivityVaryantGruplariBinding;
import com.eqpos.eqentry.db.VaryantsDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VaryantGruplari extends AppCompatActivity {

    private int mainParentGrupId = 0;
    private String mainGrupIsmi = "";
    private ActivityVaryantGruplariBinding binding;
    private VaryantAdapterGruplar adapter;
    private VaryantViewModelGruplar viewModel;
    private int sira = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //setContentView(R.layout.activity_varyant_gruplari);
        binding = ActivityVaryantGruplariBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        this.setTitle(String.format(Locale.getDefault(), "Varyant Grup ve Türleri"));
        initViews();

    }

    private void initViews() {
        getBundles();
        initRecyclerView();
        btnAddVaryantGrupOlaylari();
        btnGeriOlaylari();

        otomatikVeriDoldurmaIsmeleri();
    }

    private void otomatikVeriDoldurmaIsmeleri(){
        // Veritabanından çekilen verileri simüle eden bir liste
        List<String> veriListesi = VaryantsDao.getAllGrupTurleri(1, mainParentGrupId);  // 1, ana grup türlerini temsil ediyor

        // ArrayAdapter oluştur ve verileri bağla
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                veriListesi
        );

        // AutoCompleteTextView'a adapter'ı ata
        binding.tvGrupTuru.setAdapter(adapter);

        // Otomatik tamamlama için minimum karakter sayısı (isteğe bağlı)
        binding.tvGrupTuru.setThreshold(1); // 1 karakterden itibaren önerileri göster


    }


    private void btnGeriOlaylari() {
        binding.btnGeri.setOnClickListener(view -> finish());
    }


    private void btnAddVaryantGrupOlaylari() {
        binding.btnAddVaryantGruplari.setOnClickListener(view -> {
            String grupIsmi = binding.tvGrupIsmi.getText().toString();
            String grupTuru = binding.tvGrupTuru.getText().toString();

            if (grupIsmi.isEmpty()) {
                binding.tilTanim.setError("Grup ismi boş olamaz");

            } else if (grupTuru.isEmpty()) {
                binding.tilTanim.setError("Türü ismi boş olamaz");
            } else {
                // String tanim, int sira, String aciklama, int parentid
                viewModel.addNewGrupVaryant(grupTuru, sira, grupIsmi, mainParentGrupId);
                Toast.makeText(VaryantGruplari.this, "Yeni varyant grubu eklendi: " + grupIsmi, Toast.LENGTH_SHORT).show();
                binding.tvGrupIsmi.setText("");
            }

        });
    }


    private void initRecyclerView() {
        binding.rvVaryantGruplari.setHasFixedSize(true);
        binding.rvVaryantGruplari.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        viewModel = new VaryantViewModelGruplar(getApplication(), mainParentGrupId);

        viewModel.getVaryantsGruplarLiveData().observe(this, varyants -> {
            adapter = new VaryantAdapterGruplar(this, varyants);
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

            mainParentGrupId = extras.getInt("id");

            String mainIsim = extras.getString("aciklama") + (" - " + mainParentGrupId);
            mainGrupIsmi = mainIsim != null ? mainIsim : "";

            binding.tvMainGrupIsmi.setText(mainGrupIsmi);
        }
    }


}