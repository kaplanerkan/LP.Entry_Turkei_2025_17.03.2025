package com.eqpos.eqentry.views.varyants;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.eqpos.eqentry.db.Database;
import com.eqpos.eqentry.models.VaryantModelWithBadget;

import java.util.List;

public class VaryantViewModelAltGruplar extends AndroidViewModel {
    private final Database dbHelper;
    private final LiveData<List<VaryantModelWithBadget>> varyantsAltGruplarLiveData;


    public VaryantViewModelAltGruplar(Application application, int parentid) {
        super(application);
        dbHelper = new Database(application);
        varyantsAltGruplarLiveData = dbHelper.getAllVaryantAltGruplarLiveData(parentid);
    }

    public LiveData<List<VaryantModelWithBadget>> getVaryantsAltGruplarLiveData() {
        return varyantsAltGruplarLiveData;
    }

    public void addNewAltGrupVaryant(String tanim, int sira, String aciklama, int parentid) {
        dbHelper.addNewAltGroupVaryant(tanim, sira, aciklama, parentid);
    }


}
