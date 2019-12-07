package com.ltosc.droidtracker.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ltosc on 03/08/2016.
 */
public class DroidTrackerLocationListener implements LocationListener
{
    private Location lastLocation;
    private List<ILocationChangedListener> locationChangedListeners;

    public DroidTrackerLocationListener(String provider)
    {
        locationChangedListeners = new ArrayList<>();
        lastLocation = new Location(provider);
    }

    public void addLocationChangedListener(ILocationChangedListener listener)
    {
        locationChangedListeners.add(listener);
    }

    @Override
    public void onLocationChanged(Location location)
    {
        lastLocation.set(location);
        Log.e(DroidTrackerLocationListener.class.getName(), "onLocationChanged: " + location);

        for(ILocationChangedListener listener : locationChangedListeners)
        {
            listener.onLocationChanged(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }

    @Override
    public void onProviderEnabled(String provider)
    {

    }

    @Override
    public void onProviderDisabled(String provider)
    {

    }
}
