package com.ltosc.droidtracker.protocol;

import android.location.Location;
import android.util.Log;

import com.ltosc.droidtracker.config.DeviceInfo;
import com.ltosc.droidtracker.helper.NetworkHelper;
import com.ltosc.droidtracker.protocol.exception.LoginServiceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ltosc on 03/08/2016.
 */
public class DroidTrackerProtocol
{
    private final String SERVICE_IP_ADDRESS = "191.96.43.104";
    private final int TCP_SERVICE_PORT = 8887;
    private final int UDP_SERVICE_PORT = 8888;

    public final String UTF8_CHARSET = "UTF-8";

    public final String END_COMMAND = "\n";
    public final String END_RESPONSE = ";;";

    public final String CMD_OK = "OK";
    public final String CMD_BAD = "BAD";
    public final String CMD_LOGIN_OK = "LOGIN_OK";
    public final String CMD_LOGIN_BAD = "LOGIN_BAD";

    private DeviceInfo deviceInfo;

    public DroidTrackerProtocol(DeviceInfo deviceInfo)
    {
        this.deviceInfo = deviceInfo;
    }

    private String formatCommand(String cmd)
    {
        if(!cmd.endsWith(END_COMMAND))
        {
            cmd = cmd.concat(END_COMMAND);
        }

        return cmd;
    }

    /**
     * Cria um Socket TCP com o serviço DroidTracker
     *
     * @throws IOException
     */
    private Socket createTCPSocket() throws IOException
    {
        return new Socket(SERVICE_IP_ADDRESS, TCP_SERVICE_PORT);
    }

    /**
     * Envia um comando para o servidor
     *
     * @param cmd Comando a ser enviado
     * @return  Resposta completa do servidor
     * @throws IOException
     */
    private boolean sendTCPCommand(String cmd, boolean needLogin) throws LoginServiceException, IOException
    {
        Socket clientSocket = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        BufferedReader bufferReader = null;

        boolean opened = false;
        boolean success = false;

        try
        {
            // Cria um socket TCP
            clientSocket = createTCPSocket();
            opened = true;

            Log.i("DROIDTRACKER_LOG", "sendTCPCommand - Socket TCP criado!");

            // Stream de entrada e saída
            inputStream = clientSocket.getInputStream();
            outputStream = clientSocket.getOutputStream();

            String line;
            List<String> response = new ArrayList<>();
            bufferReader = new BufferedReader(new InputStreamReader(inputStream, UTF8_CHARSET));

            if(needLogin)
            {
                // Comando para conexão com o servidor
                final String cmdConnect = formatCommand(
                        String.format("connect>username:%s;password:%s",
                                deviceInfo.getDeviceUuid(), deviceInfo.getDevicePassword()));

                // Envia o comando de login
                outputStream.write(cmdConnect.getBytes());

                Log.i("DROIDTRACKER_LOG", "sendTCPCommand - Comando de login enviado!");

                // Recebe toda a resposta do servidor
                while(!(line = bufferReader.readLine()).equalsIgnoreCase(END_RESPONSE))
                {
                    response.add(line);
                }

                // Verifica se o login foi feito
                boolean connected = response.get(response.size() - 1).equalsIgnoreCase(CMD_LOGIN_OK);

                if(connected)
                {
                    Log.i("DROIDTRACKER_LOG", "sendTCPCommand - Login feito com sucesso!");
                }
                else
                {
                    throw new LoginServiceException();
                }
            }

            // Envia o comando para o servidor
            cmd = formatCommand(cmd);
            outputStream.write(cmd.getBytes());

            Log.i("DROIDTRACKER_LOG", String.format("sendTCPCommand - Comando [ %s ] enviado!", cmd));

            response.clear();

            // Recebe toda a resposta do servidor
            while(!(line = bufferReader.readLine()).equalsIgnoreCase(END_RESPONSE))
            {
                response.add(line);
            }

            success = response.get(response.size() - 1).equalsIgnoreCase(CMD_OK);
            Log.i("DROIDTRACKER_LOG", String.format("sendTCPCommand - Status do comando [ %s ]", success));
        }
        finally
        {
            if(opened)
            {
                // Comando de desconexão
                final String cmdQuit = formatCommand("quit");
                outputStream.write(cmdQuit.getBytes());

                Log.i("DROIDTRACKER_LOG", "sendTCPCommand - Comando 'Quit' enviado!");

                inputStream.close();
                outputStream.close();
                bufferReader.close();
                clientSocket.close();

                Log.i("DROIDTRACKER_LOG", "sendTCPCommand - Socket e Streams fechados!");
            }
        }

        return success;
    }

    private boolean sendUDPCommand(String cmd) throws IOException
    {
        DatagramSocket udpSocket = null;
        boolean success = false;

        try
        {
            udpSocket = new DatagramSocket();

            Log.i("DROIDTRACKER_LOG", "sendUDPCommand - Socket UDP criado!");

            cmd = formatCommand(cmd);
            byte[] sendData = cmd.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(
                    sendData,
                    sendData.length,
                    InetAddress.getByName(SERVICE_IP_ADDRESS),
                    UDP_SERVICE_PORT);

            udpSocket.send(sendPacket);
            success = true;

            Log.i("DROIDTRACKER_LOG", "sendUDPCommand - Pacote UDP enviado!");
        }
        finally
        {
            udpSocket.close();
            Log.i("DROIDTRACKER_LOG", "sendUDPCommand - Socket fechado!");
        }

        return success;
    }

