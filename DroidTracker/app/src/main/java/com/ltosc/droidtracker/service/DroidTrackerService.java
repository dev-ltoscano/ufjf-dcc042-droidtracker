package com.ltosc.droidtracker.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.ltosc.droidtracker.MainActivity;
import com.ltosc.droidtracker.R;
import com.ltosc.droidtracker.config.DeviceInfo;
import com.ltosc.droidtracker.config.DroidTrackerApplication;
import com.ltosc.droidtracker.location.DroidTrackerLocationManager;
import com.ltosc.droidtracker.location.ILocationChangedListener;
import com.ltosc.droidtracker.protocol.DroidTrackerProtocol;
import com.ltosc.droidtracker.protocol.IStartTrackerEventReceivedListener;
import com.ltosc.droidtracker.protocol.IStopTrackerEventReceivedListener;
import com.ltosc.droidtracker.service.task.CommandReceivedTask;
import com.ltosc.droidtracker.service.task.SendRegisterDeviceTask;
import com.ltosc.droidtracker.service.task.SendDevicePositionTask;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DroidTrackerService extends Service implements
        IStartTrackerEventReceivedListener,
        IStopTrackerEventReceivedListener,
        ILocationChangedListener
{
    private DeviceInfo deviceInfo;
    private DroidTrackerProtocol protocol;

    private DroidTrackerLocationManager locationManager;
    private NotificationManager notificationManager;

    private ScheduledExecutorService executorService;

    private SendRegisterDeviceTask sendRegisterDeviceTask;
    private CommandReceivedTask commandReceivedTask;
    private SendDevicePositionTask sendDevicePositionTask;
    private ScheduledFuture<?> sendDevicePositionTaskFuture;

    private Handler notificationHandler;

    @Override
    public void onCreate()
    {
        super.onCreate();

        DroidTrackerApplication droidTrackerApp = new DroidTrackerApplication(getApplicationContext());

        deviceInfo = new DeviceInfo();
        deviceInfo.loadDeviceInformations();

        protocol = new DroidTrackerProtocol(deviceInfo);

        locationManager = new DroidTrackerLocationManager(getApplicationContext());
        locationManager.addLocationListener(this);
        locationManager.startUpdates();

        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        notificationHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                Context ctx = getApplicationContext();
                DroidTrackerApplication droidTrackerApp = new DroidTrackerApplication(ctx);
                SharedPreferences.Editor prefEditor = droidTrackerApp.appPreferences.edit();

                if(msg.what == 1)
                {
                    prefEditor.putBoolean(ctx.getString(R.string.preference_streaming_run), true);
                    prefEditor.commit();
                }
                else
                {
                    prefEditor.putBoolean(ctx.getString(R.string.preference_streaming_run), false);
                    prefEditor.commit();
                }

                String contentInfo = (String) msg.obj;

                Intent activityIntent = new Intent(DroidTrackerService.this, MainActivity.class);

                PendingIntent pendingIntent = PendingIntent.getActivity(
                        getApplicationContext(),
                        0,
                        activityIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                Notification notification = new Notification.Builder(DroidTrackerService.this)
                        .setContentTitle(getText(R.string.app_name))
                        .setContentInfo(contentInfo)
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .build();

                notificationManager.notify(1, notification);
            }
        };

        executorService = Executors.newScheduledThreadPool(3);

        sendRegisterDeviceTask = new SendRegisterDeviceTask(protocol);
        executorService.scheduleAtFixedRate(sendRegisterDeviceTask, 0, 30, TimeUnit.SECONDS);

        boolean enableStreaming = droidTrackerApp.appPreferences.getBoolean(
                getApplicationContext().getString(R.string.preference_enable_streaming), false);

        sendDevicePositionTask = new SendDevicePositionTask(protocol);

        if(enableStreaming)
        {
            commandReceivedTask = new CommandReceivedTask(protocol);
            commandReceivedTask.addStartTrackerListener(this);
            commandReceivedTask.addStopTrackerListener(this);

            executorService.scheduleAtFixedRate(commandReceivedTask, 0, 60, TimeUnit.SECONDS);

            boolean streamingOn = droidTrackerApp.appPreferences.getBoolean(
                    getApplicationContext().getString(R.string.preference_streaming_run), false);

            if(streamingOn)
            {
                sendDevicePositionTaskFuture = executorService.scheduleAtFixedRate(sendDevicePositionTask, 0, 15, TimeUnit.SECONDS);

                Message msg = new Message();
                msg.what = 1;
                msg.obj = "Modo streaming ativado";

                notificationHandler.sendMessage(msg);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Intent activityIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setContentTitle(getText(R.string.app_name))
                .setContentInfo("O serviço está em execução...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        stopForeground(true);
        executorService.shutdownNow();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onStartTrackerReceived()
    {
        if(sendDevicePositionTaskFuture == null)
        {
            sendDevicePositionTaskFuture = executorService.scheduleAtFixedRate(sendDevicePositionTask, 0, 15, TimeUnit.SECONDS);

            Message msg = new Message();
            msg.what = 1;
            msg.obj = "Modo streaming ativado";

            notificationHandler.sendMessage(msg);
        }

        Log.i("DROIDTRACKER_LOG", "onStartTrackerReceived()!");
    }

    @Override
    public void onStopTrackerReceived()
    {
        if(sendDevicePositionTaskFuture != null)
        {
            sendDevicePositionTaskFuture.cancel(false);
            sendDevicePositionTaskFuture = null;

            Message msg = new Message();
            msg.what = 0;
            msg.obj = "Modo streaming desativado";

            notificationHandler.sendMessage(msg);
        }

        Log.i("DROIDTRACKER_LOG", "onStopTrackerReceived()!");
    }

    @Override
    public void onLocationChanged(Location location)
    {
        sendDevicePositionTask.setDeviceLocation(location);
        Log.i("DROIDTRACKER_LOG", "onLocationChanged()!");
    }
}