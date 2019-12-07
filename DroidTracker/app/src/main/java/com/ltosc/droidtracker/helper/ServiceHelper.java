package com.ltosc.droidtracker.helper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.ltosc.droidtracker.R;
import com.ltosc.droidtracker.config.DroidTrackerApplication;
import com.ltosc.droidtracker.service.DroidTrackerService;

/**
 * Created by ltosc on 05/08/2016.
 */
public class ServiceHelper
{
    public static boolean droidTrackerServiceIsRunning(Context ctx, Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }

        return false;
    }

    public static void startDroidTrackerService(Context ctx)
    {
        if(!ServiceHelper.droidTrackerServiceIsRunning(ctx, DroidTrackerService.class))
        {
            ctx.startService(new Intent(ctx, DroidTrackerService.class));
            Toast.makeText(ctx, "DroidTracker - Serviço iniciado!", Toast.LENGTH_SHORT).show();
        }
    }

    public static void stopDroidTrackerService(Context ctx)
    {
        if(ServiceHelper.droidTrackerServiceIsRunning(ctx, DroidTrackerService.class))
        {
            ctx.stopService(new Intent(ctx, DroidTrackerService.class));
            Toast.makeText(ctx, "DroidTracker - Serviço interrompido!", Toast.LENGTH_SHORT).show();
        }
    }
}
