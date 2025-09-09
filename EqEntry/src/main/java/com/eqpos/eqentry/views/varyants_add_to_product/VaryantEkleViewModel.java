package com.eqpos.eqentry.views.varyants_add_to_product;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.eqpos.eqentry.db.Database;
import com.eqpos.eqentry.models.AddedVaryantsModel;

import java.util.List;

public class VaryantEkleViewModel extends AndroidViewModel {
    private final Database dbHelper;
  //  private final LiveData<List<AddedVaryantsModel>> varyantsAddedLiveData;


    public VaryantEkleViewModel(Application application) {
        super(application);
        dbHelper = new Database(application);
      //  varyantsAddedLiveData = dbHelper.getAllAddedVaryantsLiveData(); // O. cocugu burda olmayacak. yamuluyor.
    }

    public void addNewAddedSelected(int id, String barcode, String urunadi, double price, int anagrupid, int altgrupid, int plu) {
        dbHelper.addNewAddedSelected(id, barcode, urunadi, price, anagrupid, altgrupid, plu);
    }



}
