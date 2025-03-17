package com.eqpos.eqentry.Models;

/**
 * Created by dursu on 20.05.2019.
 */

public class BarcodeSettings {
    private String Q_Prefix = "";
    private int Q_PLU = 5;
    private int Q_Value = 5;

    private String P_Prefix = "";
    private int P_PLU = 5;
    private int P_Value = 5;

    public String getQ_Prefix() {
        return Q_Prefix;
    }

    public void setQ_Prefix(String q_Prefix) {
        Q_Prefix = q_Prefix;
    }

    public int getQ_PLU() {
        return Q_PLU;
    }

    public void setQ_PLU(int q_PLU) {
        Q_PLU = q_PLU;
    }

    public int getQ_Value() {
        return Q_Value;
    }

    public void setQ_Value(int q_Value) {
        Q_Value = q_Value;
    }

    public String getP_Prefix() {
        return P_Prefix;
    }

    public void setP_Prefix(String p_Prefix) {
        P_Prefix = p_Prefix;
    }

    public int getP_PLU() {
        return P_PLU;
    }

    public void setP_PLU(int p_PLU) {
        P_PLU = p_PLU;
    }

    public int getP_Value() {
        return P_Value;
    }

    public void setP_Value(int p_Value) {
        P_Value = p_Value;
    }
}
