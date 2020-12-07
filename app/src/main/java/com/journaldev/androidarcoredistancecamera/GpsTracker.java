package com.journaldev.androidarcoredistancecamera;

import android.Manifest;
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



public class GpsTracker extends Service implements LocationListener {
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    private static final long MIN_TIME_BW_UPDATES = 1000; //1 second
    private static GpsTracker m_gpsTracker = null;

    private LocationManager m_locationManager = null;
    private Location m_location = null;
    private Context m_context;
    private boolean m_isGPSLocationUpdated = false;

    public static GpsTracker getInstance(Context context) {
        if (m_gpsTracker == null)
            m_gpsTracker = new GpsTracker(context);
        return m_gpsTracker;
    }

    public void registerLocationUpdates() {
        if (m_locationManager == null)
            m_locationManager = (LocationManager) m_context.getSystemService(LOCATION_SERVICE);

        boolean isGPSEnabled = m_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = m_locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(m_context,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(m_context,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (isGPSEnabled) {
            if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {
                m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            } else {
                //request permission
            }
        }

        if (isNetworkEnabled) {
            if (hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
                m_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            } else {
                //request permission
            }
        }

    }

    public Location getLocation() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(m_context,
                    Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(m_context,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            m_location = m_locationManager.getLastKnownLocation(m_locationManager.NETWORK_PROVIDER);
        } else {
            //request permission
        }

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {
            if (m_isGPSLocationUpdated) {
                m_location = m_locationManager.getLastKnownLocation(m_locationManager.GPS_PROVIDER);
                m_isGPSLocationUpdated = false;
            }
        } else {
            //request permission
        }

        return m_location;
    }

    public void removeUpdates() {
        m_locationManager.removeUpdates(this);
    }

    private GpsTracker(Context context) {
        this.m_context = context;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if (location.getProvider().equals(m_locationManager.GPS_PROVIDER))
            m_isGPSLocationUpdated = true;
    }

    @Override
    public void onProviderDisabled(String provider)
    {
    }

    @Override
    public void onProviderEnabled(String provider)
    {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }
}
