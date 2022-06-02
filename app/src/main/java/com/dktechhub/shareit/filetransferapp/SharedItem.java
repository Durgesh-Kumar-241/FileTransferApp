package com.dktechhub.shareit.filetransferapp;

import android.net.Uri;

import com.dktechhub.shareit.filetransferapp.ui.main.ShareState;

import org.intellij.lang.annotations.Identifier;

public class SharedItem {
    public Uri uri;
    public String name;
    public long size;
    public String size2="";
    public String type;
    public ShareState shareState=ShareState.PENDING;
    public int progress=0;
    public boolean outGoing=false;
    public String id ="";
    public SharedItem(Uri uri,String name,long size,String type,String id)
    {
        this.name=name;
        this.type=type;
        this.uri=uri;
        this.size=size;
        size2=getSize(size);
        outGoing=true;
        this.id=id;
    }
    public SharedItem(String name,long size,String type,String id)
    {
        this.name=name;this.size=size;this.type=type;
        size2=getSize(size);
        this.id=id;
    }
    public static String getSize(long num)
    {
        String str;
        int KB = 1000;
        int MB = KB*1000;
        int GB = MB*1000;
        if(num>=GB)
            str=String.format("%.2f GB",(float)num/GB);
        else if(num>=MB)
            str=String.format("%.2f MB",(float)num/MB);
        else if(num>=KB)
            str=String.format("%.2f KB",(float)num/KB);
        else str=String.format("%s B", num);
        return str;
    }
}
