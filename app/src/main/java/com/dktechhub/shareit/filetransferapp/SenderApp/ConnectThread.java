package com.dktechhub.shareit.filetransferapp.SenderApp;

import android.util.Log;

import com.dktechhub.shareit.filetransferapp.BackgroudToUIRunner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class ConnectThread extends Thread{
    String remoteIP;
    String TAG = "SenderApp TAG";
    boolean cancelled = false;
    ConnecterInterface connecterInterface;
   public ConnectThread(String remoteIP,ConnecterInterface connecterInterface)
    {
        this.remoteIP = remoteIP;
        this.connecterInterface = connecterInterface;
    }

    @Override
    public void run() {
        super.run();
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
            outputStream.write("\r\n".getBytes());
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            Log.d(TAG,"Waiting for response");
            String res = bufferedReader.readLine();
            Log.d(TAG,res);
            if(res.equals("HTTP/1.1 OK"))
            {
                if(connecterInterface!=null)
                {
                    callSuccess();
                    cancelled = true;
                }
            }else if(res.equals("HTTP/1.1 UNAUTHORISED")) {
                if(connecterInterface!=null)
                {
                    callFailed("Receiver device rejected request");
                    cancelled = true;
                }else if(res.contains("WAIT"))
                {
                    Thread.sleep(10000);
                }
            }
        }catch (Exception e)
        {
            err=e.getMessage();
            e.printStackTrace();        }
    }

    void callSuccess()
    {
        BackgroudToUIRunner.runOnUiThread(() -> connecterInterface.onConnectionSuccess());
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
        void onConnectionSuccess();
        void onConnectionFailed(String message);
        void onProgress(int progress);
    }
}

