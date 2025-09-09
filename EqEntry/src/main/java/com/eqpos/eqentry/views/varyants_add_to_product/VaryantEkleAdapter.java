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

import com.eqpos.eqentry.databinding.ItemVaryantMainBinding;
import com.eqpos.eqentry.models.VaryantModel;

import java.util.List;

public class VaryantEkleAdapter extends RecyclerView.Adapter<VaryantEkleAdapter.ViewHolder> {
    private List<VaryantModel> varyantList;
    private Context context;
    private String urunAdi="";


    public VaryantEkleAdapter(Context context, String urunAdi,  List<VaryantModel> varyantList) {
        this.context = context;
        this.varyantList = varyantList;
        this.urunAdi = urunAdi;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ViewBinding ile layout inflate etme
        ItemVaryantMainBinding binding = ItemVaryantMainBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VaryantModel varyant = varyantList.get(position);
        // ID ve Aciklama'yı TextView'lara bağla
        holder.binding.tvId.setText(String.valueOf(varyant.getId()));
        holder.binding.tvAciklama.setText(String.valueOf(varyant.getAciklama()));
        holder.binding.tvSira.setText(String.valueOf(varyant.getSira()));

        // Item tıklama: Model bilgilerini konsola yaz
        holder.itemView.setOnClickListener(v -> {
            Log.d("VaryantSelected", "Seçilen Varyant: ID=" + varyant.getId() +
                    ", Sira=" + varyant.getSira() +
                    ", Tanim=" + varyant.getTanim() +
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

            Intent intent = new Intent(context, VaryantsEkleGruplari.class);
            // Bundle oluştur ve verileri ekle
            Bundle bundle = new Bundle();
            bundle.putInt("id", varyant.getId());
            bundle.putInt("sira", varyant.getSira());
            bundle.putString("tanim", varyant.getTanim());
            bundle.putInt("rowcell", varyant.getRowcell());
            bundle.putString("aciklama", varyant.getAciklama());
            bundle.putInt("parentid", varyant.getParentid());
            bundle.putString("urunadi", urunAdi);
            bundle.putString("maingrupismi", varyant.getAciklama());        // ADIDAS burda

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
        final ItemVaryantMainBinding binding;

        public ViewHolder(@NonNull ItemVaryantMainBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
