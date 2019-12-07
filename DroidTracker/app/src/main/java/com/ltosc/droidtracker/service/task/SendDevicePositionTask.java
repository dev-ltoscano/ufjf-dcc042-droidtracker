package com.ltosc.droidtracker.service.task;

import android.location.Location;
import android.util.Log;

import com.ltosc.droidtracker.protocol.DroidTrackerProtocol;

import java.io.IOException;

/**
 * Created by ltosc on 03/08/2016.
 */
public class SendDevicePositionTask implements Runnable
{
    private DroidTrackerProtocol protocol;
    private Location deviceLocation;

    public SendDevicePositionTask(DroidTrackerProtocol protocol)
    {
        this.protocol = protocol;
    }

    public void setDeviceLocation(Location location)
    {
        deviceLocation = location;
    }

    @Override
    public void run()
    {
        try
        {
            protocol.sendDevicePosition(deviceLocation);
            Log.i("DROIDTRACKER_LOG", "sendDevicePosition() executed!");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
