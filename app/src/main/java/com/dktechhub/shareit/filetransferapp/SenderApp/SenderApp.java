package com.dktechhub.shareit.filetransferapp.SenderApp;

import android.content.ContentResolver;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;


import com.dktechhub.shareit.filetransferapp.SharedItem;
import com.dktechhub.shareit.filetransferapp.ui.main.ItemStateChangeListener;
import com.dktechhub.shareit.filetransferapp.ui.main.RecyclerViewAdapter;
import com.dktechhub.shareit.filetransferapp.ui.main.RemoreFilesInterface;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class SenderApp {
    private static SenderApp instance;
    private RemoreFilesInterface remoreFilesInterface;
    RemoteObserverThread remoteObserverThread;
    TaskPerformer t;

    RecyclerViewAdapter recyclerViewAdapter;
    public static String deviceName=Build.DEVICE;
    String remote;
    Queue<Pair<SharedItem,Integer>> tasks=new LinkedList<>();
    ContentResolver contentResolver;
    public void initialize(ContentResolver contentResolver,String remote,RecyclerViewAdapter recyclerViewAdapter)
    {
        this.contentResolver=contentResolver;
        this.remote=remote;
        this.recyclerViewAdapter=recyclerViewAdapter;
        recyclerViewAdapter.setSenderAppCall(this::addTasks);
    }


    public void addTasks(Queue<Pair<SharedItem,Integer>> q)
    {
        tasks.addAll(q);

    }
    public void setRemoreFilesInterface(RemoreFilesInterface remoreFilesInterface) {
        this.remoreFilesInterface = remoreFilesInterface;
    }

    public static SenderApp getInstance()
    {
        if(instance==null)
            instance=new SenderApp();
        return instance;
    }

    public void startRemoteObserver(String remote)
    {
        if(remoteObserverThread==null|| remoteObserverThread.isCancelled)
        {
            remoteObserverThread = new RemoteObserverThread(remoreFilesInterface,remote);
            remoteObserverThread.start();
        }
    }
    public void startWorker(Pair<SharedItem,Integer> p)
    {
        t = new TaskPerformer(new ItemStateChangeListener() {
            @Override
            public void onItemChanged(int index) {
                recyclerViewAdapter.notifyItemChanged(index);
            }

            @Override
            public void onTaskCompleted(boolean success) {
                doNext();
            }

            @Override
            public void onTaskCancelled() {
                doNext();
            }

            @Override
            public ContentResolver getContentResolver() {
                return contentResolver;
            }
        },p.first,p.second,remote);

        t.start();

    }

    private void doNext() {
        Log.d("SenderApp","doNext called");
        if(!tasks.isEmpty())
        {
            startWorker(tasks.remove());
        }else {
            new Handler().postDelayed(this::doNext,5000);
        }
    }


    public void stopWorker()
    {
        t.isCancelled=true;
    }

    public void stopRemoteObserverThread()
    {
        if(remoteObserverThread!=null)
            remoteObserverThread.isCancelled=true;
    }

    public void pushFiles(ArrayList<SharedItem> list)
    {
        if(remoteObserverThread!=null)
            remoteObserverThread.push(list);
    }



    public void close()
    {
        stopRemoteObserverThread();
        stopWorker();
    }
   public void startTransfer()
    {
        doNext();
    }
}
