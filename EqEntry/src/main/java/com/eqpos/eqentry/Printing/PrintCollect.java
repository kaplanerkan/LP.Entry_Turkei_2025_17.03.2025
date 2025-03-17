package com.eqpos.eqentry.Printing;

import com.eqpos.eqentry.DB.CustomerDao;
import com.eqpos.eqentry.Models.Customer;
import com.eqpos.eqentry.R;
import com.eqpos.eqentry.tools.Variables;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

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
}
