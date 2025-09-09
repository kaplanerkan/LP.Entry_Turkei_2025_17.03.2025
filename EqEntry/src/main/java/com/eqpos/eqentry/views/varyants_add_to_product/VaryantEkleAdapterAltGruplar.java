package com.eqpos.eqentry.views.varyants_add_to_product;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eqpos.eqentry.databinding.ItemVaryantEkleGruplarBinding;
import com.eqpos.eqentry.models.VaryantModel;

import java.util.ArrayList;
import java.util.List;

public class VaryantEkleAdapterAltGruplar extends RecyclerView.Adapter<VaryantEkleAdapterAltGruplar.ViewHolder> {
    private List<VaryantModel> varyantList;
    private Context context;
    private String urunAdi = "";

    public VaryantEkleAdapterAltGruplar(Context context, String urunadi, List<VaryantModel> varyantList) {
        this.context = context;
        this.varyantList = varyantList;
        this.urunAdi = urunadi;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ViewBinding ile layout inflate etme
        ItemVaryantEkleGruplarBinding binding = ItemVaryantEkleGruplarBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VaryantModel varyant = varyantList.get(position);
        // ID ve Aciklama'yı TextView'lara bağla
        holder.binding.tvGrupId.setText(String.valueOf(varyant.getId()));

        holder.binding.tvGrupAdi.setText(String.valueOf(varyant.getAciklama()));
        holder.binding.tvGrupTuru.setText(String.valueOf(varyant.getTanim()));

        holder.binding.tvGrupSira.setText(String.valueOf(varyant.getSira()));
        // CheckBox durumunu modelden al
        holder.binding.cbSelected.setChecked(varyant.isSelected());

        // Item tıklama: Model bilgilerini konsola yaz
        holder.itemView.setOnClickListener(v -> {
//            Log.d("VaryantSelected", "Seçilen Varyant: ID=" + varyant.getId() +
//                    ", Sira=" + varyant.getSira() +
//                    ", Tanim=" + varyant.getTanim() +
//                    ", Rowcell=" + varyant.getRowcell() +
//                    ", Aciklama=" + varyant.getAciklama() +
//                    ", ParentID=" + varyant.getParentid());

            // Seçili durumu toggle et (tersine çevir)
            varyant.setSelected(!varyant.isSelected());
            holder.binding.cbSelected.setChecked(varyant.isSelected());

            // Yeşil renk animasyonu
            int originalColor = 0xFFFFFFFF; // Beyaz (#FFFFFF)
            int highlightColor = 0xFFC8E6C9; // Hafif yeşil
            ValueAnimator colorAnimator = ValueAnimator.ofObject(
                    new ArgbEvaluator(), originalColor, highlightColor);
            colorAnimator.setDuration(300); // 300 ms animasyon süresi
            colorAnimator.addUpdateListener(animator -> {
                holder.binding.getRoot().setCardBackgroundColor((int) animator.getAnimatedValue());
            });
            colorAnimator.setRepeatCount(1);
            colorAnimator.setRepeatMode(ValueAnimator.REVERSE); // Geri dönerek orijinal renge döner
            colorAnimator.start();




        });
    }

    @Override
    public int getItemCount() {
        return varyantList.size();
    }

    public void updateList(List<VaryantModel> newList) {
        this.varyantList = newList;
        notifyDataSetChanged();
    }

    // Tüm varyantları seç
    public void selectAll() {
        for (VaryantModel varyant : varyantList) {
            varyant.setSelected(true);
        }
        notifyDataSetChanged(); // Tüm liste güncellendi
    }

    // Tüm varyantların seçimini kaldır
    public void deselectAll() {
        for (VaryantModel varyant : varyantList) {
            varyant.setSelected(false);
        }
        notifyDataSetChanged(); // Tüm liste güncellendi
    }


    // Seçili varyantları döndüren metod
    public List<VaryantModel> getSelectedVaryants() {
        List<VaryantModel> selectedList = new ArrayList<>();
        for (VaryantModel varyant : varyantList) {
            if (varyant.isSelected()) {
                selectedList.add(varyant);
            }
        }
        return selectedList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemVaryantEkleGruplarBinding binding;

        public ViewHolder(@NonNull ItemVaryantEkleGruplarBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
