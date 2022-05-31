package com.dktechhub.shareit.filetransferapp;

import android.os.Handler;
import android.os.Looper;

public class BackgroudToUIRunner {
    public static void runOnUiThread(Runnable r)
    {
        new Handler(Looper.getMainLooper()).post(r);
    }
}
