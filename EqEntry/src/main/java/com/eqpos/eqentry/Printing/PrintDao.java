package com.eqpos.eqentry.Printing;

import android.util.Log;

import com.eqpos.eqentry.tools.UnicodeFormatter;
import com.eqpos.eqentry.tools.Variables;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.Normalizer;

/**
 * Created by dursu on 28.11.2018.
 */

public class PrintDao {

    public static void printEmptyRow(int count) {
        try {
            OutputStream os = Variables.mBluetoothSocket.getOutputStream();
            for (int i=1; i<=count; i++) {
                os.write("\n".getBytes());
            }
        } catch (Exception e) {
            Log.e("PrintDao", "Printing ", e);
        }
    }

    public static void printBarcode(String barcode, boolean isCenter, int barkodType) {

        try {
            OutputStream os = Variables.mBluetoothSocket.getOutputStream();

            if (isCenter) {
                os.write(Formatter.centerAlign());
            }

            // Setting height
            int gs = 29;
            os.write(intToByteArray(gs));
            int h = 104;
            os.write(intToByteArray(h));
            int n = 302;
            os.write(intToByteArray(n));

            // Setting Width
            int gs_width = 29;
            os.write(intToByteArray(gs_width));
            int w = 119;
            os.write(intToByteArray(w));
            int n_width = 2;
            os.write(intToByteArray(n_width));

            //Print BarCode
            int gs1 = 29;
            os.write(intToByteArray(gs1));
            int k = 107;
            os.write(intToByteArray(k));
            int m = barkodType;
            os.write(intToByteArray(m));


            System.out.println("Barcode Length : " + barcode.length());
            int n1 = barcode.length();
            os.write(intToByteArray(n1));

            for (int i = 0; i < barcode.length(); i++) {
                os.write((barcode.charAt(i) + "").getBytes());
            }


            if(barcode.length() < 4 ) {
                printRow(" ",true);
                printRow(" ",true);
            }

            printEmptyRow(1);

        } catch (Exception e) {
            printEmptyRow(3);
            Log.e("PrintDao", "Printing ", e);
        }
    }
    public static byte intToByteArray(int value) {
        byte[] b = ByteBuffer.allocate(4).putInt(value).array();

        for (int k = 0; k < b.length; k++) {
            System.out.println("Selva  [" + k + "] = " + "0x"
                    + UnicodeFormatter.byteToHex(b[k]));
        }

        return b[3];
    }

    public static void printDoubleLine(int lineSize) {
        try {
            OutputStream os = Variables.mBluetoothSocket.getOutputStream();
            byte[] cc = new byte[]{0x1B,0x21,0x00};  // 0- normal size text
            os.write(cc);

            for (int i=1; i<=lineSize; i++) {
                os.write("=".getBytes());
            }
            os.write("\n".getBytes());
        } catch (Exception e) {
            Log.e("PrintDao", "Printing ", e);
        }
    }
    public static void printLine(int lineSize) {
        try {
            OutputStream os = Variables.mBluetoothSocket.getOutputStream();
            byte[] cc = new byte[]{0x1B,0x21,0x00};  // 0- normal size text
            os.write(cc);

            for (int i=1; i<=lineSize; i++) {
                os.write("-".getBytes());
            }
            os.write("\n".getBytes());
        } catch (Exception e) {
            Log.e("PrintDao", "Printing ", e);
        }
    }
    public static void printStar() {
        try {
            OutputStream os = Variables.mBluetoothSocket.getOutputStream();
            byte[] cc = new byte[]{0x1B,0x21,0x00};  // 0- normal size text
            os.write(cc);

            for (int i=1; i<=30; i++) {
                os.write("*".getBytes());
            }
            os.write("\n".getBytes());
        } catch (Exception e) {
            Log.e("PrintDao", "Printing ", e);
        }
    }


    public  static void printRow(String text, boolean isCenter) {
        try {
            OutputStream os = Variables.mBluetoothSocket.getOutputStream();

            byte[] cc = new byte[]{0x1B,0x21,0x00};  // 0- normal size text
            os.write(cc);

            int starting = 1;
            if (isCenter) {
                os.write(Formatter.centerAlign());
            } else {
                os.write(Formatter.leftAlign());
            }

            os.write(String.format("%1$"+starting+"s\n", convertCharacters(text)).getBytes("iso-8859-1"));

        } catch (Exception e) {
            Log.e("PrintDao", "Printing ", e);
        }
    }

    public  static void printRowLarge(String text, boolean isCenter) {
        try {
            OutputStream os = Variables.mBluetoothSocket.getOutputStream();
            byte[] bb3 = new byte[]{0x1B,0x21,0x38}; // 3- bold with large text
            os.write(bb3);

            int starting = 1;
            if (isCenter) {
                os.write(Formatter.centerAlign());
            }

            os.write(String.format("%1$"+starting+"s\n", convertCharacters(text)).getBytes("UTF8" ));
        } catch (Exception e) {
            Log.e("PrintDao", "Printing ", e);
        }
    }

