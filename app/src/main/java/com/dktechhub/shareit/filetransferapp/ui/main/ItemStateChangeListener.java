package com.dktechhub.shareit.filetransferapp.ui.main;

import android.content.ContentResolver;

public interface ItemStateChangeListener {
    void onItemChanged(int index);
    void onTaskCompleted(boolean success);
    void onTaskCancelled();
    ContentResolver getContentResolver();
}
