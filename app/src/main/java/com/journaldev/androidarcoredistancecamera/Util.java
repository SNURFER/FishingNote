package com.journaldev.androidarcoredistancecamera;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Vector;

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

    public static void createFishTypeList (Spinner spinner, Context context) {
        DbHandler dbHandler = DbHandler.getInstance(context);
        Vector<String> fishTypes = dbHandler.selectFromFishTypes();
        fishTypes.add("물고기 종류를 선택해주세요");
        String[] items = new String[fishTypes.size()];
        fishTypes.toArray(items);
        Util.adaptSpinner(items, spinner, context);
    }

    public static void adaptSpinner (String[] items, Spinner spinner, Context context) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context,
                R.layout.support_simple_spinner_dropdown_item,items) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    ((TextView)v.findViewById(android.R.id.text1)).setText("");
                    ((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount()));
                }
                return v;
            }

            @Override
            public int getCount() {
                return super.getCount() - 1;
            }

        };
        spinner.setAdapter(arrayAdapter);
        spinner.setSelection(arrayAdapter.getCount());
    }
}
