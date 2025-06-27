package com.dktechhub.shareit.filetransferapp.ReceiverApp;

import android.util.Log;

import com.dktechhub.shareit.filetransferapp.BackgroudToUIRunner;
import com.dktechhub.shareit.filetransferapp.SenderApp.SenderApp;
import com.dktechhub.shareit.filetransferapp.SharedItem;
import com.dktechhub.shareit.filetransferapp.ui.main.Crypto;
import com.dktechhub.shareit.filetransferapp.ui.main.FileConverter;
import com.dktechhub.shareit.filetransferapp.ui.main.LocalPathProvider;
import com.dktechhub.shareit.filetransferapp.ui.main.RecyclerViewAdapter;
import com.dktechhub.shareit.filetransferapp.ui.main.RemoreFilesInterface;
import com.dktechhub.shareit.filetransferapp.ui.main.ShareState;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;

public class HandlerThread extends Thread{
    private final Socket socket;
    private String requestMethod;
    private String requestUri;
    private final HashMap<String,String> requestHeaders=new HashMap<>();
    private InputStream inputStream;
    private OutputStream outputStream;
    private InputStreamReader inputStreamReader;
    private BufferedReader bufferedReader;
    HandlerInterface handlerInterface;
    String TAG = "HandlerThread Log";
    private PrintWriter printWriter;

    private final RemoreFilesInterface remoreFilesInterface;
    HandlerThread(Socket socket,HandlerInterface handlerInterface,RemoreFilesInterface remoreFilesInterface)
    {
        this.socket=socket;
        this.handlerInterface=handlerInterface;
        this.remoreFilesInterface=remoreFilesInterface;
    }

