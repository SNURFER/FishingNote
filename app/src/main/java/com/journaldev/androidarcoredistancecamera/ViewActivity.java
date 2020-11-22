package com.journaldev.androidarcoredistancecamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import java.util.Vector;

public class ViewActivity extends Activity {

    private Button m_btnGoBackToPreView;
    private Button m_btnSetView;
    private Spinner m_spnFishTypesCondition;
    private ImageView m_ivSearchedImage;

    private DbHandler m_localDbHandler;

    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view);

        getView();
        setListeners();
        initialize();
    }

    private void getView() {
        m_btnGoBackToPreView = findViewById(R.id.btnGoBackToPreview);
        m_btnSetView = findViewById(R.id.btnSetView);
        m_spnFishTypesCondition = findViewById(R.id.spnFishTypesCondition);
        m_ivSearchedImage = findViewById(R.id.ivSearchedImage);
    }

    private void setListeners() {
        m_btnGoBackToPreView.setOnClickListener(v->{
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        m_btnSetView.setOnClickListener(v->{
            Vector<DbHandler.FishInfo> fishInfos = m_localDbHandler.selectFromFishInfo(null);
            if (fishInfos.isEmpty()) {
                Util.toastMsg(this, "No Fish Data");
            } else {
                Util.setImageView(fishInfos.get(0).image, m_ivSearchedImage);
            }
        });
    }

    private void initialize() {
        String[] items = getResources().getStringArray(R.array.FishTypes);
        Util.adaptSpinner(items, m_spnFishTypesCondition, this);
        m_localDbHandler = new DbHandler(this);
    }
}
