package com.eqpos.eqentry.printing;

import com.eqpos.eqentry.db.Dao;
import com.eqpos.eqentry.tools.Variables;


import java.text.ParseException;

public class PrintLabel {

    public static void printLabel(String productname, String price, String barcode, String unitamount, String amountunite) throws ParseException {

        PrintDao.printProductName(productname, true);

        PrintDao.printLine(48);

        PrintDao.printPrice(price,Dao.Currency );

        if((Variables.strToDouble(unitamount) > 0) && Variables.showUnitPrice) {
            PrintUnitPrice(price, unitamount, amountunite, Dao.Currency);
        } else
            PrintDao.printEmptyRow(1);

        PrintDao.printBarcode(barcode, true,72);

        PrintDao.printRowWide(barcode, true);

        PrintDao.printEmptyRow(2);

    }

    private static void PrintUnitPrice(String price, String unitamount, String amountunite, String currency) {
        Double pricedouble = 0.0;
        Double unitamountdouble= 0.0;
        currency = " " + currency;

        try {
            pricedouble = Variables.strToDouble(price);
            unitamountdouble =Variables.strToDouble(unitamount);

            String lRow = " ";
            double lFiyat = 0.0;

            switch (amountunite) {
                case "Gr":
                    lFiyat = pricedouble / unitamountdouble; //1 gr fiyatı
                    lRow = "100gr=" + Variables.doubleToStr(Variables.roundTo(100 * lFiyat,2), 2) +
                            currency +
                            " 1kg=" + Variables.doubleToStr(Variables.roundTo(1000 * lFiyat,2), 2) +
                            currency;
                    break;

                case "Kg":
                    lFiyat = pricedouble / unitamountdouble; //1 kg fiyatı
                    lRow = "100gr=" +  Variables.doubleToStr(Variables.roundTo( lFiyat/10,2), 2)  +
                            currency +
                            " 1kg=" + Variables.doubleToStr(Variables.roundTo( lFiyat,2), 2)  +
                            currency;
                    break;

                case "Lt":
                    lFiyat = pricedouble / unitamountdouble;
                    lRow = "100ml=" + Variables.doubleToStr(Variables.roundTo( lFiyat/10,2), 2)  +
                            currency +
                            " 1Lt=" + Variables.doubleToStr(Variables.roundTo( lFiyat,2), 2)  +
                            currency;
                    break;

                case "Ml":

                case "Cl":
                    lFiyat = pricedouble / unitamountdouble; //1 ml fiyatı 1cl fiyatı programdan gelen unit
                    lRow = "100ml=" +  Variables.doubleToStr(Variables.roundTo(100 * lFiyat,2), 2)  +
                            currency +
                            " 1Lt=" + Variables.doubleToStr(Variables.roundTo(1000 * lFiyat,2), 2)  +
                            currency;
                    break;

                default:

            }
            PrintDao.printRow( lRow,true);

        } catch (ParseException ex) {
            throw new IllegalStateException("Unexpected value amountunite: " + amountunite);
        }
    }
}
