package com.ltosc.droidtracker.service.task;

import android.util.Log;

import com.ltosc.droidtracker.protocol.DroidTrackerProtocol;
import com.ltosc.droidtracker.protocol.exception.LoginServiceException;

import java.io.IOException;

/**
 * Created by ltosc on 04/08/2016.
 */
public class SendEchoTask implements Runnable
{
    private DroidTrackerProtocol protocol;

    public SendEchoTask(DroidTrackerProtocol protocol)
    {
        this.protocol = protocol;
    }

    @Override
    public void run()
    {
        try
        {
            protocol.sendEchoToService();
            Log.i("DROIDTRACKER_LOG", "sendEchoToService() executed!");
        }
        catch(LoginServiceException ex)
        {
            ex.printStackTrace();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
}
