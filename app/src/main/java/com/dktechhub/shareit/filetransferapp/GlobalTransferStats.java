package com.dktechhub.shareit.filetransferapp;

import android.widget.ProgressBar;
import android.widget.TextView;

public class GlobalTransferStats {
    long total=0;
    long sent=0;
    int progress=0;
    ProgressBar progressBar;
    TextView sentText,timeLeftText,speedText;
    void addToTotal(long l)
    {
        total+=l;
    }
    void updateProgress(long ent)
    {
        sent+=ent;
        progress= (int) (sent*100/total);
        progressBar.setProgress(progress);
    }
}
