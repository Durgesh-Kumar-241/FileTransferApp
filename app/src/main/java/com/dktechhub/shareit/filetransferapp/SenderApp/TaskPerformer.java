package com.dktechhub.shareit.filetransferapp.SenderApp;

import android.util.Log;

import com.dktechhub.shareit.filetransferapp.BackgroudToUIRunner;
import com.dktechhub.shareit.filetransferapp.SharedItem;
import com.dktechhub.shareit.filetransferapp.ui.main.Crypto;
import com.dktechhub.shareit.filetransferapp.ui.main.ItemStateChangeListener;
import com.dktechhub.shareit.filetransferapp.ui.main.LocalPathProvider;
import com.dktechhub.shareit.filetransferapp.ui.main.LocalStats;
import com.dktechhub.shareit.filetransferapp.ui.main.ShareState;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;

public class TaskPerformer extends Thread{
    private final String remote;
    ItemStateChangeListener itemStateChangeListener;
    SharedItem sharedItem;
    boolean isCancelled = false;
    String TAG = "TaskPerformer_Tag";
    boolean success = true;
    int index;
    public void cancel()
    {
        isCancelled=true;
        notifyCancelled();
    }
   public TaskPerformer(ItemStateChangeListener itemStateChangeListener, SharedItem sharedItem,int index,String remote)
    {
        this.itemStateChangeListener = itemStateChangeListener;
        this.sharedItem=sharedItem;
        this.remote=remote;
        this.index=index;
    }

    @Override
    public void run() {
        super.run();
        Log.d(TAG,"running task performer");
        if(sharedItem==null||(sharedItem.outGoing&&sharedItem.uri==null))
            notifyTaskCompleted(false);
        else if(sharedItem.shareState==ShareState.CANCELLED)
        {
            notifyTaskCompleted(false);
            return;
        }
        else if(sharedItem.outGoing)
        {
            postItem();
        }else {
            getItem();
        }

        notifyTaskCompleted(success);
        Log.d(TAG,"Finished task performer");
    }

    HashMap<String,String> responceHeaders = new HashMap<>();

    private void getItem() {

        try{
            Socket s = new Socket(remote,2004);
            InputStream inputStream=s.getInputStream();
            OutputStream outputStream = s.getOutputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            outputStream.write(("GET /item?id="+sharedItem.id+" HTTP/1.1\r\n").getBytes());
            outputStream.write(("Content-Length: "+String.valueOf(sharedItem.size)+"\r\n").getBytes());
            outputStream.write("\r\n".getBytes());
            //String res = bufferedReader.readLine();
            String header;
            int temp;
            header = bufferedReader.readLine();
            System.out.println(header);
            int responseCode = Integer.parseInt(header.substring(header.indexOf(' ')+1,header.indexOf(' ')+4));
            // System.out.println(responseCode);
            while ((header = bufferedReader.readLine()).length() != 0) {
                if (header.contains(":")) {
                    temp = header.indexOf(':');
                    responceHeaders.put(header.substring(0, temp), header.substring(temp + 2));
                }
                System.out.println(header);
            }
            Log.d(TAG,"Responce code :"+responseCode+" headers"+responceHeaders);
            if(responseCode==200)
            {

                FileOutputStream fileOutputStream = (FileOutputStream) LocalPathProvider.getOut(sharedItem);
                //CipherInputStream cipherInputStream = Crypto.getDecryptedFile(inputStream);
                //sharedItem.uri = Uri.fromFile()
                long max = sharedItem.size;
                long downloaded =0;
                int read = 0;
                byte[] buffer=new byte[1024*1000];
                while ((read = inputStream.read(buffer)) > 0&&!isCancelled) {

                    downloaded += read;
                    fileOutputStream.write(buffer,0, read);
                    sharedItem.progress=((int)(downloaded*100 / max));
                    //Log.d(TAG,downloaded+"/"+max);
                    notifyItemChanged();
                    LocalStats.updateProgress(read);
                }
                ;
                if(isCancelled)
                {
                    sharedItem.shareState=ShareState.CANCELLED;
                }else {
                    sharedItem.progress=100;
                    sharedItem.shareState=ShareState.COMPLETED;
                }

                notifyItemChanged();

                inputStream.close();
                Log.d(TAG,"Downloaded file completely");
                //  System.out.println("Received File");
                fileOutputStream.flush();
                fileOutputStream.close();
                outputStream.close();
                LocalStats.updateProgress(0);
            }else throw new Exception("Invalid request");


        } catch (Exception e) {
            e.printStackTrace();
            success =false;
            sharedItem.shareState= ShareState.FAILED;notifyItemChanged();notifyTaskCompleted(false);
        }


    }

    private void postItem() {
        try{
            Socket s = new Socket(remote,2004);
            InputStream inputStream=s.getInputStream();
            OutputStream outputStream = s.getOutputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            outputStream.write(("POST /item?id="+sharedItem.id+" HTTP/1.1\r\n").getBytes());
            outputStream.write(("Content-Length: "+ sharedItem.size +"\r\n\r\n").getBytes());
            InputStream inputStream1 = LocalPathProvider.getInputStream(sharedItem);
            long max = sharedItem.size;
            long uploaded =0;
            int read = 0;
            byte[] buffer=new byte[1024*1000];
            //CipherInputStream cipherInputStream = Crypto.getEncryptedFile(inputStream1);
            while ((read = inputStream1.read(buffer)) > 0) {

                uploaded += read;
                outputStream.write(buffer,0, read);
                sharedItem.progress=((int)(uploaded*100 / max));
                //Log.d(TAG,uploaded+"/"+max);
                notifyItemChanged();
                LocalStats.updateProgress(read);
            }
            Log.d(TAG,"upload finished");
            outputStream.write("\r\n\r\n".getBytes());
            inputStream1.close();
            s.shutdownOutput();
            LocalStats.updateProgress(0);
            String header;
            int temp;
            header = bufferedReader.readLine();
            System.out.println(header);
            int responseCode = Integer.parseInt(header.substring(header.indexOf(' ')+1,header.indexOf(' ')+4));
            // System.out.println(responseCode);
            while ((header = bufferedReader.readLine()).length() != 0) {
                if (header.contains(":")) {
                    temp = header.indexOf(':');
                    responceHeaders.put(header.substring(0, temp), header.substring(temp + 2));
                }
                System.out.println(header);
            }
            Log.d(TAG,"Responce code :"+responseCode+" headers"+responceHeaders);

            if(isCancelled)
            {
                sharedItem.shareState=ShareState.CANCELLED;
            }else if(responseCode==200){
                sharedItem.progress=100;
                sharedItem.shareState=ShareState.COMPLETED;
            }
            notifyItemChanged();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            sharedItem.shareState=ShareState.FAILED;
            notifyItemChanged();
        }
    }

    void notifyCancelled()
    {
        BackgroudToUIRunner.runOnUiThread(() -> itemStateChangeListener.onTaskCancelled());
    }
    void notifyTaskCompleted(boolean success)
    {
        BackgroudToUIRunner.runOnUiThread(() -> itemStateChangeListener.onTaskCompleted(success));
    }
    void notifyItemChanged()
    {
        BackgroudToUIRunner.runOnUiThread(() -> itemStateChangeListener.onItemChanged(index));
    }
}
