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
    private boolean m_isGPSLocationUpdated = false;

    public static GpsTracker getInstance(Context context) {
        if (m_gpsTracker == null)
            m_gpsTracker = new GpsTracker();
        m_gpsTracker.registerLocationUpdates(context);
        m_gpsTracker.setLocation(context);
        return m_gpsTracker;
    }

    private GpsTracker() {
    }

    private void registerLocationUpdates(Context context) {
        if (m_locationManager == null)
            m_locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        boolean isGPSEnabled = m_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = m_locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (isGPSEnabled) {
            if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {
                m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            } else {
                Util.toastMsg(context, "FineLocationPermission is denied.");
            }
        }

        if (isNetworkEnabled) {
            if (hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
                m_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
            } else {
                Util.toastMsg(context, "CoarseLocationPermission is denied.");
            }
        }

    }

    private void setLocation(Context context) {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            m_location = m_locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {
            if (m_isGPSLocationUpdated) {
                m_location = m_locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                m_isGPSLocationUpdated = false;
            }
        }
    }

    public double getLatitude() {
        if (m_location != null)
            return m_location.getLatitude();
        return 0;
    }

    public double getLongitude() {
        if (m_location != null)
            return m_location.getLongitude();
        return 0;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER))
            m_isGPSLocationUpdated = true;

        m_locationManager.removeUpdates(this);
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
