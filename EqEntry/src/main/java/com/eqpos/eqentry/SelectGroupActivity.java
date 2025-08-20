package com.eqpos.eqentry;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;

import com.eqpos.eqentry.adapters.ButtonAdapter_ProductGroup;
import com.eqpos.eqentry.db.ProductDao;

import java.util.ArrayList;
import java.util.HashMap;

public class SelectGroupActivity extends AppCompatActivity {
    private GridView gvGroupList;
    private  ArrayList<HashMap<String, String>> gGroupList;
    private View.OnClickListener onClickListener_ProductGroup = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group);
        this.setTitle(R.string.productlist);

        gvGroupList = (GridView) findViewById(R.id.gv_selectgroup);

        /*
         * Button adapterde hangi butona tiklandigini anlamak icin adaptere parametre
         * olarak gonderiyorum.
         */
        onClickListener_ProductGroup = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                setValuesAndFinish(view.getId());
            }
        };
        listProductGroups();
    }


    private void listProductGroups() {
        gGroupList = ProductDao.getGroupList();

        ButtonAdapter_ProductGroup adp = new ButtonAdapter_ProductGroup(this, onClickListener_ProductGroup, gGroupList);
        gvGroupList.setAdapter(adp);
    }

    private void setValuesAndFinish(int position) {
        Intent out = new Intent();
        out.putExtra("groupid", Integer.parseInt(gGroupList.get(position).get("id")));
        out.putExtra("groupname", gGroupList.get(position).get("groupname"));

        setResult(RESULT_OK, out);
        finish();
    }
}
