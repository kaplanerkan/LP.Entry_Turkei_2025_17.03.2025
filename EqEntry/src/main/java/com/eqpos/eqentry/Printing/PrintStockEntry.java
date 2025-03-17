package com.eqpos.eqentry.Printing;

import com.eqpos.eqentry.DB.InvoiceDao;
import com.eqpos.eqentry.DB.StockEntryDao;
import com.eqpos.eqentry.R;
import com.eqpos.eqentry.tools.Variables;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.eqpos.eqentry.DB.CustomerDao.getCustomerNewBalance;

/**
 * Created by dursu on 28.11.2018.
 */

public class PrintStockEntry {

    public static void printDelivery(long deliveryId) {
        HashMap<String, String> header = StockEntryDao.getDeliveryHeader(deliveryId);
        ArrayList<HashMap<String, String>> detail = StockEntryDao.getDeliveryDetail(deliveryId);


        PrintDao.printRow(header.get("customername"), false);
        PrintDao.printRow(header.get("customeraddress"), false);
        PrintDao.printRow(Variables.context.getString(R.string.user)+": "+ Variables.userName, false );
        PrintDao.printRow(Variables.context.getString(R.string.number)+": "+ header.get("documentnumber"), false );
        PrintDao.printRow(Variables.context.getString(R.string.date)+": "+ header.get("documentdate")+ " " + header.get("documenttime"), false);
        PrintDao.printDoubleLine(48);
        PrintDao.printEmptyRow(1);

        for (int i=0; i<detail.size(); i++) {
            PrintDao.printRow(detail.get(i).get("productname"), false);
            PrintDao.printRow(detail.get(i).get("packageamount"), detail.get(i).get("amount")+detail.get(i).get("unitename"),
                    detail.get(i).get("costprice"), detail.get(i).get("total"));
        }
        PrintDao.printLine(48);
        PrintDao.printRow("",Variables.context.getString(R.string.total)+": ", header.get("total"));
        PrintDao.printEmptyRow(1);
        PrintDao.printRow(header.get("amountstr"), false);
        PrintDao.printEmptyRow(1);

        PrintDao.printRow(Variables.context.getString(R.string.receiver), "", "", Variables.context.getString(R.string.signature));
        PrintDao.printEmptyRow(7);
        PrintDao.printRow("-", false);
    }
}
