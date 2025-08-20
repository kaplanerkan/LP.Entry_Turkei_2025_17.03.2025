package com.eqpos.eqentry.views.varyants;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.eqpos.eqentry.R;
import com.eqpos.eqentry.databinding.ActivityVaryantsBinding;
import com.eqpos.eqentry.databinding.DialogAddVaryantMainBinding;

import java.util.ArrayList;
import java.util.Locale;

public class Varyants extends AppCompatActivity {


    private ActivityVaryantsBinding binding;
    private VaryantAdapter adapter;
    private VaryantViewModel viewModel;

    private int mainVaryantSize = 0; // Ana varyant sayısı
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        //setContentView(R.layout.activity_varyants);
        binding = ActivityVaryantsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        this.setTitle(String.format(Locale.getDefault(),"%s - Varyants", getString(R.string.app_name)));

        initViews();

    }

    private void initViews() {
        initRecyclerView();

        btnAddVaryantOlaylari();
    }

    private void btnAddVaryantOlaylari() {
        binding.btnAddVaryant.setOnClickListener(view -> {
            // Yeni varyant ekleme işlemleri
            //viewModel.addNewVaryant();
            Log.d("Varyants", "Yeni varyant ekleme butonuna tıklandı : ADET:: " + adapter.getItemCount());
            DialogAddVaryantMainBinding mainVaryantDialogBinding = DialogAddVaryantMainBinding.inflate(getLayoutInflater());
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(mainVaryantDialogBinding.getRoot())
                    .create();
            dialog.show();

            mainVaryantDialogBinding.btnSave.setOnClickListener(v1 -> {
                String tanim = mainVaryantDialogBinding.etTanim.getText().toString().trim();
                if (!tanim.isEmpty()) {
                    viewModel.addNewMainVaryant(tanim, mainVaryantSize);
                    dialog.dismiss();
                } else {
                    mainVaryantDialogBinding.tilTanim.setError("Varyant ismi boş olamaz");
                }
            });

            mainVaryantDialogBinding.btnCancel.setOnClickListener(v1 -> dialog.dismiss());
        });

    }







    private void initRecyclerView() {
        binding.recyclerViewVaryants.setHasFixedSize(true);
        binding.recyclerViewVaryants.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VaryantAdapter(this, new ArrayList<>());
        binding.recyclerViewVaryants.setAdapter(adapter);

        // ViewModel'ı başlat
        viewModel = new ViewModelProvider(this).get(VaryantViewModel.class);

        // LiveData'yı gözlemle
        viewModel.getVaryantsLiveData().observe(this, varyantList -> {
            adapter.updateList(varyantList);
            Log.d("Varyants", "Liste güncellendi, boyut: " + varyantList.size());
            mainVaryantSize = varyantList.size() + 1 ; // Ana varyant sayısını güncelle
        });
    }


}