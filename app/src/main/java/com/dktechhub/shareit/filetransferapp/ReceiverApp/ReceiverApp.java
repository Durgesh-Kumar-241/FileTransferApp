package com.dktechhub.shareit.filetransferapp.ReceiverApp;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.dktechhub.shareit.filetransferapp.BackgroudToUIRunner;
import com.dktechhub.shareit.filetransferapp.SharedItem;
import com.dktechhub.shareit.filetransferapp.ui.main.RecyclerViewAdapter;
import com.dktechhub.shareit.filetransferapp.ui.main.RemoreFilesInterface;
import com.dktechhub.shareit.filetransferapp.ui.main.TransferStateInterface;

import java.util.ArrayList;

public class ReceiverApp {
    MainThread mainThread;
    private static ReceiverApp instance;

    ContentResolver contentResolver;
    Context context;
    private ArrayList<String> allowedDevices=new ArrayList<>();
    private ArrayList<String> deninedDevice = new ArrayList<>();
    private ArrayList<String> pending=new ArrayList<>();
    TransferStateInterface transferStateInterface;
    RemoreFilesInterface remoreFilesInterface;
    ArrayList<SharedItem> toPush = new ArrayList<>();
    private RecyclerViewAdapter recyclerViewAdapter;

    public void setRemoreFilesInterface(RemoreFilesInterface remoreFilesInterface) {
        this.remoreFilesInterface = remoreFilesInterface;
        if(mainThread!=null)
        {
            mainThread.setRemoreFilesInterface(remoreFilesInterface);
        }
    }

    public static ReceiverApp getInstance(Context context, TransferStateInterface transferStateInterface)
    {
        if(instance==null)
            instance=new ReceiverApp(context,transferStateInterface);
        return instance;
    }

    public void initialize(ContentResolver contentResolver,RecyclerViewAdapter recyclerViewAdapter)
    {
        this.contentResolver=contentResolver;
        this.recyclerViewAdapter=recyclerViewAdapter;
    }
    public ReceiverApp(Context context,TransferStateInterface transferStateInterface)
    {
        this.context = context;
        this.transferStateInterface =transferStateInterface;
    }

    public void start()
    {
        if(mainThread==null||!mainThread.isRunning)
        {
            mainThread=new MainThread(new HandlerThread.HandlerInterface() {

                @Override
                public ArrayList<SharedItem> getItemsList() {
                    return toPush;
                }

                @Override
                public RecyclerViewAdapter getAdapter() {
                    return ReceiverApp.this.recyclerViewAdapter;
                }


                @Override
                public int addNewDevice(String remote, String device) {
                    return ReceiverApp.this.addNewDevice(remote,device);
                }
            });
            mainThread.start();
            mainThread.setRemoreFilesInterface(remoreFilesInterface);
        }

    }
    public void stop()
    {
        if(mainThread!=null)
            mainThread.isRunning=false;
    }

    //onNewConnectionRequest()

    int addNewDevice( String remote,String device){
        Log.d("ReceiverApp",remote+" requested");
        if(allowedDevices.contains(remote))
            return 1;
        else if(deninedDevice.contains(remote))
            return 0;
        else if(pending.contains(remote))
            return  -1;
        else {
            pending.add(remote);
            BackgroudToUIRunner.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(device+" wants to connect.. allow?");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            allowedDevices.add(remote);
                            transferStateInterface.onConnectionSuccess(null,device);
                        }
                    });
                    builder.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {dialog.dismiss();
                            dialog.dismiss();
                            deninedDevice.add(remote);
                        }
                    }

                    );
                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            deninedDevice.add(remote);
                        }
                    });
                    builder.create().show();
                }
            });
        }
        return -1;
    }

    public void pushFiles(ArrayList<SharedItem> list)
    {
        toPush.addAll(list);
    }
}