    public  static void printRowBold(String text, boolean isCenter) {
        try {
            OutputStream os = Variables.mBluetoothSocket.getOutputStream();

            byte[] cc = new byte[]{0x1B,0x21,0x8};  // bold
            os.write(cc);

            int starting = 1;
            if (isCenter) {
                os.write(Formatter.centerAlign());
            }

            os.write(String.format("%1$"+starting+"s\n", convertCharacters(text)).getBytes("UTF8" ));
        } catch (Exception e) {
            Log.e("PrintDao", "Printing ", e);
        }
    }

    public  static void printRowHigh(String text, boolean isCenter) {
        try {
            OutputStream os = Variables.mBluetoothSocket.getOutputStream();
            byte[] cc = new byte[]{0x1B,0x21,0x10};  // high

            os.write(cc);

            int starting = 1;
            if (isCenter) {
                os.write(Formatter.centerAlign());
            }

            os.write(String.format("%1$"+starting+"s\n", convertCharacters(text)).getBytes("UTF8" ));
        } catch (Exception e) {
            Log.e("PrintDao", "Printing ", e);
        }
    }


    public static void printPrice(String text,String money) {
        try {

            OutputStream os = Variables.mBluetoothSocket.getOutputStream();
            os.write(Formatter.centerAlign());

            byte[] bb3 = new byte[]{0x1B,0x21,0x38}; // 3- bold with large text

            os.write(bb3);

            os.write(0x1C);
            os.write(0x2E); //cancel character chinnese
            os.write(0x1B);
            os.write(0x74);
            os.write(0x10);

            if(money=="£") {
                os.write(money.getBytes("iso-8859-1" ) );//POUND
                os.write(convertCharacters(text).getBytes("UTF8" ) );
            }
            else {
                os.write(convertCharacters(text).getBytes("UTF8"));
                os.write(convertCharacters(" " + money).getBytes("UTF8"));
            }
            os.write("\n".getBytes());
        } catch (Exception e) {
            Log.e("PrintDao", "Printing ", e);
        }
    }


    public static void printRowWide(String text, boolean isCenter) {
        try {
            OutputStream os = Variables.mBluetoothSocket.getOutputStream();
            byte[] cc = new byte[]{0x1B,0x21,0x20};  // 0- normal size text
            os.write(cc);

            int starting = 2;
            if (isCenter) {
                os.write(Formatter.centerAlign());
            }

            os.write(String.format("%1$"+starting+"s\n", convertCharacters(text)).getBytes("UTF-8" ));
            //os.write("\n".getBytes());


        } catch (Exception e) {
            Log.e("PrintDao", "Printing ", e);
        }
    }

    public static void printProductName(String text, boolean isCenter) {
        try {
            OutputStream os = Variables.mBluetoothSocket.getOutputStream();
            byte[] cc = new byte[]{0x1B,0x21,0x20};  // wide size text
            os.write(cc);

            int starting = 2;
            if (isCenter) {
                os.write(Formatter.centerAlign());
            }

            String[] wordlist = text.split(" ");

            int length = 0;

            String line = "";
            int line_count = 1;

            for(String word : wordlist ) {
                length += word.length();

                if (length < 14) {
                    line += word + " ";
                }
                else if(line_count<2){
                    os.write(String.format("%1$"+starting+"s\n", convertCharacters(line)).getBytes("iso-8859-1"));
                    //getBytes("UTF-8" ));
                    length = word.length();
                    line = word + " ";
                    line_count++;
                }

            }
            os.write(String.format("%1$"+starting+"s\n", convertCharacters(line)).getBytes("iso-8859-1"));
            //.getBytes("UTF-8" ));

        } catch (Exception e) {
            Log.e("PrintDao", "Printing ", e);
        }
    }

    public static void printRow(String text, String value) {
        try {
            OutputStream os = Variables.mBluetoothSocket.getOutputStream();
            os.write(String.format("%1$31s %2$11s\n",
                    convertCharacters(text),
                    convertCharacters(value)).getBytes("iso-8859-1"));
        } catch (Exception e) {
            Log.e("PrintDao", "Printing ", e);
        }
    }

    public static void printRow(String column1, String column2, String column3) {
        try {
            OutputStream os = Variables.mBluetoothSocket.getOutputStream();
            os.write(String.format("%1$9s %2$16s %3$17s\n",
                    convertCharacters(column1), convertCharacters(column2),
                    convertCharacters(column3)).getBytes("UTF8"));
        } catch (Exception e) {
            Log.e("PrintDao", "Printing ", e);
        }
    }


    public static void printRow(String column1, String column2, String column3, String column4) {
        try {
            OutputStream os = Variables.mBluetoothSocket.getOutputStream();
            os.write(String.format("%1$10s %2$10s %3$10s %4$11s\n", convertCharacters(column1), convertCharacters(column2), convertCharacters(column3),
                    convertCharacters(column4)).getBytes("UTF8"));
        } catch (Exception e) {
            Log.e("PrintDao", "Printing ", e);
        }
    }


