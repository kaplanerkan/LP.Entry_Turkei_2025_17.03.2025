package com.eqpos.eqentry.adapters;

import android.content.Context;
import android.graphics.Color;
//import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.eqpos.eqentry.db.InventurDao;
import com.eqpos.eqentry.models.InventurHolder;
import com.eqpos.eqentry.R;
import com.eqpos.eqentry.tools.SharedPrefUtil;
import com.eqpos.eqentry.tools.Variables;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dursu on 24.10.2018.
 */

public class InventurAdapter extends BaseAdapter {

    private Context context;

    private ArrayList<HashMap<String, String>> gList;

    private final int selectedWarehouseId = SharedPrefUtil.getInt(SharedPrefUtil.KEY_SELECTED_DEPO_ID,0);
    public InventurAdapter(Context context, ArrayList<HashMap<String, String>> list) {

        this.context = context;
        this.gList = list;

        if (selectedWarehouseId == 0){
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
        }

        holder.lblPlu.setText(gList.get(position).get("plu"));
        holder.lblProductName.setText(gList.get(position).get("productname"));
        holder.lblCurr.setText(gList.get(position).get("currentquantity"));
        holder.lblDiff.setText(gList.get(position).get("difference"));
        holder.edNew.setText(gList.get(position).get("newquantity"));

        holder.edNew.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                double currQuantity = 0;
                try {
                    currQuantity = Variables.strToDouble(gList.get(position).get("currentquantity"));
                } catch (ParseException e) {
                    e.printStackTrace();
                    currQuantity = 0.0;
                }
                double newQuantity = 0;
                try {
                    newQuantity = Variables.strToDouble(gList.get(position).get("newquantity"));
                } catch (ParseException e) {
                    e.printStackTrace();
                    newQuantity = 0.0;
                }
                if (currQuantity == newQuantity) {
                    holder.edNew.setText("");
                }
            } else {
                if (holder.edNew.getText().toString().isEmpty())
                    holder.edNew.setText(gList.get(position).get("currentquantity"));
            }
        });

        holder.btAdd.setOnClickListener(v -> updateQuantity(position, row, holder.lblCurr, holder.lblDiff, holder.edNew, 1.0));
        holder.btDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateQuantity(position, row, holder.lblCurr, holder.lblDiff, holder.edNew, -1.0);
            }
        });

        holder.edNew.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Double value = 0.0;
                try {
                    value = Variables.strToDouble(s.toString());
                } catch (ParseException e) {
                    return;
                }
                double diff = 0;
                try {
                    diff = Variables.strToDouble(gList.get(position).get("currentquantity"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                diff = value - diff;
                if (diff != 0.0) {
                    row.setBackgroundColor(Color.LTGRAY);
                }
                holder.lblDiff.setText(String.valueOf(diff));
                gList.get(position).put("newquantity", Variables.doubleToStr(value, 0));
                gList.get(position).put("difference", Variables.doubleToStr(diff,0));

                InventurDao.changeNewStock(Integer.parseInt(gList.get(position).get("productid")), value, selectedWarehouseId);
            }
        });
        return row;
    }

    private void updateQuantity(int position, View row, TextView lblCurr, TextView lblDiff, EditText edNew, Double value) {
        Double amount = 0.0;
        try {
            amount = Variables.strToDouble(edNew.getText().toString());
        } catch (ParseException ex) {
            return;
        }

        amount += value;
        if (amount < 0) {
            amount = 0.0;
        }
        edNew.setText(Variables.doubleToStr(amount, 0));
        double diff = 0;
        try {
            diff = Variables.strToDouble(gList.get(position).get("currentquantity"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        diff = amount - diff;
        if (diff != 0.0) {
            row.setBackgroundColor(Color.LTGRAY);
        }
        lblDiff.setText(String.valueOf(diff));
        gList.get(position).put("newquantity", Variables.doubleToStr(amount,2));
        gList.get(position).put("difference", Variables.doubleToStr(diff,2));


        InventurDao.changeNewStock(Integer.parseInt(gList.get(position).get("productid")), amount, selectedWarehouseId);
    }
}


