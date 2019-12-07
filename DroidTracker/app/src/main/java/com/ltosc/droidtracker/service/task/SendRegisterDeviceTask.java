package com.ltosc.droidtracker.service.task;

import android.util.Log;

import com.ltosc.droidtracker.protocol.DroidTrackerProtocol;
import com.ltosc.droidtracker.protocol.exception.LoginServiceException;

import java.io.IOException;

/**
 * Created by ltosc on 03/08/2016.
 */
public class SendRegisterDeviceTask implements Runnable
{
    private DroidTrackerProtocol protocol;

    public SendRegisterDeviceTask(DroidTrackerProtocol protocol)
    {
        this.protocol = protocol;
    }

    @Override
    public void run()
    {
        try
        {
            protocol.registerDeviceInService();
            Log.i("DROIDTRACKER_LOG", "registerDeviceInService() executed!");
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
