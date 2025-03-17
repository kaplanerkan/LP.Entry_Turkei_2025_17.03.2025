package com.eqpos.eqentry.tools;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.eqpos.eqentry.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

/**
 * Created by dursu on 23.02.2018.
 */

public class SocketProcess {
    public static String errorMessage = "";
    public static Context context = null;
    public static SocketAddress address = null;
    public static Socket client=null;

    public static boolean isConnectWiFiNetwork(Context ctx, boolean msjGoster) {

        ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isWiFi = false;
        boolean isMobile = false;
        isWiFi = wifi.isConnected();
        /*
        * Mobil internet desteği olmayan tabletlerde
        */
        if (mobile!=null) isMobile = mobile.isConnected();


        if ((!isWiFi && !isMobile) && msjGoster) {
            if (msjGoster)
                Toast.makeText(ctx, ctx.getResources().getString(R.string.error_no_network_connection), Toast.LENGTH_SHORT).show();
        }
        return isWiFi;
    }

    public static void createSocket() throws IOException {
        errorMessage = "";
        client = null;

        address = new InetSocketAddress(Variables.hostIp, Variables.hostPort);
        client = new Socket();
        client.connect(address);
    }

    public static boolean connectToServer() {
        try {
            createSocket();
            return client.isConnected() && !client.isClosed();
        } catch (IOException ex) {
            Log.d("Connect Server", ex.getMessage());
            return false;
        }
    }

    public static String sendMessage(String msg)  {
        String rMsg = Variables._RETURNFAULT;
        String lPaket = "";
        int lPos = 0;

        try {
            rMsg= new sendMessageTask().execute(msg).get();
            if (rMsg == null) {
                rMsg = Variables._ERROR;
            }
        } catch (InterruptedException e) {
            return Variables._ERROR;
        } catch (ExecutionException e) {
            return Variables._ERROR;
        }
        return rMsg;
    }


    public static class sendMessageTask extends AsyncTask<String, Integer, String>  {
        Dialog dlgProgress;

        @Override
        protected String doInBackground(String... msg) {
            String rMsg = Variables._RETURNFAULT;
            try {
                if (connectToServer()) {

                            BufferedReader msgReceiver = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
                            PrintWriter msgSender = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), "UTF-8")), true);

                            msgSender.println(Arrays.toString( msg));
                            msgSender.flush();

                            try {
                                rMsg = msgReceiver.readLine();
                            } catch (IOException e) {
                                e.printStackTrace();
                                return Variables._ERROR;
                            }
                            msgSender.close();
                            msgReceiver.close();

                            client.close();
                            client = null;
                        }
            } catch (IOException e) {
                e.printStackTrace();
                return Variables._ERROR;
            }
            return rMsg;
        }

        @Override
        protected void onPreExecute() {
            /*dlgProgress = new Dialog(context);
            dlgProgress.setTitle(context.getString(R.string.msg_sendingdata));
            dlgProgress.setCancelable(false);
            dlgProgress.show();*/
        }

        @Override
        protected void onPostExecute(String s) {
           // dlgProgress.dismiss();
            //burada gelen socket verisi geri gönderilecek
            super.onPostExecute(s);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }
}
