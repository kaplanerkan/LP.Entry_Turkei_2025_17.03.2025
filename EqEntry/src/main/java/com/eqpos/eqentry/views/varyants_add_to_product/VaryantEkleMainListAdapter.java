package com.eqpos.eqentry.views.varyants_add_to_product;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eqpos.eqentry.databinding.ItemVaryantEkleMainListAdapterBinding;
import com.eqpos.eqentry.models.AddedVaryantsModel;

import java.util.List;
import java.util.Locale;

public class VaryantEkleMainListAdapter extends RecyclerView.Adapter<VaryantEkleMainListAdapter.ViewHolder> {
    private List<AddedVaryantsModel> varyantList;
    private Context context;


    private OnVaryantSilClickListener clickListener; // Callback için arayüz


    public VaryantEkleMainListAdapter(Context context, List<AddedVaryantsModel> varyantList, OnVaryantSilClickListener listener) {
        this.context = context;
        this.varyantList = varyantList;
        clickListener = listener;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ViewBinding ile layout inflate etme
        ItemVaryantEkleMainListAdapterBinding binding = ItemVaryantEkleMainListAdapterBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AddedVaryantsModel varyant = varyantList.get(position);
        // ID ve Aciklama'yı TextView'lara bağla
        holder.binding.tvVaryantId.setText(String.valueOf(varyant.getId()));
        holder.binding.tvEklenenVaryantAdi.setText(String.valueOf(varyant.getUrunadi()));
        String preis = String.format(Locale.getDefault(),"%.2f", varyant.getPrice());
        holder.binding.tvPreis.setText(preis);
        holder.binding.tvBarcode.setText(varyant.getBarcode());
        String plu = String.format(Locale.getDefault(),"%d", varyant.getPlu());
        holder.binding.tvPlu.setText(plu);

        holder.binding.btnVaryantSil.setOnClickListener(view -> {
            if (clickListener != null) {
                clickListener.onVaryantSil(varyant); // Callback'i çağır
            }
        });

        holder.binding.btnEditVaryant.setOnClickListener(view -> {
            if (clickListener != null) {
                clickListener.onEditVaryant(varyant); // Callback'i çağır
            }
        });

        // Item tıklama: Model bilgilerini konsola yaz
        holder.itemView.setOnClickListener(v -> {
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

//            Intent intent = new Intent(context, VaryantGruplari.class);
//            // Bundle oluştur ve verileri ekle
//            Bundle bundle = new Bundle();
//            bundle.putInt("id", varyant.getId());
//            bundle.putInt("sira", varyant.getSira());
//            bundle.putString("tanim", varyant.getTanim());
//            bundle.putInt("rowcell", varyant.getRowcell());
//            bundle.putString("aciklama", varyant.getAciklama());
//            bundle.putInt("parentid", varyant.getParentid());
//
//            // Bundle'ı Intent'e ekle
//            intent.putExtras(bundle);
//            // İkinci Activity'yi başlat
//            context.startActivity(intent);

        });
    }

    @Override
    public int getItemCount() {
        return varyantList.size();
    }

    public void updateList(List<AddedVaryantsModel> newList) {
        this.varyantList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemVaryantEkleMainListAdapterBinding binding;

        public ViewHolder(@NonNull ItemVaryantEkleMainListAdapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
