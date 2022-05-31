package com.dktechhub.shareit.filetransferapp.ui.main;

import java.util.Random;

public class IDProvider {
    static Random random;
    public static String getNewId()
    {   if(random==null)
        random = new Random();
        random.setSeed(System.currentTimeMillis());
        return "a"+ random.nextInt(1000);
    }
}
