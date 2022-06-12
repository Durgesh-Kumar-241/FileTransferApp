package com.dktechhub.shareit.filetransferapp.ui.main;

import androidx.core.util.Pair;

import com.dktechhub.shareit.filetransferapp.SharedItem;

import java.util.ArrayList;

public interface RemoreFilesInterface {

    void onNewFilesAvailable(Pair<ArrayList<SharedItem>, Long> p);
}
