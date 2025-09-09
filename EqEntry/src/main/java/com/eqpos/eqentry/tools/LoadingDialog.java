package com.eqpos.eqentry.tools;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;


import com.eqpos.eqentry.R;
import com.eqpos.eqentry.db.SettingsDao;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;


public class LoadingDialog {


    private Dialog dialog;
    private TextView timerText;

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    public void dismiss() {
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }catch (Exception ex) {
            Log.e("LoadingDialog", "dismiss deyiz: ", ex);
        }
    }


    public void showLoading(Context context, String message) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        try {

            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            dialog.setContentView(R.layout.loading_dialog);

            TextView txtMessage = dialog.findViewById(R.id.txtText);
            dialog.setCancelable(false);

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            if (message != null) {
                txtMessage.setText(message);
            } else {
                txtMessage.setText("");
            }

            if (dialog != null && !dialog.isShowing()) {
                dialog.show();
            }

        }catch (Exception ex) {
            Log.e("LoadingDialog", "showLoading deyiz: ", ex);

        }
    }

    public void checkServerStatus(ServerStatusCallback callback) {

        int port = SettingsDao.getIntValue("ServerPort");
        String host = SettingsDao.getStrValue("ServerIP");


        new Thread(() -> {
            boolean isOnline = false;
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 5000); // 5 saniye zaman aşımı
                isOnline = true;
            } catch (IOException e) {
                isOnline = false;
            }
            boolean finalIsOnline = isOnline;
            new Handler(Looper.getMainLooper()).post(() -> callback.onServerStatusChecked(finalIsOnline));
        }).start();
    }

    public interface ServerStatusCallback {
        void onServerStatusChecked(boolean isOnline);
    }


}
