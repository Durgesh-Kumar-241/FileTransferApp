package com.dktechhub.shareit.filetransferapp.ui.main;

import android.os.Environment;

import java.io.File;

public class LocalPathProvider {
    static File parent = new File(Environment.getExternalStorageDirectory(),"Shared");
    static File videos = new File(parent,"Videos");
    static File images = new File(parent,"Images");
    static File docs = new File(parent,"Documents");
    static File audios = new File(parent,"Audios");

    public static File getLocalFile(String type)
    {   //File parent = new File(Environment.getExternalStorageDirectory(),"Shared");
        if(type.contains("video"))
            return videos;
        else if(type.contains("image"))
            return images;

        else if(type.contains("audio"))
            return audios;

        else return docs;

    }
}
