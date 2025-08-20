package com.eqpos.eqentry.printing;

import com.eqpos.eqentry.db.CustomerDao;
import com.eqpos.eqentry.db.InvoiceDao;
import com.eqpos.eqentry.R;
import com.eqpos.eqentry.tools.Variables;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.eqpos.eqentry.db.CustomerDao.*;

/**
 * Created by dursu on 28.11.2018.
 */

public class PrintInvoice {

    public static void printInvoice_old1(long invoiceId) {
        HashMap<String, String> header = InvoiceDao.getInvoiceHeader(invoiceId);
        ArrayList<HashMap<String, String>> detail = InvoiceDao.getInvoiceDetail(invoiceId);

        PrintDao.printRow(header.get("customername"), false);
        PrintDao.printRow(header.get("customeraddress"), false);
        PrintDao.printRow(Variables.context.getString(R.string.user)+": "+ Variables.userName, false );
        PrintDao.printRow(Variables.context.getString(R.string.number)+": "+ header.get("invoicenumber"), false );
        PrintDao.printRow(Variables.context.getString(R.string.date)+": "+ header.get("invoicedate")+ " " + header.get("invoicetime"), false);
        PrintDao.printDoubleLine(48);
        PrintDao.printEmptyRow(1);

        for (int i=0; i<detail.size(); i++) {
            PrintDao.printRow(detail.get(i).get("productname"), false);

            if (Variables.isPrintTaxOnInvoice) {
                PrintDao.printRow(detail.get(i).get("packageamount"), detail.get(i).get("amount") + detail.get(i).get("unitename"), detail.get(i).get("unitprice"), detail.get(i).get("discount1"), detail.get(i).get("total"));
            } else {
                PrintDao.printRow(detail.get(i).get("packageamount"), detail.get(i).get("amount") + detail.get(i).get("unitename"), detail.get(i).get("unitprice"), detail.get(i).get("discount1"), detail.get(i).get("subtotal"));
            }
        }
        PrintDao.printLine(48);
        if (Variables.isPrintTaxOnInvoice) {
            PrintDao.printRow("",Variables.context.getString(R.string.total)+": ", header.get("total"));
            PrintDao.printRow("", Variables.context.getString(R.string.tax) + ": ", header.get("taxamount"));
        }
        PrintDao.printRow("",Variables.context.getString(R.string.discount)+": ", header.get("discount"));
        PrintDao.printRow("",Variables.context.getString(R.string.subtotal)+": ", header.get("subtotal"));
        PrintDao.printEmptyRow(3);
        PrintDao.printRow(header.get("amountstr"), false);

        double lNewBalance = getCustomerNewBalance(Integer.parseInt(header.get("customerid")));
        double lPreviousBalance = 0;
        try {
            lPreviousBalance = lNewBalance - Variables.strToDouble(header.get("subtotal"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (Variables.isPrintTaxOnInvoice) {
            PrintDao.printEmptyRow(2);
            PrintDao.printRow(Variables.context.getString(R.string.previousbalance)+": ", Variables.doubleToStr(lPreviousBalance,2), "");
            PrintDao.printRow(Variables.context.getString(R.string.currentinvoicetotal)+": ", header.get("subtotal"), "");
            PrintDao.printRow(Variables.context.getString(R.string.newbalance)+": ", Variables.doubleToStr(lNewBalance,2),"");
            PrintDao.printEmptyRow(3);
        }
    }

    public static void printInvoice_old2(long invoiceId) {
        HashMap<String, String> header = InvoiceDao.getInvoiceHeader(invoiceId);
        ArrayList<HashMap<String, String>> detail = InvoiceDao.getInvoiceDetail(invoiceId);
        Variables.getFirmInfo();

        PrintDao.printRow(Variables.companyName, true);
        PrintDao.printRow(Variables.companyAddres, true);
        PrintDao.printRow(Variables.companyPhone, true);
        PrintDao.printLine(48);

        if (Integer.parseInt(header.get("customerid"))>0) {
            PrintDao.printRow(Variables.context.getString(R.string.customer) + ": " + header.get("customername"), false);
            PrintDao.printRow(Variables.context.getString(R.string.taxid) + ": " + header.get("customeraddress"), false);
        }
        PrintDao.printRow(Variables.context.getString(R.string.date)+": "+ header.get("invoicedate"), false);
        PrintDao.printLine(48);

        PrintDao.printRow(0,Variables.context.getString(R.string.amount),
                Variables.context.getString(R.string.productname),
                Variables.context.getString(R.string.currentprice),
                Variables.context.getString(R.string.total));
        PrintDao.printLine(48);

        for (int i=0; i<detail.size(); i++) {

            if (Variables.isPrintTaxOnInvoice) {
                PrintDao.printRow(i+1,detail.get(i).get("amount"),
                                  detail.get(i).get("productname"),
                                  detail.get(i).get("unitprice"),
                                  detail.get(i).get("total"));
            } else {
                PrintDao.printRow(i+1,detail.get(i).get("amount"),
                        detail.get(i).get("productname"),
                        detail.get(i).get("unitprice"),
                        detail.get(i).get("subtotal"));
            }
        }
        PrintDao.printLine(48);
        PrintDao.printRow(Variables.context.getString(R.string.total)+": ", header.get("total"));

        if (Integer.parseInt(header.get("customerid"))>0) {
            double lNewBalance = getCustomerNewBalance(Integer.parseInt(header.get("customerid")));
            double lPreviousBalance = 0;
            try {
                lPreviousBalance = lNewBalance - Variables.strToDouble(header.get("total"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            PrintDao.printRow(Variables.context.getString(R.string.previousbalance) + ": ", Variables.doubleToStr(lPreviousBalance, 2), "");
            PrintDao.printRow(Variables.context.getString(R.string.newbalance) + ": ", Variables.doubleToStr(lNewBalance, 2), "");
        }
        PrintDao.printEmptyRow(1);
        PrintDao.printRow(Variables.companySlogan, true);
        PrintDao.printEmptyRow(3);
    }




    public static void printInvoice(long invoiceId) {
        HashMap<String, String> header = InvoiceDao.getInvoiceHeader(invoiceId);
        ArrayList<HashMap<String, String>> detail = InvoiceDao.getInvoiceDetail(invoiceId);

        double lNewBalance = 0;
        double lPreviousBalance = 0;
        double lInvoiceTotal = 0;
        String lProductName = "";

        try {
            lInvoiceTotal = Variables.strToDouble(header.get("subtotal"));
            lPreviousBalance = getCustomerLastBalance(Integer.parseInt(header.get("customerid")));
//
//
//            if (lPreviousBalance>0) {
//               lPreviousBalance = lPreviousBalance - lInvoiceTotal ;
//            }
         //   lPreviousBalance = CustomerDao.getLastBalancaNachRechnung( invoiceId, Long.parseLong(header.get("customerid")));

            lNewBalance = lPreviousBalance + lInvoiceTotal ;
        } catch (ParseException e) {
            e.printStackTrace();
        }


        Variables.getFirmInfo();

        PrintDao.printRowLarge(Variables.companyName, true);
        PrintDao.printRow(Variables.companyAddres, true);
        PrintDao.printRow(Variables.companyPhone, true);
        PrintDao.printLine(48);

        if (Integer.parseInt(header.get("customerid")) > 0) {
            PrintDao.printRow(Variables.context.getString(R.string.customer) + ": " + header.get("customername"), false);
            PrintDao.printRow(Variables.context.getString(R.string.taxid) + ": " + header.get("customeraddress"), false);
            //PrintDao.printRow(Variables.context.getString(R.string.previousbalance) + ": " + Variables.doubleToStr(lPreviousBalance, 2),false);
            PrintDao.printRow(Variables.context.getString(R.string.previousbalance) + ": " +
                    Variables.doubleToStr(lPreviousBalance, 2),false);

        }

        PrintDao.printRow(Variables.context.getString(R.string.user) + ": " + Variables.userName, false);
        PrintDao.printRow(Variables.context.getString(R.string.number) + ": " + header.get("invoicenumber"), false);
        PrintDao.printRow(Variables.context.getString(R.string.date) + ": " + header.get("invoicedate") + " " + header.get("invoicetime"), false);
        PrintDao.printDoubleLine(48);
        PrintDao.printEmptyRow(1);

        PrintDao.printRow(Variables.context.getString(R.string.productname),
                Variables.context.getString(R.string.amount),
                Variables.context.getString(R.string.currentprice),
                Variables.context.getString(R.string.discount),
                Variables.context.getString(R.string.total));

        PrintDao.printLine(48);

        for (int i = 0; i < detail.size(); i++) {
            lProductName = detail.get(i).get("origin");
            if (lProductName!=""){
                lProductName = lProductName + " - " + detail.get(i).get("productname");
            } else {
                lProductName = detail.get(i).get("productname");
            }

            PrintDao.printRow(lProductName, false);

            if (Variables.isPrintTaxOnInvoice) {
                PrintDao.printRow(detail.get(i).get("packageamount"), detail.get(i).get("amount") + detail.get(i).get("unitename"), detail.get(i).get("unitprice"), detail.get(i).get("discount1"), detail.get(i).get("total"));
            } else {
                PrintDao.printRow(detail.get(i).get("packageamount"), detail.get(i).get("amount") + detail.get(i).get("unitename"), detail.get(i).get("unitprice"), detail.get(i).get("discount1"), detail.get(i).get("subtotal"));
            }
        }
        PrintDao.printLine(48);
        if (Variables.isPrintTaxOnInvoice) {
            PrintDao.printRow("", Variables.context.getString(R.string.total) + ": ", header.get("total"));
            PrintDao.printRow("", Variables.context.getString(R.string.tax) + ": ", header.get("taxamount"));
        }
        PrintDao.printRow("", Variables.context.getString(R.string.discount) + ": ", header.get("discount"));
        PrintDao.printRow("", Variables.context.getString(R.string.subtotal) + ": ", header.get("subtotal"));
        PrintDao.printEmptyRow(1);
        PrintDao.printRow(header.get("amountstr"), false);

        if (Variables.isPrintTaxOnInvoice) {
            PrintDao.printEmptyRow(1);
            PrintDao.printRow("",Variables.context.getString(R.string.currentinvoicetotal) + ": ", header.get("subtotal"));


            //Erkan: 29.06.2024
            double getCustomerGuncelBalance = CustomerDao.getCustomerGuncelBalance(Integer.parseInt(header.get("customerid")));
            //getCustomerGuncelBalance += lNewBalance ;
            //PrintDao.printRow("",Variables.context.getString(R.string.newbalance) + ": ", Variables.doubleToStr(lNewBalance, 2));

            //PrintDao.printRow("",Variables.context.getString(R.string.newbalance) + ": ", Variables.doubleToStr(getCustomerGuncelBalance, 2));
            PrintDao.printRow("",Variables.context.getString(R.string.last_balance) + ": ", Variables.doubleToStr(lPreviousBalance, 2));
            PrintDao.printRow("",Variables.context.getString(R.string.totalbalance) + ": ", Variables.doubleToStr(lNewBalance, 2));




            PrintDao.printEmptyRow(1);
        }

        PrintDao.printRowBold(Variables.context.getString(R.string.notfinancial), true);
        PrintDao.printEmptyRow(3);
    }
}
