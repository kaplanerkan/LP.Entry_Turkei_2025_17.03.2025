package com.eqpos.eqentry.views.depo_secimi;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.eqpos.eqentry.databinding.ItemDepoBinding;
import com.eqpos.eqentry.models.DepoModel;
import java.util.List;

public class DepoAdapter extends RecyclerView.Adapter<DepoAdapter.ViewHolder> {

    private List<DepoModel> depoList;
    private int selectedDepoId = -1; // Pozisyon yerine unique ID track et

    public DepoAdapter(List<DepoModel> depoList) {
        this.depoList = depoList;
    }

    public int getSelectedDepoId() {
        return selectedDepoId;
    }

    public void setSelectedDepoId(int id) {
        this.selectedDepoId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDepoBinding binding = ItemDepoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DepoModel depo = depoList.get(position);
        holder.binding.tvDepoId.setText("ID: " + depo.getId());
        holder.binding.tvDepoIsmi.setText(depo.getIsmi()); // Veya getDepoAdi(), modeline göre

        boolean isSelected = depo.getId() == selectedDepoId;
        holder.binding.rbSelect.setChecked(isSelected);

        if (isSelected) {
            holder.binding.itemRoot.setBackgroundColor(Color.parseColor("#E0FFE0")); // Hafif yeşil
        } else {
            holder.binding.itemRoot.setBackgroundColor(Color.WHITE); // Eski renk
        }

        holder.binding.itemRoot.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                DepoModel currentDepo = depoList.get(currentPosition);
                if (selectedDepoId != currentDepo.getId()) {
                    selectedDepoId = currentDepo.getId();
                    notifyDataSetChanged();
                }
            }
        });

        holder.binding.rbSelect.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                DepoModel currentDepo = depoList.get(currentPosition);
                if (selectedDepoId != currentDepo.getId()) {
                    selectedDepoId = currentDepo.getId();
                    notifyDataSetChanged();
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return depoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemDepoBinding binding;

        ViewHolder(ItemDepoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}