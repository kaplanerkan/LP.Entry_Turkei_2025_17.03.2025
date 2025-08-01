package com.eqpos.eqentry.Printing;

import com.eqpos.eqentry.DB.CustomerDao;
import com.eqpos.eqentry.Models.Customer;
import com.eqpos.eqentry.R;
import com.eqpos.eqentry.tools.Variables;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.eqpos.eqentry.DB.CustomerDao.getCustomerNewBalance;

/**
 * Created by dursu on 16.01.2019.
 */

public class PrintCollect {
    public static void printInvoice(int collectId) {
        HashMap<String, String> header = CustomerDao.getCollectForPrint(collectId);
        Customer cus = CustomerDao.getCustomer(Integer.parseInt(header.get("customerid")));

        PrintDao.printRow(header.get("customername"), false);
        PrintDao.printRow(header.get("customeraddress"), false);
        PrintDao.printRow(header.get("taxoffice") + "  " + header.get("taxid"), false);
        PrintDao.printLine(48);
        PrintDao.printRow(Variables.context.getString(R.string.user) + ": " + Variables.userName, false);
        PrintDao.printRow(Variables.context.getString(R.string.number) + ": " + header.get("documentnumber"), false);
        PrintDao.printRow(Variables.context.getString(R.string.date) + ": " + header.get("collectdate") + " " + header.get("collecttime"), false);
        PrintDao.printEmptyRow(1);
        PrintDao.printRow(header.get("paymenttype"), Variables.context.getString(R.string.total) + ": " + header.get("amount"));
        PrintDao.printLine(48);
        if (!header.get("description").isEmpty()) {

            PrintDao.printRow(Variables.context.getString(R.string.description) + ":", false);
            PrintDao.printRow(header.get("description"), false);
            PrintDao.printLine(48);
        }

        double currentAmount = 0;
        try {
            currentAmount = Variables.strToDouble(header.get("amount"));
        }catch (ParseException e) {
            e.printStackTrace();
        }

        double lPreviousBalance = (cus.getOldBalance()+cus.getCollects()) -
                                  (cus.getCollects()-currentAmount);
        //double lPreviousBalance = (cus.getOldBalance()+currentAmount)

        double lNewBalance = lPreviousBalance - currentAmount;

        PrintDao.printRow("", "", Variables.context.getString(R.string.previousbalance) + ": ", Variables.doubleToStr(lPreviousBalance, 2));
        PrintDao.printRow("", "", Variables.context.getString(R.string.currentinvoicetotal) + ": ", Variables.doubleToStr(currentAmount, 2));
        PrintDao.printRow("", "", Variables.context.getString(R.string.newbalance) + ": ", Variables.doubleToStr(lNewBalance, 2));
        PrintDao.printEmptyRow(3);

        PrintDao.printRow(Variables.context.getString(R.string.receiver), "", "", Variables.context.getString(R.string.signature));
        PrintDao.printEmptyRow(13);
        PrintDao.printRow("-", false);
    }


    // 01.08.2025 ERKAN
    public static void printInvoiceERKAN(int collectId) {
        HashMap<String, String> header = CustomerDao.getCollectForPrint(collectId);
        Customer cus = CustomerDao.getCustomer(Integer.parseInt(header.get("customerid")));

        PrintDao.printRow(header.get("customername"), false);
        PrintDao.printRow(header.get("customeraddress"), false);
        PrintDao.printRow(header.get("taxoffice") + "  " + header.get("taxid"), false);
        PrintDao.printLine(48);
        PrintDao.printRow(Variables.context.getString(R.string.user) + ": " + Variables.userName, false);
        PrintDao.printRow(Variables.context.getString(R.string.number) + ": " + header.get("documentnumber"), false);
        PrintDao.printRow(Variables.context.getString(R.string.date) + ": " + header.get("collectdate") + " " + header.get("collecttime"), false);
        PrintDao.printEmptyRow(1);
        PrintDao.printRow(header.get("paymenttype"), Variables.context.getString(R.string.total) + ": " + header.get("amount"));
        PrintDao.printLine(48);
        if (!header.get("description").isEmpty()) {

            PrintDao.printRow(Variables.context.getString(R.string.description) + ":", false);
            PrintDao.printRow(header.get("description"), false);
            PrintDao.printLine(48);
        }

        double currentAmount = 0;
        try {
            currentAmount = Variables.strToDouble(header.get("amount"));
        }catch (ParseException e) {
            e.printStackTrace();
        }

        double lPreviousBalance = (cus.getOldBalance()+cus.getCollects()) -
                (cus.getCollects()-currentAmount);
        //double lPreviousBalance = (cus.getOldBalance()+currentAmount)

        double lNewBalance = lPreviousBalance - currentAmount;


        // 30.07.2025 ERKAN
        double oncekiBakiye = cus.getOldBalance();
        double faturaBakiye = cus.getBalance() + cus.getCollects();
        double yeniBakiye = (oncekiBakiye+faturaBakiye) - currentAmount;
        // Önceki bakiye
        //PrintDao.printRow("", "", Variables.context.getString(R.string.previousbalance) + ": ", Variables.doubleToStr(lPreviousBalance, 2));
        PrintDao.printRow("", "", Variables.context.getString(R.string.previousbalance) + ": ", Variables.doubleToStr(oncekiBakiye, 2));    // 350,00

        // Fatura bakiyesi
        //PrintDao.printRow("", "", Variables.context.getString(R.string.newbalance) + ": ", Variables.doubleToStr(lNewBalance, 2));
        PrintDao.printRow("", "", Variables.context.getString(R.string.newbalance) + ": ", Variables.doubleToStr(faturaBakiye, 2));

        // Tahsilat
        //PrintDao.printRow("", "", Variables.context.getString(R.string.currentinvoicetotal) + ": ", Variables.doubleToStr(currentAmount, 2));
        PrintDao.printRow("", "", "Tahsilat: ", Variables.doubleToStr(currentAmount, 2));    // 100,00


        // Yeni bakiye
        PrintDao.printRow("", "", "Yeni Bakiye: ", Variables.doubleToStr(yeniBakiye, 2));    // 100,00

        PrintDao.printEmptyRow(3);

        PrintDao.printRow(Variables.context.getString(R.string.receiver), "", "", Variables.context.getString(R.string.signature));
        PrintDao.printEmptyRow(13);
        PrintDao.printRow("-", false);
    }



