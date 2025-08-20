package com.eqpos.eqentry.views.varyants;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.eqpos.eqentry.db.Database;
import com.eqpos.eqentry.models.VaryantModel;

import java.util.List;

public class VaryantViewModel extends AndroidViewModel {
    private final Database dbHelper;
    private final LiveData<List<VaryantModel>> varyantsLiveData;


    public VaryantViewModel(Application application) {
        super(application);
        dbHelper = new Database(application);
        varyantsLiveData = dbHelper.getAllVaryantsLiveData();
    }

    public LiveData<List<VaryantModel>> getVaryantsLiveData() {
        return varyantsLiveData;
    }

    public void addNewMainVaryant(String tanim, int sira) {
        dbHelper.addNewMainVaryant(tanim, sira);
    }
}
