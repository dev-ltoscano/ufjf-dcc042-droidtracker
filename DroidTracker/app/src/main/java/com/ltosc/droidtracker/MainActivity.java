package com.ltosc.droidtracker;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ltosc.droidtracker.config.DeviceInfo;
import com.ltosc.droidtracker.config.DroidTrackerApplication;
import com.ltosc.droidtracker.helper.ServiceHelper;
import com.ltosc.droidtracker.location.DroidTrackerLocationManager;
import com.ltosc.droidtracker.location.ILocationChangedListener;
import com.ltosc.droidtracker.protocol.DroidTrackerProtocol;
import com.ltosc.droidtracker.protocol.exception.LoginServiceException;
import com.ltosc.droidtracker.service.DroidTrackerService;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements ILocationChangedListener
{
    private DroidTrackerApplication droidTrackerApp;
    private DroidTrackerProtocol protocol;
    private DeviceInfo deviceInfo;

    private TextView txtViewServiceStatus;

    private DroidTrackerLocationManager locationManager;
    private TextView txtViewLatitude;
    private TextView txtViewLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final DroidTrackerApplication droidTrackerApp = new DroidTrackerApplication(getApplicationContext());

        txtViewServiceStatus = (TextView) findViewById(R.id.txtViewServiceStatus);

        if(ServiceHelper.droidTrackerServiceIsRunning(this, DroidTrackerService.class))
        {
            txtViewServiceStatus.setText("Rodando");
        }

        Switch switchEnableStreaming = (Switch)findViewById(R.id.switchEnableStreaming);


        boolean enableStreaming = droidTrackerApp.appPreferences.getBoolean(
                droidTrackerApp.context.getString(R.string.preference_enable_streaming), false);

        switchEnableStreaming.setChecked(enableStreaming);

        switchEnableStreaming.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                ServiceHelper.stopDroidTrackerService(MainActivity.this);

                SharedPreferences.Editor prefEditor = droidTrackerApp.appPreferences.edit();
                prefEditor.putBoolean(droidTrackerApp.context.getString(R.string.preference_enable_streaming), isChecked);
                prefEditor.commit();
            }
        });

        locationManager = new DroidTrackerLocationManager(getApplicationContext());
        locationManager.addLocationListener(this);
        locationManager.startUpdates();

        txtViewLatitude = (TextView) findViewById(R.id.txtViewLatitude);
        txtViewLongitude = (TextView) findViewById(R.id.txtViewLongitude);

        final Button btnStartService = (Button) findViewById(R.id.btnStartService);

        btnStartService.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                ServiceHelper.startDroidTrackerService(MainActivity.this);

                if(ServiceHelper.droidTrackerServiceIsRunning(MainActivity.this, DroidTrackerService.class))
                {
                    txtViewServiceStatus.setText("Rodando");

                    SharedPreferences.Editor prefEditor = droidTrackerApp.appPreferences.edit();
                    prefEditor.putBoolean(MainActivity.this.getString(R.string.preference_service_run), true);
                    prefEditor.commit();
                }
            }
        });

        final Button btnStopService = (Button) findViewById(R.id.btnStopService);

        btnStopService.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                ServiceHelper.stopDroidTrackerService(MainActivity.this);

                if(!ServiceHelper.droidTrackerServiceIsRunning(MainActivity.this, DroidTrackerService.class))
                {
                    txtViewServiceStatus.setText("Parado");

                    SharedPreferences.Editor prefEditor = droidTrackerApp.appPreferences.edit();
                    prefEditor.putBoolean(MainActivity.this.getString(R.string.preference_service_run), false);
                    prefEditor.commit();
                }
            }
        });

        // Verifica se é a primeira vez que o aplicativo é aberto
        if(droidTrackerApp.checkFirstTimeRun())
        {
            deviceInfo = new DeviceInfo();

            // Salva as informações do dispositivo nas preferências e verifica se houve sucesso
            if(deviceInfo.saveDeviceInformations())
            {
                new AsyncTask()
                {
                    @Override
                    protected Object doInBackground(Object[] params)
                    {
                        try
                        {
                            protocol = new DroidTrackerProtocol(deviceInfo);

                            // Registra o dispositivo no serviço do DroidTracker
                            protocol.newLoginInService();
                        }
                        catch (LoginServiceException ex)
                        {
                            ex.printStackTrace();
                        }
                        catch(IOException ex)
                        {
                            ex.printStackTrace();
                        }

                        return null;
                    }
                }.execute();

                // Marca que o aplicativo já rodou a primeira vez
                droidTrackerApp.setFirstTimeRun(false);

                // Exibe mensagem avisando que o dispositivo foi registrado com sucesso
                Toast.makeText(this, R.string.toast_msg_first_run_register, Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, R.string.toast_error_not_saved_preferences, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if(ServiceHelper.droidTrackerServiceIsRunning(this, DroidTrackerService.class))
        {
            txtViewServiceStatus.setText("Rodando");
        }
        else
        {
            txtViewServiceStatus.setText("Parado");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
        }

        return true;
    }

    private void startDroidTrackerService()
    {
        if(!ServiceHelper.droidTrackerServiceIsRunning(this, DroidTrackerService.class))
        {
            startService(new Intent(this, DroidTrackerService.class));

            Toast.makeText(this, "Serviço iniciado!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "O serviço já está em execução!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        txtViewLatitude.setText(String.valueOf(location.getLatitude()));
        txtViewLongitude.setText(String.valueOf(location.getLongitude()));
    }
}