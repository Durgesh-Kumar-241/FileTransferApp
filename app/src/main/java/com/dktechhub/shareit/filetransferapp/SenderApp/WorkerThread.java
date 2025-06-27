package com.dktechhub.shareit.filetransferapp.SenderApp;

import android.content.ContentResolver;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;

import com.dktechhub.shareit.filetransferapp.BackgroudToUIRunner;
import com.dktechhub.shareit.filetransferapp.SharedItem;
import com.dktechhub.shareit.filetransferapp.ui.main.ShareState;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;


public class WorkerThread extends Thread{
    private static final String TAG = "WorkerThread";
    public boolean isCancelled = false;
    public String remote;
    ContentResolver contentResolver;
    public WorkerThread(ContentResolver contentResolver, String remote)
    {
        this.contentResolver=contentResolver;
        this.remote=remote;
    }
    @Override
    public void run() {
        super.run();
        try{
            while (!isCancelled)
            {
                try{
                    if(queue.isEmpty()) {
                        try {
                            Log.d(TAG,"Waiting for new task....");
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else {
                        Log.d(TAG,"completing task");
                        completeTask(queue.remove());
                    }
                }catch (Exception e2)
                {
                    e2.printStackTrace();
                }
                Log.d(TAG,"Worker running");
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.d(TAG,"Worker stopped");


    }

    private void completeTask(Pair<SharedItem, Integer> p) {
        if(p.first.outGoing)
            doPost(p);
        else doGet(p);
    }

    private void doGet(Pair<SharedItem,Integer> p) {
        Log.d(TAG,"doGet");
        try{
            Socket s = new Socket(remote,2004);
            OutputStream outputStream = s.getOutputStream();
            InputStream inputStream = s.getInputStream();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            outputStream.write(("DOWNLOAD /item?id="+p.first.id+" HTTP/1.1\r\n\r\n").getBytes());
            String header;
        Log.d(TAG,"doget Request sent");
            header = bufferedReader.readLine();
            int temp;
            Log.d(TAG,"doGet res:"+header);
            int responseCode = Integer.parseInt(header.substring(header.indexOf(' ')+1,header.indexOf(' ')+4));
            System.out.println(responseCode);
            while ((header = bufferedReader.readLine()).length() != 0) {
                if (header.contains(":")) {
                    temp = header.indexOf(':');
                    //responceHeaders.put(header.substring(0, temp), header.substring(temp + 2));
                }
            }
            File parent = new File(Environment.getExternalStorageDirectory(),"Shared content");
            if(!(parent.canWrite()||parent.mkdirs()))
                throw new Exception("Storage permission error");
            File f = new File(parent,p.first.name);
            FileOutputStream fileInputStream = new FileOutputStream(f);
            byte[] buf = new byte[1024*1000];
            int read;
            long written=0;
            long max = p.first.size;
            while ((read=inputStream.read(buf))>0)
            {
                fileInputStream.write(buf,0,read);
                written+=read;
                p.first.progress= (int) ((written/max)*100);
                publish(p.second);
            }
            p.first.shareState=ShareState.COMPLETED;
            fileInputStream.close();

            //recyclerViewAdapter.items.get(current).
        }catch (Exception e)
        {
            e.printStackTrace();
            p.first.shareState=ShareState.FAILED;
        }
    publish(p.second);

    }

    private void doPost(Pair<SharedItem,Integer> p) {
        Log.d(TAG,"doPost");

        try{

            Socket s = new Socket(remote,2004);
            OutputStream outputStream = s.getOutputStream();
            InputStream inputStream = s.getInputStream();
            FileInputStream fileInputStream = (FileInputStream) contentResolver.openInputStream(p.first.uri);
            DataInputStream dis = new DataInputStream(new BufferedInputStream(fileInputStream));
            outputStream.write(("UPLOAD /item?id="+p.first.id+" HTTP/1.1\r\n\r\n").getBytes());
            int read=0;long max = p.first.size;
            byte[] buff =new byte[1024*1000];
            long written =0;
            while ((read= dis.read(buff))>0)
            {
                outputStream.write(buff,0,read);
                written+=read;
                p.first.progress= (int) ((written/max)*100);
                publish(p.second);
                Log.d("Antihost sending",written+"/"+max);
            }
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            Log.d(TAG,"sent");
            String res = bufferedReader.readLine();
            Log.d(TAG,res);
            p.first.shareState= ShareState.COMPLETED;
            inputStream.close();
            outputStream.close();
            s.close();

        }catch (Exception e)
        {
            p.first.shareState=ShareState.FAILED;
            e.printStackTrace();
        }
        publish(p.second);
    }

    void publish(int index)
    {
        BackgroudToUIRunner.runOnUiThread(() -> {
            try{if(workerInterface!=null)
                workerInterface.onItemChanged(index);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    public void addWork(Queue<Pair<SharedItem, Integer>> q) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                queue.addAll(q);
            }
        },2000);

    }

    public interface WorkerInterface{
        void onItemChanged(int index);
    }
    
    Queue<Pair<SharedItem,Integer>> queue =new LinkedList<>();
    WorkerInterface workerInterface;

    public void setWorkerInterface(WorkerInterface workerInterface) {
        this.workerInterface = workerInterface;
    }

    void addWork(int index, SharedItem sharedItem)
    {
        queue.add(new Pair<>(sharedItem,index));
    }

}
