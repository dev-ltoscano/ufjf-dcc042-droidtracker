package com.ltosc.droidtracker.location;

import android.content.Context;
import android.location.LocationManager;

/**
 * Created by ltosc on 03/08/2016.
 */
public class DroidTrackerLocationManager
{
    private LocationManager locationManager;

    private DroidTrackerLocationListener[] locationListeners = new DroidTrackerLocationListener[]
    {
            new DroidTrackerLocationListener(LocationManager.GPS_PROVIDER),
            new DroidTrackerLocationListener(LocationManager.NETWORK_PROVIDER)
    };

    public DroidTrackerLocationManager(Context ctx)
    {
        locationManager = (LocationManager)ctx.getSystemService(Context.LOCATION_SERVICE);
    }

    public void addLocationListener(ILocationChangedListener listener)
    {
        locationListeners[1].addLocationChangedListener(listener);
    }

    public void startUpdates()
    {
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000,
                0,
                locationListeners[1]);
    }

    public void stopUpdates()
    {
        locationManager.removeUpdates(locationListeners[1]);
    }
}
