package com.dktechhub.shareit.filetransferapp.ui.main;

import com.dktechhub.shareit.filetransferapp.SharedItem;

import java.util.ArrayList;

public interface RemoreFilesInterface {
    void onNewFilesAvailable(ArrayList<SharedItem> sharedItems);
}
