package com.eqpos.eqentry.db;

import com.eqpos.eqentry.models.BarcodeSettings;


/**
 * Created by dursu on 20.05.2019.
 */

public class BarcodeDao {
    private static Database Db;

    public static void saveBarcodeSettings(boolean isQuantity, String prefix, int plu, int value) {
        String lPre = "P_";
        if (isQuantity)
            lPre = "Q_";

        SettingsDao.setStrValue(lPre + "PREFIX", prefix);
        SettingsDao.setIntValue(lPre + "PLU", plu);
        SettingsDao.setIntValue(lPre + "VALUE", value);
    }

    public static BarcodeSettings getBarcodeSettings() {
        BarcodeSettings settings = new BarcodeSettings();

        settings.setP_Prefix(SettingsDao.getStrValue("P_PREFIX"));
        settings.setP_PLU(SettingsDao.getIntValue("P_PLU"));
        settings.setP_Value(SettingsDao.getIntValue("P_VALUE"));
        settings.setQ_Prefix(SettingsDao.getStrValue("Q_PREFIX"));
        settings.setQ_PLU(SettingsDao.getIntValue("Q_PLU"));
        settings.setQ_Value(SettingsDao.getIntValue("Q_VALUE"));

        return settings;
    }
}