    @Override
    public void run() {
        super.run();
        try{
            this.inputStream=socket.getInputStream();
            this.outputStream=socket.getOutputStream();
            this.inputStreamReader = new InputStreamReader(this.inputStream);
            this.bufferedReader = new BufferedReader(inputStreamReader);
            this.printWriter=new PrintWriter(outputStream);
            decodeHeaders();
            Log.d(TAG,"decoded headers");
            //recyclerViewAdapter= handlerInterface.getAdapter();
            handle();

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    void handle()
    {
        switch (requestMethod) {
            case "CONNECT":
                handleConnectionRequest();
                break;
            case "REMOTE":
                handleList();
                break;
            case "POST":
                Log.d(TAG,"post requested");
                handleUp();
                break;
            case "GET":
                Log.d(TAG,"get requested");
                respondFile();
                break;
        }
    }



    void respondFile()
    {
        Log.d(TAG,"Respond file: requested--"+requestUri);
        try{
            recyclerViewAdapter= handlerInterface.getAdapter();
            String id = requestUri.substring(requestUri.indexOf("id=")+3);
            Log.d(TAG,recyclerViewAdapter.map.toString());
            Log.d(TAG,"requested : i= "+id);
            int index = recyclerViewAdapter.getItemIndex(id);

            if(index==-1||(recyclerViewAdapter.items.get(index))==null||recyclerViewAdapter.items.get(index).uri==null)
            {
                newSimpleResponse(404);
                return;
            }

            Log.d(TAG,"Requested item name: "+recyclerViewAdapter.items.get(index).name);
            long max = recyclerViewAdapter.items.get(index).size;

            InputStream fileInputStream =  LocalPathProvider.getInputStream(recyclerViewAdapter.items.get(index));
            //CipherInputStream cipherInputStream = Crypto.getEncryptedFile(fileInputStream);
            outputStream.write("HTTP/1.1 200\r\n\r\n".getBytes());
            long written =0;
            int read =0;
            byte[] bytes = new byte[1024*1000];
            while ((read=fileInputStream.read(bytes))>0)
            {
                written+=read;
                outputStream.write(bytes,0,read);
                recyclerViewAdapter.items.get(index).progress= ((int)(written*100 / max));
                notifyAdapter(index);
                //Log.d(TAG,"Download returned:"+written+"/"+max);
            }
            Log.d(TAG,"Returned complete");

            recyclerViewAdapter.items.get(index).shareState=ShareState.COMPLETED;
            notifyAdapter(index);
            fileInputStream.close();
        } catch (IOException e)
        {
            //respondText(404,"File not found");
            //recyclerViewAdapter.items.get(index).shareState=ShareState.FAILED;
            //notifyAdapter(index);
        }


        onDestroy();
    }
    void newSimpleResponse(int code)
    {
        try{
            outputStream.write(("HTTP/1.1 "+code+"\r\n\r\n").getBytes());
            onDestroy();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public RecyclerViewAdapter recyclerViewAdapter;

    public void handleUp()
    {
        Log.d(TAG,"Receive file: requested--"+requestUri);

        int index=-1;
        try{
            recyclerViewAdapter= handlerInterface.getAdapter();
            String id = requestUri.substring(requestUri.indexOf("id=")+3);
            Log.d(TAG,recyclerViewAdapter.map.toString());
            Log.d(TAG,"requested : i= "+id);
            index = recyclerViewAdapter.getItemIndex(id);
            if(index==-1||(recyclerViewAdapter.items.get(index))==null)
            {
                newSimpleResponse(404);
                Log.d(TAG,"item null");
                return;
            }

            Log.d(TAG,"Requested item name: "+recyclerViewAdapter.items.get(index).name);
            long max = recyclerViewAdapter.items.get(index).size;
            FileOutputStream foutputStream = (FileOutputStream) LocalPathProvider.getOut(recyclerViewAdapter.items.get(index));
            if(outputStream==null)
            {
                newSimpleResponse(404);
                return;
            }
            long written =0;
            int read =0;
            byte[] bytes = new byte[1024*1000];
            Log.d(TAG,"Started receiving....");
           // CipherInputStream cipherInputStream = Crypto.getDecryptedFile(inputStream);
            while (written<=max&&(read=inputStream.read(bytes))>0)
            {
                written+=read;
                foutputStream.write(bytes,0,read);
                recyclerViewAdapter.items.get(index).progress= ((int)(written*100 / max));
                notifyAdapter(index);
                //Log.d(TAG,"File received"+written+"/"+max);
            }

            Log.d(TAG,"completed receiving..");
            foutputStream.flush();
            foutputStream.close();

            recyclerViewAdapter.items.get(index).shareState=ShareState.COMPLETED;
            notifyAdapter(index);
            Log.d(TAG,"completed receiving..");
            outputStream.write("HTTP/1.1 200\r\n\r\n".getBytes());
            outputStream.close();
            inputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            newSimpleResponse(404);
            Log.d(TAG,"file create error");
        } catch (IOException e)
        {
            e.printStackTrace();
        }


        onDestroy();

    }





    void notifyAdapter(int index)
    {   if(recyclerViewAdapter!=null)
        BackgroudToUIRunner.runOnUiThread(() -> recyclerViewAdapter.notifyItemChanged(index));
    }


    private void handleList() {
        try{
            StringBuilder sb = new StringBuilder();
            String header;
            while ((header = bufferedReader.readLine()).length() != 0) {
                //System.out.println(header);
                sb.append(header);
            }
            outputStream.write("HTTP/1.1 200 OK\r\n".getBytes());
            outputStream.write("\r\n".getBytes());
            String s = FileConverter.getJson(handlerInterface.getItemsList()) +"\r\n";
            outputStream.write(s.getBytes());
            outputStream.write("\r\n".getBytes());
            //Log.d(TAG,"file pushed");
            JSONObject js = new JSONObject(sb.toString());
            ArrayList<SharedItem> arrayList = FileConverter.fromJson(js);
            //Log.d("HandlerThread",arrayList.toString());
            if(arrayList.size()>0)
            publishNewItems(arrayList);
            handlerInterface.getItemsList().clear();
            outputStream.close();
            inputStream.close();
            socket.close();

        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void handleConnectionRequest() {
        int i=handlerInterface.addNewDevice(socket.getInetAddress().getHostAddress(),requestHeaders.get("Device"));

        Log.d(TAG,"current status ="+i);
        try{
            if(i==1)
            {
                outputStream.write("HTTP/1.1 OK\r\n".getBytes());
                outputStream.write(("Device: "+ SenderApp.deviceName+"\r\n\r\n").getBytes());
                handlerInterface.onConnectionSuccess(requestHeaders.get("Device"));

            }else if(i==0)
            {
                outputStream.write("HTTP/1.1 UNAUTHORISED\r\n\r\n".getBytes());
            }else {
                outputStream.write("HTTP/1.1 WAIT\r\n\r\n".getBytes());

            }
            Log.d(TAG,"Responded : "+ i);
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void decodeHeaders() throws IOException {
        String header;
        int temp;
        header = bufferedReader.readLine();
        this.requestMethod=(header.substring(0, header.indexOf(' ')));
        this.requestUri= URLDecoder.decode(header.substring(header.indexOf(' ') + 1, header.lastIndexOf(' ')));
        Log.d(TAG,requestMethod);
        Log.d(TAG,requestUri);
        while ((header = bufferedReader.readLine()).length() != 0) {
            if (header.contains(":")) {
                temp = header.indexOf(':');
                this.requestHeaders.put(header.substring(0, temp), header.substring(temp + 2));
            }
        }

        Log.d(TAG,requestHeaders.toString());
    }

    void onDestroy()
    {
        try{
            outputStream.close();
            inputStream.close();
            printWriter.close();
            bufferedReader.close();
            inputStreamReader.close();
            socket.close();
        }catch (Exception e)
        {
            e.printStackTrace();
            Log.d(TAG,e.toString());
        }
    }
    public interface HandlerInterface{
        //void onNewConnectionRequest(HandlerThread handlerThread);
        //Context getContext();
        ArrayList<SharedItem> getItemsList();
        RecyclerViewAdapter getAdapter();
        int addNewDevice(String remote, String device);
        void onConnectionSuccess(String device);
    }

    void publishNewItems(ArrayList<SharedItem> sharedItems)
    {
        if(remoreFilesInterface!=null)
        {
            BackgroudToUIRunner.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    remoreFilesInterface.onNewFilesAvailable(sharedItems);
                }
            });
        }else {
            Log.d(TAG,"null interface");
        }
    }

}
