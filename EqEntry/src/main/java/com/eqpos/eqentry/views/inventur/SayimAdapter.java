package com.eqpos.eqentry.views.inventur;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eqpos.eqentry.databinding.ItemSayimListesiBinding;
import com.eqpos.eqentry.models.SayimModel;

import java.util.List;

public class SayimAdapter extends RecyclerView.Adapter<SayimAdapter.SayimViewHolder> {

    private List<SayimModel> sayimList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
        void onSendClick(int position);
        void onEditClick(int position);
    }

    public SayimAdapter(List<SayimModel> sayimList, OnItemClickListener listener) {
        this.sayimList = sayimList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SayimViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSayimListesiBinding binding = ItemSayimListesiBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new SayimViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SayimViewHolder holder, int position) {
        SayimModel sayim = sayimList.get(position);
        holder.bind(sayim);
    }

    @Override
    public int getItemCount() {
        return sayimList.size();
    }

    public void updateItem(int position, String yeniMiktar) {
        if (position >= 0 && position < sayimList.size()) {
            sayimList.get(position).setNewQuantity(yeniMiktar);
            notifyItemChanged(position);
        }
    }


    public class SayimViewHolder extends RecyclerView.ViewHolder {
        private ItemSayimListesiBinding binding;

        public SayimViewHolder(ItemSayimListesiBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // Buton click listener'ları
            binding.buttonDelete.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(position);
                    }
                }
            });

            binding.buttonSend.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onSendClick(position);
                    }
                }
            });

            binding.btnDuzenle.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onEditClick(position);
                    }
                }
            });


        }

        public void bind(SayimModel sayim) {
            binding.textUrunAdi.setText(sayim.getUrunAdi());
            binding.textYeniMiktar.setText(sayim.getNewQuantity());
            binding.textDepoId.setText(sayim.getWarehouseId());
        }
    }
}