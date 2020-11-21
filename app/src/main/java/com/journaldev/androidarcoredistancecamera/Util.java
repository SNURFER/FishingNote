package com.journaldev.androidarcoredistancecamera;

import android.content.Context;
import android.widget.Toast;

public final class Util {

    public static void toastMsg(Context activity, String msg) {
        Toast toast = Toast.makeText(activity, msg, Toast.LENGTH_LONG);
        toast.show();
    }
    
}
