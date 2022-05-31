package com.dktechhub.shareit.filetransferapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    AlertDialog alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.send).setOnClickListener(v -> mm1(false));
        findViewById(R.id.receive).setOnClickListener(v -> mm1(true));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Lan not connected!! Connect using your wifi/hotspot");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        alertDialog = builder.create();
    }
    void mm1(boolean b)
    {   if(isLanConnected())
        startActivity(new Intent(MainActivity.this,ShareActivity.class).putExtra("receiver",b));
        else {
        alertDialog.show();
        }
    }

    boolean isLanConnected()
    {
        return MWebHotspot.getInstance(this).getLocalIP().length()>0;
    }
}