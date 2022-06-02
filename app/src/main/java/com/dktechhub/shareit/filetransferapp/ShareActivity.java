package com.dktechhub.shareit.filetransferapp;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.dktechhub.shareit.filetransferapp.ReceiverApp.ReceiverApp;
import com.dktechhub.shareit.filetransferapp.ui.main.ReceiverFragment;
import com.dktechhub.shareit.filetransferapp.ui.main.SenderFragment;
import com.dktechhub.shareit.filetransferapp.ui.main.TransferScreenFragment;
import com.dktechhub.shareit.filetransferapp.ui.main.TransferStateInterface;

public class ShareActivity extends AppCompatActivity implements TransferStateInterface {
    boolean isActive = false;
    boolean sender = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        if (savedInstanceState == null) {
            boolean receiver = getIntent().getBooleanExtra("receiver",false);
            if(receiver){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, ReceiverFragment.newInstance(this))
                        .commitNow();
                sender = false;
                ReceiverApp.getInstance(ShareActivity.this,this).start();
            }

            else {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, SenderFragment.newInstance(this)).commitNow();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ReceiverApp.getInstance(ShareActivity.this,this).stop();
    }

    @Override
    public void onConnectionSuccess(String remote, String device) {
        if(getSupportActionBar()!=null)
            getSupportActionBar().hide();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, TransferScreenFragment.newInstance(sender,remote,device)).commitNow();
        isActive = true;
    }

    @Override
    public void onConnectionFailed() {
        isActive = false;
    }

    @Override
    public void onTrasferComplete() {
        isActive = false;
    }

    @Override
    public void onBackPressed() {
        if(!isActive)
        {
            super.onBackPressed();
            finish();
        }

        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Stop sending file?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                dialog.dismiss();
                finish();
            }).setNegativeButton("No", (dialog, which) -> dialog.dismiss()).create().show();
        }
    }


}