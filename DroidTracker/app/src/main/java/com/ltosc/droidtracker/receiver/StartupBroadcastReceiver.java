package com.ltosc.droidtracker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.ltosc.droidtracker.R;
import com.ltosc.droidtracker.config.DroidTrackerApplication;
import com.ltosc.droidtracker.helper.ServiceHelper;
import com.ltosc.droidtracker.service.DroidTrackerService;

public class StartupBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context ctx, Intent intent)
    {
        DroidTrackerApplication droidTrackerApp = new DroidTrackerApplication(ctx);
        boolean startup = droidTrackerApp.appPreferences.getBoolean(ctx.getString(R.string.preference_startup_service_on_boot), false);

        if(startup)
        {
            ServiceHelper.startDroidTrackerService(ctx);

            SharedPreferences.Editor prefEditor = droidTrackerApp.appPreferences.edit();
            prefEditor.putBoolean(ctx.getString(R.string.preference_service_run), true);
            prefEditor.commit();
        }
    }
}
