package com.eqpos.eqentry.views.depo_secimi;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.eqpos.eqentry.databinding.DialogDepoBinding;
import com.eqpos.eqentry.models.DepoModel;
import com.eqpos.eqentry.views.inventur.SayimlarimiGoster;

import java.util.List;

public class DepoDialogFragment extends DialogFragment {

    private final DepoSelectionCallback callback;
    private final List<DepoModel> depoList; // Constructor'dan alınacak liste
    private DialogDepoBinding binding;
    private DepoAdapter adapter;

    public DepoDialogFragment(DepoSelectionCallback callback, List<DepoModel> depoList) {
        this.callback = callback;
        this.depoList = depoList;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogDepoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // RecyclerView setup
        binding.rvDepoList.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));

        // Adapter'ı set et
        adapter = new DepoAdapter(depoList);
        binding.rvDepoList.setAdapter(adapter);


        //Sayimlarimi Göster
        binding.btnSayimlarimiGoster.setOnClickListener(view1 -> {
//            int selectedDepoId = adapter.getSelectedDepoId();
//            if (selectedDepoId == -1) {
//                Toast.makeText(getContext(), "Lütfen bir depo seçin!", Toast.LENGTH_SHORT).show();
//            } else {
            Bundle bundle = new Bundle();
            bundle.putInt("selectedDepoId", 0);
            Intent sayimlarimiGoster = new Intent(getContext(), SayimlarimiGoster.class);
            sayimlarimiGoster.putExtras(bundle);
            startActivity(sayimlarimiGoster);
            dismiss();
//            }
        });


        // TAMAM butonu click
        binding.btnTamam.setOnClickListener(v -> {
            int selectedDepoId = adapter.getSelectedDepoId();
            if (selectedDepoId == -1) {
                Toast.makeText(getContext(), "Lütfen bir depo seçin!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Seçili depo'yu bul
            for (DepoModel depo : depoList) {
                if (depo.getId() == selectedDepoId) {
                    if (callback != null) {
                        callback.onDepoSelected(depo.getId(), depo.getIsmi()); // Veya getDepoAdi()
                    }
                    break;
                }
            }
            dismiss(); // Dialog'u kapat
        });

        // İPTAL butonu click
        binding.btnIptal.setOnClickListener(v -> {
            dismiss(); // Sadece dialog'u kapat
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Dialog penceresini büyüt
        Window window = getDialog().getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}