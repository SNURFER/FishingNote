package com.journaldev.androidarcoredistancecamera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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
    private Button m_btnTest;
    private Button m_btnGoBack;
    private Button m_btnGetSomething;
    private Button m_btnDelete;
    private Button m_btnSave;
    private ImageView m_ivPreviewImage;
    private Spinner   m_spnFishTypes;

    private DbHandler m_localDbHandler;
    private Intent m_intent;
    private boolean m_isSaved = false;

    /*TODO refactor*/
    private final int FISH_TYPE_NOT_SELECTED = 4;

    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        getView();
        setListeners();
        initialize();

        setPreviewImage(m_intent.getByteArrayExtra("image"));
    }

    private void setListeners() {
        m_btnTest.setOnClickListener(v->{
            Intent intent = new Intent(this, TestActivity.class);
            startActivity(intent);
        });
        m_btnGoBack.setOnClickListener(v-> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
        m_btnSave.setOnClickListener(v-> {
            /*TODO use asynctask*/
            if (!m_isSaved) {
                if (m_spnFishTypes.getSelectedItemPosition() != FISH_TYPE_NOT_SELECTED) {
                    m_localDbHandler.InsertInToFishInfo(0,
                            m_spnFishTypes.getSelectedItem().toString(),
                            m_intent.getFloatExtra("fish_size", 0),
                            m_intent.getByteArrayExtra("image"));
                    m_isSaved = true;
                } else {
                    Util.toastMsg(this, "Must choose fish type");
                }
            } else {
                   Util.toastMsg(this, "Already Saved");
            }
        });

        m_btnGetSomething.setOnClickListener(v-> {
            Vector<DbHandler.FishInfo> fishInfos = m_localDbHandler.SelectFromFishInfo(null);
            setPreviewImage(fishInfos.get(0).image);
        });

        m_btnDelete.setOnClickListener(v-> {
            m_localDbHandler.Delete();
        });

    }

    private void getView() {
        m_btnTest = findViewById((R.id.btnTest));
        m_btnGoBack = findViewById((R.id.btnGoBack));
        m_btnGetSomething = findViewById(R.id.btnGetSometing);
        m_btnDelete = findViewById(R.id.btnDelete);
        m_btnSave = findViewById(R.id.btnSave);
        m_ivPreviewImage = findViewById(R.id.ivPreviewImage);
        m_spnFishTypes = findViewById(R.id.spnFishTypes);
    }

    private void initialize() {
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
        m_spnFishTypes.setAdapter(arrayAdapter);
        m_spnFishTypes.setSelection(arrayAdapter.getCount());
        m_localDbHandler =  new DbHandler(this);
        m_intent = getIntent();
    }

    private void setPreviewImage (byte[] bytes) {
        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        m_ivPreviewImage.setImageBitmap(image);
    }
}
