package com.eqpos.eqentry.views.varyants_add_to_product;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.eqpos.eqentry.databinding.ActivityVaryantEkleGruplariBinding;
import com.eqpos.eqentry.views.varyants.VaryantViewModelGruplar;

import java.util.Locale;

public class VaryantsEkleGruplari extends AppCompatActivity {

    private int mainParentGrupId = 0;
    private String mainGrupIsmi = "";
    private int sira = 0;
    private String urunAdi = "";
    private ActivityVaryantEkleGruplariBinding binding;
    private VaryantViewModelGruplar viewModel;
    private VaryantEkleAdapterGruplar adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //setContentView(R.layout.activity_varyant_ekle_gruplari);
        binding = ActivityVaryantEkleGruplariBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setTitle(String.format(Locale.getDefault(), "Varyant Grup ve Türleri"));

        initViews();

    }

    private void initViews() {
        getBundles();
        initRecyclerView();

        binding.btnGeri.setOnClickListener(view -> finish());
    }


    private void initRecyclerView() {
        binding.rvVaryantGruplari.setHasFixedSize(true);
        binding.rvVaryantGruplari.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        viewModel = new VaryantViewModelGruplar(getApplication(), mainParentGrupId);

        viewModel.getVaryantsGruplarLiveData().observe(this, varyants -> {
            adapter = new VaryantEkleAdapterGruplar(this, urunAdi,mainGrupIsmi, varyants);
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

            String mainIsim = extras.getString("aciklama");
            mainGrupIsmi = mainIsim != null ? mainIsim : "";


            binding.tvMainGrupIsmi.setText(mainGrupIsmi);

            urunAdi = extras.getString("urunadi");
            binding.tvTitel.setText(urunAdi);
        }
    }


}