package com.eqpos.eqentry.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;


import com.eqpos.eqentry.DB.Dao;
import com.eqpos.eqentry.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: drcakir
 * Created Date: 1/6/13 8:46 PM
 */

public class ButtonAdapter_ProductGroup extends BaseAdapter {
    private Context mContext;
    private List<HashMap<String,String>> mList=null;
    private OnClickListener mClickListener;

    public ButtonAdapter_ProductGroup(Context c, OnClickListener clickListener, List<HashMap<String, String>> list) {
        mContext = c;
        mClickListener = clickListener;
        mList = list;
    }

    // Total number of things contained within the adapter
    public int getCount() {
        return mList.size();
    }

    // Require for structure, not really used in my code.
    public Object getItem(int position) {
        return null;
    }

    // Require for structure, not really used in my code. Can
    // be used to get the id of an item in the adapter for
    // manual control.
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position,
                        View convertView, ViewGroup parent) {
        Button btn;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            btn = new Button(mContext);
            btn.setLayoutParams(new GridView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT, Dao.groupButonSize));
            btn.setPadding(8, 8, 8, 8);
        }
        else {
            btn = (Button) convertView;
        }

        btn.setText(mList.get(position).get("groupname"));
        if (mClickListener != null)
            btn.setOnClickListener(mClickListener);


        btn.setBackgroundColor(Color.parseColor("#f1f1f1"));
        btn.setTextAppearance(mContext, R.style.ButtonProductGroup);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, Dao.groupFontSize );


        btn.setId(position);

        return btn;
    }
}
