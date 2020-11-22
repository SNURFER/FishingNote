package com.journaldev.androidarcoredistancecamera;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
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

    public static void setImageView (byte[] bytes, ImageView imageView) {
        if (bytes == null) {
            return;
        }

        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        imageView.setImageBitmap(image);
    }
}