    /**
     * Müşterinin tüm tahsilatlarını yazdırır.
     *
     * @param customerId Müşteri ID'si
     *  01.08.2025: ERKAN
     */
    public static void printAllCollects(long customerId) {
        ArrayList<HashMap<String, String>> header = CustomerDao.getAllCollectList(customerId);

        // Liste boşsa bilgi ver
        if (header == null || header.isEmpty()) {
            System.out.println("Liste boş, veri bulunamadı.");
            return;
        }

        double oncekiBakiye = 0.0;
        double faturaBakiye = 0.0;
        double yeniBakiye = 0.0;
        double tahsilat = 0.0;

        Customer cus = CustomerDao.getCustomer(customerId);
        oncekiBakiye = cus.getOldBalance();
        faturaBakiye = cus.getBalance() ;

        // Listeyi döngü ile gez
        for (HashMap<String, String> map : header) {
            // Müşteri bilgilerini al

            // Başlık bilgileri
            PrintDao.printRow(map.get("customername"), false);
            PrintDao.printRow(map.get("customeraddress") != null ? map.get("customeraddress") : "", false);
            PrintDao.printRow((map.get("taxoffice") != null ? map.get("taxoffice") : "") + "  " +
                    (map.get("taxid") != null ? map.get("taxid") : ""), false);
            PrintDao.printLine(48);
            PrintDao.printRow(Variables.context.getString(R.string.user) + ": " + Variables.userName, false);
            PrintDao.printRow(Variables.context.getString(R.string.number) + ": " + map.get("documentnumber"), false);
            PrintDao.printRow(Variables.context.getString(R.string.date) + ": " + map.get("collectdate"), false);
            PrintDao.printEmptyRow(1);
            PrintDao.printRow(map.get("paymenttype"), Variables.context.getString(R.string.total) + ": " + map.get("amount"));

            PrintDao.printLine(48);

            // Açıklama varsa yazdır
            if (map.get("description") != null && !map.get("description").isEmpty()) {
                PrintDao.printRow(Variables.context.getString(R.string.description) + ":", false);
                PrintDao.printRow(map.get("description"), false);
                PrintDao.printLine(48);
            }

            // Mevcut tutarı al
            double currentAmount = 0;
            try {
                currentAmount = Variables.strToDouble(map.get("amount"));
                tahsilat += currentAmount; // Tahsilat toplamını güncelle
            } catch (ParseException e) {
                e.printStackTrace();
            }


        } // FOR


        // Bakiye hesaplamaları
//        double oncekiBakiye = cus.getOldBalance();
//        double faturaBakiye = cus.getBalance() + cus.getCollects();
//        double yeniBakiye = (oncekiBakiye + faturaBakiye) - currentAmount;

        yeniBakiye = oncekiBakiye + faturaBakiye ;
        faturaBakiye += tahsilat;

        // Önceki bakiye
        PrintDao.printRowERKAN("Önceki Bakiye:   ", Variables.doubleToStr(oncekiBakiye, 2));
        // Fatura bakiyesi
        PrintDao.printRowERKAN("Fatura Bakiyesi: ", Variables.doubleToStr(faturaBakiye, 2));
        // Tahsilat
        PrintDao.printRowERKAN("Tahsilat:        ", Variables.doubleToStr(tahsilat, 2));
        // Yeni bakiye
        PrintDao.printRowERKAN("Yeni Bakiye:     ", Variables.doubleToStr(yeniBakiye, 2));

        PrintDao.printEmptyRow(3);

        // Alıcı ve imza
        PrintDao.printRow(Variables.context.getString(R.string.receiver), "", "",
                Variables.context.getString(R.string.signature));
        PrintDao.printEmptyRow(13);
        PrintDao.printRow("-", false);


    }


}
