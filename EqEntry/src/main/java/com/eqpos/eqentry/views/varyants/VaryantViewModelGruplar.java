package com.eqpos.eqentry.views.varyants;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.eqpos.eqentry.db.Database;
import com.eqpos.eqentry.models.VaryantModelWithBadget;

import java.util.List;

public class VaryantViewModelGruplar extends AndroidViewModel {
    private final Database dbHelper;
    private final LiveData<List<VaryantModelWithBadget>> varyantsGrupolarLiveData;



    public VaryantViewModelGruplar(Application application, int parentid) {
        super(application);
        dbHelper = new Database(application);
        varyantsGrupolarLiveData = dbHelper.getAllVaryantGruplarLiveDataWithBadger(parentid);
    }

    public LiveData<List<VaryantModelWithBadget>> getVaryantsGruplarLiveData() {
        return varyantsGrupolarLiveData;
    }

    public void addNewGrupVaryant(String tanim, int sira, String aciklama, int parentid) {
        dbHelper.addNewGroupVaryant(tanim, sira, aciklama, parentid);
    }


    public void getVaryantEklenenGroupCountLiveData(int anagrupId) {
        dbHelper.getEklenGrupAdedLiveData(anagrupId);
    }

}
