package com.journaldev.androidarcoredistancecamera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import androidx.core.content.ContextCompat;
import java.util.Timer;
import java.util.TimerTask;

public class GpsTracker extends Service {
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    private static final long MIN_TIME_BW_UPDATES = 0;
    private static GpsTracker m_gpsTracker = null;

    private final LocationManager m_locationManager;
    private Location m_location;
    private Location m_gpsLocation = null;
    private Location m_networkLocation = null;

    private final LocationListener m_gpsLocationListner;
    private final LocationListener m_networkLocationListner;

    private Timer m_timer;

    public static GpsTracker getInstance(Context context) {
        if (m_gpsTracker == null)
            m_gpsTracker = new GpsTracker(context);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Util.toastMsg(context, "Location permission is denied");
        } else {
            m_gpsTracker.registerLocationUpdates();
//            m_gpsTracker.setLocation();
        }
        return m_gpsTracker;
    }

    private GpsTracker(Context context) {
        context = context.getApplicationContext();
        m_locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        m_location = new Location("");
        m_location.setLatitude(0.0d);
        m_location.setLongitude(0.0d);
        m_gpsLocationListner = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                m_timer.cancel();
                m_gpsLocation = location;
                m_locationManager.removeUpdates(this);
                m_locationManager.removeUpdates(m_networkLocationListner);
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }
            @Override
            public void onProviderEnabled(String s) {
            }
            @Override
            public void onProviderDisabled(String s) {
            }
        };
        m_networkLocationListner = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                m_networkLocation = location;
                m_locationManager.removeUpdates(this);
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }
            @Override
            public void onProviderEnabled(String s) {
            }
            @Override
            public void onProviderDisabled(String s) {
            }
        };
    }

    @SuppressLint("MissingPermission")
    private void registerLocationUpdates() {
        boolean isGPSEnabled = m_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = m_locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGPSEnabled)
            m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, m_gpsLocationListner);
        if (isNetworkEnabled)
            m_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, m_networkLocationListner);

        m_timer = new Timer();
        m_timer.schedule(new setLocation(), 10000);
    }

    class setLocation extends TimerTask {
        @Override
        public void run() {
            m_locationManager.removeUpdates(m_gpsLocationListner);
            m_locationManager.removeUpdates(m_networkLocationListner);

            if (m_gpsLocation != null) {
                m_location = m_gpsLocation;
            } else if (m_networkLocation != null) {
                m_location = m_networkLocation;
            }

            m_gpsLocation = null;
            m_networkLocation = null;
        }
    }

//    public void setLocation() {
//            m_locationManager.removeUpdates(m_gpsLocationListner);
//            m_locationManager.removeUpdates(m_networkLocationListner);
//
//            if (m_gpsLocation != null) {
//                m_location = m_gpsLocation;
//            } else if (m_networkLocation != null){
//                m_location = m_networkLocation;
//            }
//            m_gpsLocation = null;
//            m_networkLocation = null;
//    }
//
//    @SuppressLint("MissingPermission")
//    private void setLocation() {
//        if (m_isGPSLocationUpdated) {
//            m_location = m_locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            m_isGPSLocationUpdated = false;
//        } else {
//            m_location = m_locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//        }
//    }

    public double getLatitude() {
        return m_location.getLatitude();
    }

    public double getLongitude() {
        return m_location.getLongitude();
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }
}
