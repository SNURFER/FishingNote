package com.journaldev.androidarcoredistancecamera;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.ProgressBar;
import android.widget.Toast;

public final class Util {

    public static void toastMsg(Context activity, String msg) {
        Toast toast = Toast.makeText(activity, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    public static void showDialog(ProgressDialog dialog, String msg) {
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(msg);
        dialog.show();
    }
}