    public static void printRow(String column1, String column2, String column3, String column4, String column5) {
        try {
            OutputStream os = Variables.mBluetoothSocket.getOutputStream();
            os.write(String.format("%1$8s %2$8s %3$8s %4$8s %5$9s\n",
                    convertCharacters(column1), convertCharacters(column2),
                    convertCharacters(column3), convertCharacters(column4),
                    convertCharacters(column5)).getBytes("UTF8"));
        } catch (Exception e) {
            Log.e("PrintDao", "Printing ", e);
        }
    }

    public static void printRow(Integer sira, String column1, String column2, String column3, String column4) {
        try {
            if (column2.length()>22){
                column2 = column2.substring(0,21);
            }
            OutputStream os = Variables.mBluetoothSocket.getOutputStream();
            os.write(String.format("%1$-6s %2$-22s %3$6s %4$8s\n",
                    convertCharacters(column1), convertCharacters(column2),
                    convertCharacters(column3), convertCharacters(column4)).getBytes("UTF8"));
        } catch (Exception e) {
            Log.e("PrintDao", "Printing ", e);
        }
    }

    public static void printTest() {
        Thread t = new Thread() {
            public void run() {
                try {
                    OutputStream os = Variables.mBluetoothSocket.getOutputStream();
                    String BILL = "";
                    BILL = BILL + "TEST PRINT SUCCESSFULL\n";
                    BILL = BILL + "----------------------\n";
                    BILL = BILL + "\n\n\n\n\n";



                     /*BILL = "                   XXXX MART    \n"
                            + "                   XX.AA.BB.CC.     \n " +
                            "                 NO 25 ABC ABCDE    \n" +
                            "                  XXXXX YYYYYY      \n" +
                            "                   MMM 590019091      \n";
                    BILL = BILL
                            + "-----------------------------------------------\n";


                   BILL = BILL + String.format("%1$-10s %2$10s %3$13s %4$10s", "Products", "Qty", "Rate", "Total");
                    BILL = BILL + "\n";
                    BILL = BILL
                            + "-----------------------------------------------";
                    BILL = BILL + "\n " + String.format("%1$-10s %2$10s %3$11s %4$10s", "item-001", "5", "10.00", "50.00");
                    BILL = BILL + "\n " + String.format("%1$-10s %2$10s %3$11s %4$10s", "item-002", "10", "5.00", "50.00");
                    BILL = BILL + "\n " + String.format("%1$-10s %2$10s %3$11s %4$10s", "item-003", "20", "10.00", "200.00");
                    BILL = BILL + "\n " + String.format("%1$-10s %2$10s %3$11s %4$10s", "item-004", "50", "10.00", "500.00");

                    BILL = BILL
                            + "\n-----------------------------------------------";
                    BILL = BILL + "\n\n ";

                    BILL = BILL + "                   Total Qty:" + "      " + "85" + "\n";
                    BILL = BILL + "                   Total Value:" + "     " + "800.00" + "\n";

                    BILL = BILL
                            + "-----------------------------------------------\n";
                    BILL = BILL + "\n\n\n\n\n\n\n\n\n";*/


                    os.write(BILL.getBytes());
                    //This is printer specific code you can comment ==== > Start



                } catch (Exception e) {
                    Log.e("MainActivity", "Exe ", e);
                }
            }
        };
        t.start();
    }

    public static String convertCharacters(String value) {
        String lValue = value;
        if (lValue != null) {
            lValue = lValue.replace("İ", "I");
            lValue = lValue.replace("Ş", "S");
            lValue = lValue.replace("Ğ", "G");
            lValue = lValue.replace("Ü", "U");
            lValue = lValue.replace("Ö", "O");
            lValue = lValue.replace("Ç", "C");
            lValue = lValue.replace("ı", "i");
            lValue = lValue.replace("ş", "s");
            lValue = lValue.replace("ğ", "g");
            lValue = lValue.replace("ü", "u");
            lValue = lValue.replace("ö", "o");
            lValue = lValue.replace("ç", "c");
            lValue = lValue.replace("Ä", "A");
            lValue = lValue.replace("ä", "a");
            lValue = lValue.replace("ß", "B");
        } else
            lValue = "";

        return lValue;
    }

    /**
     * Method to write with a given format
     *
     * @param buffer     the array of bytes to actually write
     * @param pFormat    The format byte array
     * @param pAlignment The alignment byte array
     * @return true on successful write, false otherwise
     */
    public static void writeWithFormat(byte[] buffer, final byte[] pFormat, final byte[] pAlignment) {
        try {
            OutputStream os = Variables.mBluetoothSocket.getOutputStream();
            // Notify printer it should be printed with given alignment:
            os.write(pAlignment);
            // Notify printer it should be printed in the given format:
            os.write(pFormat);
            // Write the actual data:
            os.write(buffer, 0, buffer.length);
        } catch (IOException e) {
            Log.e("PrintDao", "Printing ", e);
        }
    }


}
