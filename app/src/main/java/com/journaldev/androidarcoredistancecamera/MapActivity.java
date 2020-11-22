package com.journaldev.androidarcoredistancecamera;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity {
    private SupportMapFragment m_mapFragment;
    private GoogleMap m_map;
    private Button m_btnGoBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map );

        //TODO : receive current location LatLng(double, double) from previous activity;
        //or get value in this activity.
        //erase parameter of location and remove the comment in onMapReady
        LatLng curLocation = new LatLng(36.92997889253426, 127.0376082778128);

        getView();
        initMap(curLocation);
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

    private void initMap(LatLng curLocation) {

        m_mapFragment.getMapAsync(new OnMapReadyCallback()
        {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                m_map = googleMap;
//                remove comment if current location is calculated by this activity
//                m_map.setMyLocationEnabled(true);
                setLocation(curLocation);
            }
        });
        try {
            MapsInitializer.initialize (this);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setLocation(LatLng curLocation) {
        //set currentLocation
        m_map.animateCamera(CameraUpdateFactory.newLatLngZoom(curLocation, 15));

        //set current Location Marker
        MarkerOptions curLocationMarker = new MarkerOptions();
        curLocationMarker.position(curLocation);
        curLocationMarker.title("my location");
        curLocationMarker.snippet("from GPS info");
        //TODO : need specific image of may current location marker
        //currentLocationMarker.icon(BitmapDescriptorFactory.formResource(R.drawable.xx);
        m_map.addMarker(curLocationMarker);

        //TODO : set local fish info to marker
        //create all the records of fishing by marker
    }
}

