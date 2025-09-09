package com.eqpos.eqentry.views.varyants_add_to_product;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eqpos.eqentry.R;
import com.eqpos.eqentry.databinding.ActivityVaryantsEkleBinding;
import com.eqpos.eqentry.views.varyants.VaryantViewModel;

import java.util.ArrayList;
import java.util.Locale;

public class VaryantsEkle extends AppCompatActivity {

    private int mainParentGrupId = 0;
    private String mainGrupIsmi = "";
    private int sira = 0;
    private ActivityVaryantsEkleBinding binding;
    private VaryantViewModel viewModel;
    private VaryantEkleAdapter adapter;
    private int mainVaryantSize = 0; // Ana varyant sayısı
    private String urunAdi = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //setContentView(R.layout.activity_varyants_ekle);
        binding = ActivityVaryantsEkleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        this.setTitle(String.format(Locale.getDefault(), "%s - Varyants Secin", getString(R.string.app_name)));

        initViews();

    }

    private void initViews() {
        getBundles();
        initRecyclerView();

        binding.btnVazgec.setOnClickListener(view -> finish());
    }


    private void initRecyclerView() {
        binding.recyclerViewVaryants.setHasFixedSize(true);
        binding.recyclerViewVaryants.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VaryantEkleAdapter(this, urunAdi,  new ArrayList<>());
        binding.recyclerViewVaryants.setAdapter(adapter);

        // ViewModel'ı başlat
        viewModel = new ViewModelProvider(this).get(VaryantViewModel.class);

        // LiveData'yı gözlemle
        viewModel.getVaryantsLiveData().observe(this, varyantList -> {
            adapter.updateList(varyantList);
            Log.d("Varyants", "Liste güncellendi, boyut: " + varyantList.size());
            mainVaryantSize = varyantList.size() + 1; // Ana varyant sayısını güncelle
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

            urunAdi = extras.getString("urunadi");
            binding.tvTitel.setText(urunAdi);
        }
    }

}