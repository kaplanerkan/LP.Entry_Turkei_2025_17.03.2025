package com.eqpos.eqentry.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.eqpos.eqentry.R;
import com.eqpos.eqentry.db.InventurDao;
import com.eqpos.eqentry.models.InventurHolder;
import com.eqpos.eqentry.tools.SharedPrefUtil;
import com.eqpos.eqentry.tools.Variables;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by e.Kaplan on 25.11.2025.
 */

public class InventurAdapter2 extends BaseAdapter {

    private int selectedWarehouseId = SharedPrefUtil.getInt(SharedPrefUtil.KEY_SELECTED_DEPO_ID, 0);
    private Context context;
    private ArrayList<HashMap<String, String>> gList;

    public InventurAdapter2(Context context, ArrayList<HashMap<String, String>> list) {
        this.context = context;
        this.gList = list;

        selectedWarehouseId = SharedPrefUtil.getInt(SharedPrefUtil.KEY_SELECTED_DEPO_ID, 0);
        if (selectedWarehouseId == 0) {
            Toast.makeText(context, "Lütfen depo seçiniz!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return gList.size();
    }

    @Override
    public Object getItem(int position) {
        return gList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final View row;
        final InventurHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.inventur_row, parent, false);

            holder = new InventurHolder();
            holder.lblPlu = (TextView) row.findViewById(R.id.lbl_inventur_row_plu);
            holder.lblProductName = (TextView) row.findViewById(R.id.lbl_inventur_row_productname);
            holder.lblCurr = (TextView) row.findViewById(R.id.lbl_inventur_row_currentstock);
            holder.lblDiff = (TextView) row.findViewById(R.id.lbl_inventur_row_difference);
            holder.edNew = (EditText) row.findViewById(R.id.ed_inventur_newstock);
            holder.btDecrease = (ImageButton) row.findViewById(R.id.bt_inventur_decrease);
            holder.btAdd = (ImageButton) row.findViewById(R.id.bt_inventur_addstock);
            holder.yeniAdet = (TextView) row.findViewById(R.id.lblYeniAdet);


            row.setTag(holder);
        } else {
            row = convertView;
            holder = (InventurHolder) row.getTag();
        }

        Double difference = null;
        try {
            difference = Variables.strToDouble(gList.get(position).get("difference"));
        } catch (ParseException e) {
            e.printStackTrace();
            difference = 0.0;
        }
        if (difference != 0.0) {
            row.setBackgroundColor(Color.LTGRAY);
        } else {
            row.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.lblPlu.setText(gList.get(position).get("plu"));
        holder.lblProductName.setText(gList.get(position).get("productname"));
        holder.lblCurr.setText(gList.get(position).get("currentquantity"));
        holder.lblDiff.setText(gList.get(position).get("difference"));
        holder.edNew.setText(gList.get(position).get("newquantity"));
        holder.yeniAdet.setText(gList.get(position).get("newquantity"));


        // edNew'i sadece okunabilir yap
        holder.edNew.setFocusable(false);
        holder.edNew.setClickable(false);

        holder.btAdd.setOnClickListener(v -> showQuantityInputDialog(position, row, holder, true));

        holder.btDecrease.setOnClickListener(v -> showQuantityInputDialog(position, row, holder, false));

        return row;
    }

    /**
     * Ekleme olacak ca TRUE, cikartma olacak ise FALSE
     *
     * @param position
     * @param row
     * @param holder
     * @param isAdd
     */
    private void showQuantityInputDialog(int position, View row, InventurHolder holder, boolean isAdd) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (isAdd){
            builder.setTitle("Yeni Stok Miktarı (+)");
        } else {
            builder.setTitle("Yeni Stok Miktarı (-)");
        }

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("Tamam", (dialog, which) -> {
            String inputText = input.getText().toString().trim();
            if (inputText.isEmpty()) {
                Toast.makeText(context, "Lütfen bir değer giriniz!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double enteredValue = Variables.strToDouble(inputText);
                double currentDifference = Variables.strToDouble(gList.get(position).get("difference"));
                double newQuantity;

                if (isAdd) {
                    // Ekleme işlemi
                    if (currentDifference == 0.0) {
                        newQuantity = enteredValue;
                    } else {
                        double currentNewQuantity = Variables.strToDouble(gList.get(position).get("newquantity"));
                        newQuantity = currentNewQuantity + enteredValue;
                    }
                } else {
                    // Çıkarma işlemi - yeniAdet'ten çıkar
                    double currentNewQuantity = Variables.strToDouble(gList.get(position).get("newquantity"));
                    newQuantity = currentNewQuantity - enteredValue;
                }

                // Negatif değerleri engelle
                if (newQuantity < 0) {
                    newQuantity = 0.0;
                }

                // Yeni difference hesapla
                double currentStock = Variables.strToDouble(gList.get(position).get("currentquantity"));
                double diff = newQuantity - currentStock;

                // UI'ı güncelle
                holder.edNew.setText(Variables.doubleToStr(newQuantity, 0));
                holder.yeniAdet.setText(Variables.doubleToStr(newQuantity, 0));
                holder.lblDiff.setText(String.valueOf(diff));

                // Arka plan rengini güncelle
                if (diff != 0.0) {
                    row.setBackgroundColor(Color.LTGRAY);
                } else {
                    row.setBackgroundColor(Color.TRANSPARENT);
                }

                // Liste ve veritabanını güncelle
                gList.get(position).put("newquantity", Variables.doubleToStr(newQuantity, 0));
                gList.get(position).put("difference", Variables.doubleToStr(diff, 0));

                if (diff < 0){
                    diff = 0 ;
                }
                InventurDao.changeNewStock_Erkan(
                        Integer.parseInt(gList.get(position).get("productid")),
                        newQuantity,
                        selectedWarehouseId,
                        (int) diff
                );

            } catch (ParseException e) {
                Toast.makeText(context, "Geçersiz değer!", Toast.LENGTH_SHORT).show();
                Log.e("InventurAdapter", "Parse error: " + e.getMessage());
            }
        });

        builder.setNegativeButton("İptal", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
