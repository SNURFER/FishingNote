package com.journaldev.androidarcoredistancecamera;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PreViewActivity extends Activity {
    private Button m_btnGoBack;
    private Button m_btnGoToView;
    private Button m_btnDelete;
    private Button m_btnSave;
    private Button m_btnGoToMap;
    private ImageView m_ivPreviewImage;
    private Spinner   m_spnFishTypes;

    private DbHandler m_localDbHandler;
    private Intent m_intent;
    private boolean m_isSaved = false;
    private ProgressDialog m_progressDialog;

    /*TODO refactor*/
    private final int FISH_TYPE_NOT_SELECTED = 4;

    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        getView();
        setListeners();
        initialize();

        Util.setImageView(m_intent.getByteArrayExtra("image"), m_ivPreviewImage);
    }

    private void setListeners() {
        m_btnGoBack.setOnClickListener(v-> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
        m_btnSave.setOnClickListener(v-> {
            if (!m_isSaved) {
                if (m_spnFishTypes.getSelectedItemPosition() != FISH_TYPE_NOT_SELECTED) {
                    final Executor executor = Executors.newSingleThreadExecutor();
                    final Handler handler = new Handler(Looper.getMainLooper());
                    Util.showDialog(m_progressDialog, "Saving Data");
                    executor.execute(() -> {
                        m_localDbHandler.insertInToFishInfo(
                                m_spnFishTypes.getSelectedItem().toString(),
                                m_intent.getFloatExtra("fish_size", 0),
                                m_intent.getByteArrayExtra("image"),
                                "", 0, 0);
                        m_isSaved = true;
                        handler.post(() -> {
                            m_progressDialog.dismiss();
                            Util.toastMsg(this, "Saved data successfully");
                        });
                    });
                } else {
                    Util.toastMsg(this, "Must choose fish type");
                }
            } else {
                   Util.toastMsg(this, "Already Saved");
            }
        });

        m_btnGoToView.setOnClickListener(v-> {
            Intent intent = new Intent(this, ViewActivity.class);
            startActivity(intent);
        });

        m_btnDelete.setOnClickListener(v-> m_localDbHandler.deleteAll());

        m_btnGoToMap.setOnClickListener(v->{
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);
        });

    }

    private void getView() {
        m_btnGoBack = findViewById((R.id.btnGoBack));
        m_btnGoToView = findViewById(R.id.btnGoToView);
        m_btnDelete = findViewById(R.id.btnDelete);
        m_btnSave = findViewById(R.id.btnSave);
        m_ivPreviewImage = findViewById(R.id.ivPreviewImage);
        m_spnFishTypes = findViewById(R.id.spnFishTypes);
        m_btnGoToMap = findViewById(R.id.btnGoToMap);
    }

    private void initialize() {
        String[] items = getResources().getStringArray(R.array.FishTypes);
        Util.adaptSpinner(items, m_spnFishTypes, this);
        m_localDbHandler =  new DbHandler(this);
        m_intent = getIntent();
        m_progressDialog = new ProgressDialog(this);
    }

}
