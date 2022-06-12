package com.dktechhub.shareit.filetransferapp.SenderApp;

import androidx.core.util.Pair;

import com.dktechhub.shareit.filetransferapp.BackgroudToUIRunner;
import com.dktechhub.shareit.filetransferapp.SharedItem;
import com.dktechhub.shareit.filetransferapp.ui.main.FileConverter;
import com.dktechhub.shareit.filetransferapp.ui.main.RemoreFilesInterface;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class RemoteObserverThread extends Thread{
    public boolean isCancelled=false;
    RemoreFilesInterface remoreFilesInterface;
    ArrayList<SharedItem> toPush = new ArrayList<>();
    String remoteIP;

    public RemoteObserverThread(RemoreFilesInterface remoreFilesInterface,String remoteIP)
    {
        this.remoreFilesInterface=remoreFilesInterface;
        this.remoteIP=remoteIP;
    }
    @Override
    public void run() {
        super.run();
        while (!isCancelled)
        {
            readWrite();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void readWrite()
    {
        try{
            Socket s = new Socket(remoteIP,2004);
            OutputStream outputStream = s.getOutputStream();
            InputStream inputStream = s.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            outputStream.write("REMOTE / HTTP/1.1\r\n".getBytes());
            outputStream.write("\r\n".getBytes());
            String st = FileConverter.getJson(toPush) +"\r\n";
            outputStream.write(st.getBytes());
            outputStream.write("\r\n".getBytes());

            StringBuilder sb = new StringBuilder();
            String header;

            header = bufferedReader.readLine();
            int temp;
            //System.out.println(header);
            int responseCode = Integer.parseInt(header.substring(header.indexOf(' ')+1,header.indexOf(' ')+4));
            //System.out.println(responseCode);
            while ((header = bufferedReader.readLine()).length() != 0) {
                if (header.contains(":")) {
                    temp = header.indexOf(':');
                    //responceHeaders.put(header.substring(0, temp), header.substring(temp + 2));
                }
            }
            while ((header = bufferedReader.readLine()).length() != 0) {
                //System.out.println(header);
                sb.append(header);
            }
            JSONObject js = new JSONObject(sb.toString());
            Pair<ArrayList<SharedItem>, Long> arrayList = FileConverter.fromJson(js);
            if(arrayList.first.size()>0)
                publishNewItems(arrayList);
            toPush.clear();
            outputStream.close();
            inputStream.close();
            s.close();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }



    public void push(ArrayList<SharedItem> list) {
        toPush.addAll(list);
    }
    void publishNewItems(Pair<ArrayList<SharedItem>, Long> pair)
    {
        if(remoreFilesInterface!=null)
        {
            BackgroudToUIRunner.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    remoreFilesInterface.onNewFilesAvailable(pair);
                }
            });
        }
    }

}
