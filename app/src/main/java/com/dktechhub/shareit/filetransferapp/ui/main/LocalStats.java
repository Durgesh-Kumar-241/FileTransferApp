package com.dktechhub.shareit.filetransferapp.ui.main;


import com.dktechhub.shareit.filetransferapp.BackgroudToUIRunner;
import com.dktechhub.shareit.filetransferapp.SharedItem;

public class LocalStats {
     static long total=0;
     static long sent=0;
     static int progress=0;
     static String speed="0 MB";
     static String timeLeft = "0 Sec.";
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
        long left =Integer.MAX_VALUE;
        if(sp!=0)
        left = (total-sent)/sp;
        else{
            if(total-sent==0)
                left=0;
        }
        timeLeft = getTime((int) left);
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

    public static String getTimeLeft() {
        return timeLeft;
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

    static String getTime(int sec)
    {
        String str;
        int MIN =60;
        int HRS = 3600;

        if(sec>=HRS)
            str=String.format("%.2f Hrs",(float)sec/HRS);
        else if(sec>=MIN)
            str=String.format("%.2f Min.",(float)sec/MIN);
        else str=String.format("%d", sec);
        return str;
    }
}
