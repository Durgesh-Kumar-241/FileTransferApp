package com.dktechhub.shareit.filetransferapp;

import android.net.Uri;

import com.dktechhub.shareit.filetransferapp.ui.main.ShareState;

import org.intellij.lang.annotations.Identifier;

public class SharedItem {
    public Uri uri;
    public String name;
    public long size;
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
        outGoing=true;
        this.id=id;
    }
    public SharedItem(String name,long size,String type,String id)
    {
        this.name=name;this.size=size;this.type=type;
        this.id=id;
    }
}
