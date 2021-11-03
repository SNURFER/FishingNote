package com.journaldev.androidarcoredistancecamera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Vector;

public class MapActivity extends AppCompatActivity {
    private SupportMapFragment m_mapFragment;
    private GoogleMap m_map;
    private Button m_btnGoBack;
    private DbHandler m_localDbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map );

        //TODO : receive current location LatLng(double, double) from previous activity;
        //or get value in this activity.
        //erase parameter of location and remove the comment in onMapReady
        LatLng curLocation = new LatLng(36.92997889253426, 127.0376082778128);

        getView();
        setListeners();
        initialize();
        initMap(curLocation);
    }

    private void getView() {
        m_btnGoBack = findViewById((R.id.btnGoBack));
        m_mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map );
    }

    private void setListeners() {
        m_btnGoBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, PreViewActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
                setMyLocation(curLocation);

                //create all the records of fishing by marker
                setFishInfoMarker();
            }
        });
        try {
            MapsInitializer.initialize (this);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setMyLocation(LatLng curLocation) {
        //set zoom
        m_map.animateCamera(CameraUpdateFactory.newLatLngZoom(curLocation, 9));

        MarkerOptions myMarker = new MarkerOptions();
        myMarker.position(curLocation);
        myMarker.title("my location");
        myMarker.snippet("from GPS info");
        int height = 60;
        int width = 60;
        Bitmap marker = BitmapFactory.decodeResource(getResources(), R.drawable.marker);
        Bitmap resizeMarker = Bitmap.createScaledBitmap(marker, width, height, false);
        BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromBitmap(resizeMarker);
        myMarker.icon(markerIcon);
        m_map.addMarker(myMarker);
    }

    private void initialize() {
        m_localDbHandler =  DbHandler.getInstance(this);
    }

    //TODO : this function should be flexible to create marker by fish type
    private void setFishInfoMarker() {
        Vector<DbHandler.FishInfo> fishInfos = m_localDbHandler.selectFromFishInfo(null);
        if (!fishInfos.isEmpty()) {
            for (DbHandler.FishInfo fishInfo : fishInfos) {
                MarkerOptions newMarker = new MarkerOptions();
                LatLng curLatLng = new LatLng(GpsTracker.getInstance(this).getLatitude(),
                        GpsTracker.getInstance(this).getLongitude());
                newMarker.position(curLatLng);
                newMarker.title(fishInfo.name);
                newMarker.snippet("The length of the fish caught is : " + fishInfo.size);
                newMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                m_map.addMarker(newMarker);
            }
        }
    }

    //will be removed
    private LatLng genPositionTemp() {
        double altitude, longitude;
        longitude = 35 + (38 - 35) * Math.random();
        altitude = 127 + (129 - 127) * Math.random();
        LatLng ret = new LatLng(longitude, altitude);

        return ret;
    }
}

