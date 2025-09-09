package com.eqpos.eqentry.views.varyants_add_to_product;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eqpos.eqentry.databinding.ItemVaryantGruplarBinding;
import com.eqpos.eqentry.models.VaryantModel;
import com.eqpos.eqentry.views.varyants.VaryantAltGruplari;

import java.util.List;

public class VaryantEkleAdapterGruplar extends RecyclerView.Adapter<VaryantEkleAdapterGruplar.ViewHolder> {
    private List<VaryantModel> varyantList;
    private Context context;
    private String urunadi;
    private String mainGrupIsmi = "";
    public VaryantEkleAdapterGruplar(Context context, String urunadi, String mainGrupIsmi, List<VaryantModel> varyantList) {
        this.context = context;
        this.varyantList = varyantList;
        this.urunadi = urunadi;
        this.mainGrupIsmi = mainGrupIsmi;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ViewBinding ile layout inflate etme
        ItemVaryantGruplarBinding binding = ItemVaryantGruplarBinding.inflate(
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

        // Item tıklama: Model bilgilerini konsola yaz
        holder.itemView.setOnClickListener(v -> {
            Log.d("VaryantSelected", "Seçilen Varyant: ID=" + varyant.getId() +
                    ", Sira=" + varyant.getSira() +
                    ", Tanim=" + varyant.getTanim() +
                    ", Rowcell=" + varyant.getRowcell() +
                    ", Aciklama=" + varyant.getAciklama() +
                    ", ParentID=" + varyant.getParentid());

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

            Intent intent = new Intent(context, VaryantsEkleAltGruplari.class);
            // Bundle oluştur ve verileri ekle
            Bundle bundle = new Bundle();
            bundle.putInt("id", varyant.getId());
            bundle.putInt("sira", varyant.getSira());
            bundle.putString("tanim", varyant.getTanim());
            bundle.putInt("rowcell", varyant.getRowcell());
            bundle.putString("aciklama", varyant.getAciklama());
            bundle.putInt("parentid", varyant.getParentid());
            bundle.putString("urunadi", urunadi);
            bundle.putString("maingrupismi", mainGrupIsmi);
            // Bundle'ı Intent'e ekle
            intent.putExtras(bundle);
            // İkinci Activity'yi başlat
            context.startActivity(intent);

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

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemVaryantGruplarBinding binding;

        public ViewHolder(@NonNull ItemVaryantGruplarBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
