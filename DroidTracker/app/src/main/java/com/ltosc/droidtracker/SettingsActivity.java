package com.ltosc.droidtracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.SwitchPreference;
import android.widget.Toast;

import com.ltosc.droidtracker.config.DeviceInfo;
import com.ltosc.droidtracker.config.DroidTrackerApplication;

/**
 * Created by ltosc on 03/08/2016.
 */
public class SettingsActivity extends PreferenceActivity
{
    private DeviceInfo deviceInfo;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        deviceInfo = new DeviceInfo();
        deviceInfo.loadDeviceInformations();

        setPreferences();
    }

    private void setPreferences()
    {
        findPreference("deviceUuid").setSummary(deviceInfo.getDeviceUuid());
        findPreference("deviceName").setSummary(deviceInfo.getDeviceName());
        findPreference("deviceSystemName").setSummary(deviceInfo.getDeviceSystemName());
        findPreference("devicePassword").setSummary(deviceInfo.getDevicePassword());

        final Preference preferenceStartupServiceOnBoot = findPreference("startupServiceOnBoot");
        preferenceStartupServiceOnBoot.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newVal)
            {
                boolean switched = !(((SwitchPreference) preference).isChecked());

                DroidTrackerApplication droidTrackerApp = new DroidTrackerApplication(getApplicationContext());
                SharedPreferences.Editor prefEditor = droidTrackerApp.appPreferences.edit();
                prefEditor.putBoolean(droidTrackerApp.context.getString(R.string.preference_startup_service_on_boot), switched);
                prefEditor.commit();

                return true;
            }
        });
    }
}
