package com.ltosc.droidtracker.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.ltosc.droidtracker.R;
import com.ltosc.droidtracker.security.Md5Hash;

import java.util.UUID;

/**
 * Created by ltosc on 03/08/2016.
 */
public class DeviceInfo
{
    private String deviceUuid;
    private String deviceName;
    private String deviceSystemName;
    private String devicePassword;

    public DeviceInfo() { }

    /**
     * Retorna as informações do dispositivo salvas nas preferências do aplicativo
     */
    public void loadDeviceInformations()
    {
        SharedPreferences pref = DroidTrackerApplication.appPreferences;
        Context ctx = DroidTrackerApplication.context;

        if((pref != null) && (ctx != null))
        {
            setDeviceUuid(pref.getString(ctx.getString(R.string.preference_device_uuid), "Unknown"));
            setDeviceName(pref.getString(ctx.getString(R.string.preference_device_name), "Unknown"));
            setDeviceSystemName(pref.getString(ctx.getString(R.string.preference_system_name), "Unknown"));
            setDevicePassword(pref.getString(ctx.getString(R.string.preference_device_password), "Unknown"));
        }
    }

    /**
     * Guarda informações do dispositivo nas preferências do aplicativo
     *
     * @return (true) se as informações foram salvas, (false) caso contráio
     */
    public boolean saveDeviceInformations()
    {
        // Obtém dados do dispositivo
        setDeviceUuid(UUID.randomUUID().toString());
        setDeviceName(String.format("%s %s", Build.MANUFACTURER, Build.MODEL));
        setDeviceSystemName(Build.VERSION.RELEASE);
        setDevicePassword(Md5Hash.getMd5(getDeviceUuid()));

        // Guarda as informações nas preferências do aplicativo
        SharedPreferences pref = DroidTrackerApplication.appPreferences;
        SharedPreferences.Editor prefEditor = pref.edit();
        Context ctx = DroidTrackerApplication.context;

        prefEditor.putString(ctx.getString(R.string.preference_device_uuid), getDeviceUuid());
        prefEditor.putString(ctx.getString(R.string.preference_device_name), getDeviceName());
        prefEditor.putString(ctx.getString(R.string.preference_system_name), getDeviceSystemName());
        prefEditor.putString(ctx.getString(R.string.preference_device_password), getDevicePassword());

        return prefEditor.commit();
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceSystemName() {
        return deviceSystemName;
    }

    public void setDeviceSystemName(String deviceSystemName) {
        this.deviceSystemName = deviceSystemName;
    }

    public String getDevicePassword() {
        return devicePassword;
    }

    public void setDevicePassword(String devicePassword) {
        this.devicePassword = devicePassword;
    }
}
