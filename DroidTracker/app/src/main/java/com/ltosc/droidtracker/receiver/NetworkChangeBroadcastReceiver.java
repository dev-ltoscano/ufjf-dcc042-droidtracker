package com.ltosc.droidtracker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ltosc.droidtracker.R;
import com.ltosc.droidtracker.config.DroidTrackerApplication;
import com.ltosc.droidtracker.helper.NetworkHelper;
import com.ltosc.droidtracker.helper.ServiceHelper;

public class NetworkChangeBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context ctx, Intent intent)
    {
        DroidTrackerApplication droidTrackerApp = new DroidTrackerApplication(ctx);
        boolean serviceRun = droidTrackerApp.appPreferences.getBoolean(ctx.getString(R.string.preference_service_run), false);

        if(serviceRun)
        {
            if(NetworkHelper.hasInternetConnection(ctx))
            {
                ServiceHelper.startDroidTrackerService(ctx);
            }
            else
            {
                ServiceHelper.stopDroidTrackerService(ctx);
            }
        }
    }
}
