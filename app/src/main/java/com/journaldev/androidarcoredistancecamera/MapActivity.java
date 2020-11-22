package com.journaldev.androidarcoredistancecamera;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class MapActivity extends AppCompatActivity {
    SupportMapFragment m_mapFragment;
    GoogleMap m_map;
    Button m_btnGoBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map );

        getView();
        initMap();
        setListeners();

    }

    private void getView() {
        m_btnGoBack = findViewById((R.id.btnGoBack));
        m_mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map );
    }

    private void setListeners() {
        m_btnGoBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, PreViewActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void initMap() {
        m_mapFragment.getMapAsync(new OnMapReadyCallback()
        {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                m_map = googleMap;
            }
        });
        try {
            MapsInitializer.initialize (this);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

