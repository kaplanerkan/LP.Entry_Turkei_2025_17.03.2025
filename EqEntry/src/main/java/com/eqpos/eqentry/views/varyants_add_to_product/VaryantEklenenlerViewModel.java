package com.eqpos.eqentry.views.varyants_add_to_product;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.eqpos.eqentry.db.Database;
import com.eqpos.eqentry.models.AddedVaryantsModel;
import com.eqpos.eqentry.models.VaryantModel;

import java.util.List;

public class VaryantEklenenlerViewModel extends AndroidViewModel {
    private final Database dbHelper;
    private LiveData<List<AddedVaryantsModel>> varyantsAddedLiveData;


    public VaryantEklenenlerViewModel(Application application) {
        super(application);
        dbHelper = new Database(application);

    }

    public LiveData<List<AddedVaryantsModel>> getVaryantsAddedLiveData() {
        varyantsAddedLiveData = dbHelper.getAllAddedVaryantsLiveData();
        return varyantsAddedLiveData;
    }


    public void removeSelectedVaryant(String urunadi) {
        dbHelper.deleteAddedSelected(urunadi);
    }

    public void updateVaryant(String urunadi, String barcode, int plu) {
        dbHelper.updateAddedVaryant(urunadi, barcode, plu);
    }

}
