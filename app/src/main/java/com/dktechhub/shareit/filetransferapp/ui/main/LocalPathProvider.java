package com.dktechhub.shareit.filetransferapp.ui.main;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.dktechhub.shareit.filetransferapp.BuildConfig;
import com.dktechhub.shareit.filetransferapp.SharedItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class LocalPathProvider {
    static ContentResolver cmontentResolver;
    static void initialize(ContentResolver contentResolver,LocalProvider localProvider)
    {
        cmontentResolver=contentResolver;
        mlocalProvider=localProvider;
    }
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

    public static InputStream getInputStream(SharedItem sharedItem) throws FileNotFoundException {
        if(cmontentResolver==null)
        {
            throw new RuntimeException("Provider not initialized correctly");
        }else {
            return cmontentResolver.openInputStream(sharedItem.uri);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static OutputStream getOutputStream(SharedItem sharedItem) throws FileNotFoundException {
        if(cmontentResolver==null)
        {
            throw new RuntimeException("Provider not initialized correctly");
        }else {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,sharedItem.name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE,sharedItem.type);
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS+"/File transfer App/");
            Uri uri = cmontentResolver.insert(MediaStore.Files.getContentUri("external"),contentValues);
            if(uri!=null)
                sharedItem.uri=uri;
            return cmontentResolver.openOutputStream(uri);
        }
    }

    public static OutputStream getOutputStream_low_apis(SharedItem sharedItem) throws FileNotFoundException {
        File parent = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"/File transfer App/");
        Log.d("Local path provide..",parent.getAbsolutePath()+" writable "+parent.canWrite()+" isDir"+parent.isDirectory());
        if((parent.isDirectory()&&parent.canWrite()||parent.mkdirs()))
        {
            File f = new File(parent,sharedItem.name);
            sharedItem.uri= FileProvider.getUriForFile(mlocalProvider.getmApplicationContext(), BuildConfig.APPLICATION_ID + ".provider",f);
            return new FileOutputStream(f);

        } return null;
    }

    public static OutputStream getOut(SharedItem s) throws FileNotFoundException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return getOutputStream(s);
        }else
        {
            return getOutputStream_low_apis(s);
        }
    }
    static LocalProvider mlocalProvider;
    public interface LocalProvider{
        Context getmApplicationContext();
    }


}
