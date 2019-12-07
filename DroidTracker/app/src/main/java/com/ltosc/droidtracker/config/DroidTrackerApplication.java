package com.ltosc.droidtracker.config;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.ltosc.droidtracker.R;

/**
 * Created by ltosc on 03/08/2016.
 */
public class DroidTrackerApplication
{
    public static Context context;
    public static SharedPreferences appPreferences;

    public DroidTrackerApplication(Context ctx)
    {
        context = ctx;
        appPreferences = context.getSharedPreferences(context.getString(R.string.preferences_file_key), Context.MODE_PRIVATE);
    }

    /**
     * Verifica se é a primeira vez que o aplicativo está sendo executado
     *
     * @return (true) caso seja a primeira vez, (false) caso contrário
     */
    public boolean checkFirstTimeRun()
    {
        return DroidTrackerApplication.appPreferences.getBoolean(DroidTrackerApplication.context.getString(R.string.preference_first_time_run), true);
    }

    public void setFirstTimeRun(boolean isFirstTime)
    {
        SharedPreferences.Editor prefEditor = DroidTrackerApplication.appPreferences.edit();
        prefEditor.putBoolean(DroidTrackerApplication.context.getString(R.string.preference_first_time_run), isFirstTime);
        prefEditor.commit();
    }
}