    public boolean newLoginInService() throws LoginServiceException, IOException {
        final String cmdNewLogin = String.format("new-login>username:%s;password:%s",
                deviceInfo.getDeviceUuid(), deviceInfo.getDevicePassword());

        return sendTCPCommand(cmdNewLogin, false);
    }

    public boolean registerDeviceInService() throws LoginServiceException, IOException
    {
        final String cmdRegister = String.format("reg-device>uuid:%s;deviceName:%s;systemName:%s;ipAddress:%s",
                deviceInfo.getDeviceUuid(),
                deviceInfo.getDeviceName(),
                deviceInfo.getDeviceSystemName(),
                NetworkHelper.getLocalIpAddress());

        return sendTCPCommand(cmdRegister, true);
    }

    public boolean sendEchoToService() throws LoginServiceException, IOException
    {
        final String cmdEcho = "echo>DroidTracker";
        return sendTCPCommand(cmdEcho, true);
    }

    public boolean sendDevicePosition(Location location) throws IOException
    {
        if(location != null)
        {
            final String cmdPosition = formatCommand(
                    String.format("send-device-position>uuid:%s;lat:%s;long:%s",
                            deviceInfo.getDeviceUuid(),
                            location.getLatitude(),
                            location.getLongitude()));

            return sendUDPCommand(cmdPosition);
        }

        return false;
    }

    public void waitRemoteComand(final List<IStartTrackerEventReceivedListener> startTrackerListeners,
                                 final List<IStopTrackerEventReceivedListener> stopTrackerListeners) throws LoginServiceException, IOException
    {
        Socket clientSocket = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        BufferedReader bufferReader = null;

        boolean opened = false;

        try
        {
            // Cria um socket TCP
            clientSocket = createTCPSocket();
            clientSocket.setSoTimeout(58000);
            opened = true;

            Log.i("DROIDTRACKER_LOG", "waitRemoteComand - Socket TCP criado!");

            // Stream de entrada e saída
            inputStream = clientSocket.getInputStream();
            outputStream = clientSocket.getOutputStream();

            String line;
            List<String> response = new ArrayList<>();
            bufferReader = new BufferedReader(new InputStreamReader(inputStream, UTF8_CHARSET));

            // Comando para conexão com o servidor
            final String cmdConnect = formatCommand(
                    String.format("connect>username:%s;password:%s",
                            deviceInfo.getDeviceUuid(), deviceInfo.getDevicePassword()));

            // Envia o comando de login
            outputStream.write(cmdConnect.getBytes());

            Log.i("DROIDTRACKER_LOG", "waitRemoteComand - Comando de login enviado!");

            // Recebe toda a resposta do servidor
            while(!(line = bufferReader.readLine()).equalsIgnoreCase(END_RESPONSE))
            {
                response.add(line);
            }

            // Verifica se o login foi feito
            boolean connected = response.get(response.size() - 1).equalsIgnoreCase(CMD_LOGIN_OK);

            if(connected)
            {
                Log.i("DROIDTRACKER_LOG", "waitRemoteComand - Login feito com sucesso!");

                final String cmdState = formatCommand("state>waiting");

                try
                {
                    outputStream.write(cmdState.getBytes());

                    Log.i("DROIDTRACKER_LOG", "waitRemoteComand - Comando State enviado!");

                    // Recebe toda a resposta do servidor
                    while(!(line = bufferReader.readLine()).equalsIgnoreCase(END_RESPONSE))
                    {
                        Log.i("DROIDTRACKER_LOG", "waitRemoteComand - Comando recebido: " + line);

                        if(line.equalsIgnoreCase("start-tracker"))
                        {
                            for(IStartTrackerEventReceivedListener listener : startTrackerListeners)
                            {
                                listener.onStartTrackerReceived();
                            }
                        }
                        else if(line.equalsIgnoreCase("stop-tracker"))
                        {
                            for(IStopTrackerEventReceivedListener listener : stopTrackerListeners)
                            {
                                listener.onStopTrackerReceived();
                            }
                        }
                    }
                }
                catch (SocketTimeoutException ex)
                {
                    Log.i("DROIDTRACKER_LOG", "waitRemoteComand - Timeout!");
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            else
            {
                throw new LoginServiceException();
            }
        }
        finally
        {
            if(opened)
            {
                // Comando de desconexão
                final String cmdQuit = formatCommand("quit");
                outputStream.write(cmdQuit.getBytes());

                Log.i("DROIDTRACKER_LOG", "waitRemoteComand - Comando 'Quit' enviado!");

                inputStream.close();
                outputStream.close();
                bufferReader.close();
                clientSocket.close();

                Log.i("DROIDTRACKER_LOG", "waitRemoteComand - Socket e Streams fechados!");
            }
        }
    }
}
