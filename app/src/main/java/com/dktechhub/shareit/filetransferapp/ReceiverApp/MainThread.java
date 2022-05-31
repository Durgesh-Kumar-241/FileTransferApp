package com.dktechhub.shareit.filetransferapp.ReceiverApp;

import android.util.Log;

import com.dktechhub.shareit.filetransferapp.ui.main.RecyclerViewAdapter;
import com.dktechhub.shareit.filetransferapp.ui.main.RemoreFilesInterface;

import java.io.IOException;
import java.net.ServerSocket;

public class MainThread extends Thread{
    public boolean isRunning = true;
    int port = 2004;
    HandlerThread.HandlerInterface handlerInterface;
    RemoreFilesInterface remoreFilesInterface;
    public void setRemoreFilesInterface(RemoreFilesInterface remoreFilesInterface) {
        this.remoreFilesInterface = remoreFilesInterface;
        if(remoreFilesInterface==null)
        {
            Log.d("HandlerThread","null interface to main");
        }
    }

    public MainThread(HandlerThread.HandlerInterface handlerInterface)
    {
        this.handlerInterface =handlerInterface;


    }
    @Override
    public void run() {
        super.run();
        while (isRunning)
        {
            listen();
        }
    }
    void listen()
    {
        try {
        ServerSocket serverSocket = new ServerSocket(port);
        while (isRunning)
        {   try {
                new HandlerThread(serverSocket.accept(),handlerInterface,remoreFilesInterface).start();
        }catch (Exception e){
                e.printStackTrace();
            }

        }
        serverSocket.close();
    } catch (IOException e) {
        e.printStackTrace();
    }

    }


}
