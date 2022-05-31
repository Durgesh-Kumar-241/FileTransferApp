package com.dktechhub.shareit.filetransferapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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
    {   if(!canWrite())
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},100);
            }
        }else if(!isLanConnected())
        {
        alertDialog.show();
        }else
        startActivity(new Intent(MainActivity.this,ShareActivity.class).putExtra("receiver",b));

    }

    boolean isLanConnected()
    {
        return MWebHotspot.getInstance(this).getLocalIP().length()>0;
    }
    boolean canWrite()
    {
        return !(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M&&Build.VERSION.SDK_INT<Build.VERSION_CODES.Q&&checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}