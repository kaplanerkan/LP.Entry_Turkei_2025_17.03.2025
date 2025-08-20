package com.eqpos.eqentry.views.varyants;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.eqpos.eqentry.db.Database;
import com.eqpos.eqentry.models.VaryantModel;

import java.util.List;

public class VaryantViewModelAltGruplar extends AndroidViewModel {
    private final Database dbHelper;
    private final LiveData<List<VaryantModel>> varyantsAltGruplarLiveData;


    public VaryantViewModelAltGruplar(Application application, int parentid) {
        super(application);
        dbHelper = new Database(application);
        varyantsAltGruplarLiveData = dbHelper.getAllVaryantAltGruplarLiveData(parentid);
    }

    public LiveData<List<VaryantModel>> getVaryantsAltGruplarLiveData() {
        return varyantsAltGruplarLiveData;
    }

    public void addNewAltGrupVaryant(String tanim, int sira, String aciklama, int parentid) {
        dbHelper.addNewAltGroupVaryant(tanim, sira, aciklama, parentid);
    }


}
