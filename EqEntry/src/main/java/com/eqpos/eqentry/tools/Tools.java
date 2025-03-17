package com.eqpos.eqentry.tools;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by dursu on 22.05.2019.
 */

public class Tools {
    public static void showSoftKeyboard(final Context context, final EditText editText) {
        try {
            editText.requestFocus();
            editText.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            keyboard.showSoftInput(editText, 0);
                        }
                    }
                    , 200);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
