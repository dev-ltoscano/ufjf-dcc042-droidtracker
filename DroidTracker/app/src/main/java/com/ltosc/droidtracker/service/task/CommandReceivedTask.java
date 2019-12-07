package com.ltosc.droidtracker.service.task;

import android.util.Log;

import com.ltosc.droidtracker.config.DeviceInfo;
import com.ltosc.droidtracker.protocol.DroidTrackerProtocol;
import com.ltosc.droidtracker.protocol.IStartTrackerEventReceivedListener;
import com.ltosc.droidtracker.protocol.IStopTrackerEventReceivedListener;
import com.ltosc.droidtracker.protocol.exception.LoginServiceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by ltosc on 03/08/2016.
 */
public class CommandReceivedTask implements Runnable
{
    private DroidTrackerProtocol protocol;

    private List<IStartTrackerEventReceivedListener> startTrackerListeners;
    private List<IStopTrackerEventReceivedListener> stopTrackerListeners;

    public CommandReceivedTask(DroidTrackerProtocol protocol)
    {
        this.protocol = protocol;

        startTrackerListeners = new ArrayList<>();
        stopTrackerListeners = new ArrayList<>();
    }

    public void addStartTrackerListener(IStartTrackerEventReceivedListener listener)
    {
        startTrackerListeners.add(listener);
    }

    public void addStopTrackerListener(IStopTrackerEventReceivedListener listener)
    {
        stopTrackerListeners.add(listener);
    }

    @Override
    public void run()
    {
        try
        {
            protocol.waitRemoteComand(startTrackerListeners, stopTrackerListeners);
            Log.i("DROIDTRACKER_LOG", "waitRemoteComand() executed!");
        }
        catch (LoginServiceException ex)
        {
            ex.printStackTrace();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
}
