package com.dktechhub.shareit.filetransferapp.ui.main;


import com.dktechhub.shareit.filetransferapp.BackgroudToUIRunner;
import com.dktechhub.shareit.filetransferapp.SharedItem;

public class LocalStats {
     static long total=0;
     static long sent=0;
     static int progress=0;
     static String speed="";
    static long prevTime=0;

   public static void addNewItems(long l)
    {
        total+=l;
        publish();
    }
    public static void updateProgress(long sentNew)
    {
        sent+=sentNew;
        progress=(int)(sent*100/total);
        long sec = (System.currentTimeMillis()-prevTime);
        long sp = 0;
        if(sec!=0)
        sp = 1000*(sentNew)/sec;
        speed=SharedItem.getSize(sp)+"/Sec";
        prevTime=System.currentTimeMillis();
        publish();
    }

    public static String getTotal()
    {return SharedItem.getSize(total);}

    public static int getProgress() {
        return progress;
    }

    public static String getSent() {
        return SharedItem.getSize(sent);
    }

    public static String getSpeed() {
        return speed;
    }
    static LocalStateChangeInterface localStateChangeInterface;

   public static void initialize(LocalStateChangeInterface mlocalStateChangeInterface)
   {
       localStateChangeInterface =mlocalStateChangeInterface;
   }
    public static void release()
    {
        localStateChangeInterface=null;
    }
    static Runnable r = new Runnable() {
        @Override
        public void run() {
            if(localStateChangeInterface!=null)
                localStateChangeInterface.onUpdate();
        }
    };
    static void publish()
    {
        if(localStateChangeInterface!=null)
            BackgroudToUIRunner.runOnUiThread(r);
    }


    public interface LocalStateChangeInterface{
       void onUpdate();
    }
}
