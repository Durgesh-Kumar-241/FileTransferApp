package com.dktechhub.shareit.filetransferapp.SenderApp;

import android.util.Log;

import com.dktechhub.shareit.filetransferapp.BackgroudToUIRunner;
import com.dktechhub.shareit.filetransferapp.ui.main.Crypto;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class ConnectThread extends Thread{
    String remoteIP;
    String TAG = "SenderApp TAG";
    String authKey ="";
    boolean cancelled = false;
    ConnecterInterface connecterInterface;
    String serverPublic ="";
    String encryptedSecret = "";
   public ConnectThread(String remoteIP,ConnecterInterface connecterInterface)
    {
        this.remoteIP = remoteIP;
        this.connecterInterface = connecterInterface;
        try {
            Crypto.iniClientKeys();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        //authKey= KeyPairGenerator.getInstance("AES",)
        for(int i=0;i<5&&!cancelled;i++)
        {
            try {
                if(connecterInterface!=null)
                    callProgress(i*100/5);
                connect();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        callFailed(err);

    }
    String err="";
    void connect()
    {
        try{
            Socket socket = new Socket(remoteIP,2004);
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write("CONNECT / HTTP/1.1\r\n".getBytes());
            outputStream.write(("Device: "+SenderApp.deviceName+"\r\n").getBytes());
            if(encryptedSecret.length()>0)
            {
                outputStream.write(("Secret: "+encryptedSecret+"\r\n").getBytes());
                Log.d(TAG,"Encrypted secr sent:\n"+encryptedSecret);
            }
            outputStream.write("\r\n".getBytes());
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            Log.d(TAG,"Waiting for response");
            String res = bufferedReader.readLine();
            Log.d(TAG,res);
            if(res.equals("HTTP/1.1 OK"))
            {   res = bufferedReader.readLine();
                String device = "";
                if (res.contains(":")) {
                     device=res.substring(res.indexOf(':') + 2);
                }
                if(connecterInterface!=null)
                {
                    callSuccess(device);
                    cancelled = true;
                }
            }else if(res.equals("HTTP/1.1 UNAUTHORISED")) {
                if(connecterInterface!=null)
                {
                    callFailed("Receiver device rejected request");
                    cancelled = true;
                }
            }else if(res.equals("HTTP/1.1 WAIT"))
            {
                String serverPublic=bufferedReader.readLine();
                Log.d(TAG,"final: \n"+serverPublic);
                if(serverPublic.contains(":"))
                {
                    serverPublic=serverPublic.substring(serverPublic.indexOf(":")+2);
                    if(this.serverPublic.length()==0)
                        this.serverPublic=serverPublic;
                    try{
                        if(encryptedSecret.length()==0&&serverPublic.length()>0)
                            encryptedSecret=Crypto.getEncryptedSecret(serverPublic);
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    Log.d(TAG,"Server Public:\n"+serverPublic);
                }
                Thread.sleep(1000);
            }
        }catch (Exception e)
        {
            err=e.getMessage();
            e.printStackTrace();        }
    }

    void callSuccess(String device)
    {
        BackgroudToUIRunner.runOnUiThread(() -> connecterInterface.onConnectionSuccess(device));
    }
    void callFailed(String msg)
    {
        BackgroudToUIRunner.runOnUiThread(() -> connecterInterface.onConnectionFailed(msg));
    }

    void callProgress(int progress)
    {
        BackgroudToUIRunner.runOnUiThread(() -> connecterInterface.onProgress(progress));
    }

    public interface ConnecterInterface{
        void onConnectionSuccess(String device);
        void onConnectionFailed(String message);
        void onProgress(int progress);
    }
    //rec

    //JqdjZ/mn9xi9l7/5SS4eoJ9W93oOlQg99yKShwvc+/K5vK6Cu9zQE5wujtj29R5uVPrgao52TIH1
    //JqdjZ/mn9xi9l7/5SS4eoJ9W93oOlQg99yKShwvc+/K5vK6Cu9zQE5wujtj29R5uVPrgao52TIH1
    //    xPllSVhFQq8J3r/KImtDLjoaTyb1fQZAS+FePV5q8dHgDRuB3ZaR0xdtizQRYslkir8QldVI/Exj
    //    XEoOLnbEynCqhXX4nMM=





}

