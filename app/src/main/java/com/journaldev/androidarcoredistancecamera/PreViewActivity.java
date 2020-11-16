package com.journaldev.androidarcoredistancecamera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Vector;

public class PreViewActivity extends Activity {
    private Button btnTest;
    private Button btnGoBack;
    private Button btnGetSomething;
    private Button btnDelete;
    private Button btnSave;
    private ImageView ivPreviewImage;
    private Spinner   spnFishTypes;

    private DbHandler local_dbhandler;
    private Intent intent;
    private boolean is_saved = false;

    /*TODO refactor*/
    private final int FISH_TYPE_NOT_SELECTED = 4;

    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        getView();
        setListeners();
        Initialize();

        setIvPreviewImage(intent.getByteArrayExtra("image"));
    }

    private void setListeners() {
        btnTest.setOnClickListener(v->{
            Intent intent = new Intent(this, TestActivity.class);
            startActivity(intent);
        });
        btnGoBack.setOnClickListener(v-> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
        btnSave.setOnClickListener(v-> {
            /*TODO use asynctask*/
            if (!is_saved) {
                if (spnFishTypes.getSelectedItemPosition() != FISH_TYPE_NOT_SELECTED) {
                    local_dbhandler.InsertInToFishInfo(0,
                            spnFishTypes.getSelectedItem().toString(),
                            intent.getFloatExtra("fish_size", 0),
                            intent.getByteArrayExtra("image"));
                    is_saved = true;
                } else {
                    toastMsg("Must choose fish type");
                }
            } else {
                   toastMsg("Already Saved");
            }
        });

        btnGetSomething.setOnClickListener(v-> {
            Vector<DbHandler.FishInfo> fish_infos = local_dbhandler.SelectFromFishInfo(null);
            setIvPreviewImage(fish_infos.get(0).image);
        });

        btnDelete.setOnClickListener(v-> {
            local_dbhandler.Delete();
        });

    }

    private void getView() {
        btnTest = findViewById((R.id.btnTest));
        btnGoBack = findViewById((R.id.btnGoBack));
        btnGetSomething = findViewById(R.id.btnGetSometing);
        btnDelete = findViewById(R.id.btnDelete);
        btnSave = findViewById(R.id.btnSave);
        ivPreviewImage = findViewById(R.id.ivPreviewImage);
        spnFishTypes = findViewById(R.id.spnFishTypes);
    }

    private void Initialize() {
        String[] items = getResources().getStringArray(R.array.FishTypes);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
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
        spnFishTypes.setAdapter(arrayAdapter);
        spnFishTypes.setSelection(arrayAdapter.getCount());
        local_dbhandler =  new DbHandler(this);
        intent = getIntent();
    }

    private void setIvPreviewImage (byte[] bytes) {
        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        ivPreviewImage.setImageBitmap(image);
    }

    private void toastMsg(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        toast.show();
    }
}
