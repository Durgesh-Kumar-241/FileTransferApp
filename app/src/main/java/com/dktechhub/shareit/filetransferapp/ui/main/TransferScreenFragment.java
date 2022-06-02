package com.dktechhub.shareit.filetransferapp.ui.main;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dktechhub.shareit.filetransferapp.R;
import com.dktechhub.shareit.filetransferapp.ReceiverApp.ReceiverApp;
import com.dktechhub.shareit.filetransferapp.SenderApp.SenderApp;
import com.dktechhub.shareit.filetransferapp.SharedItem;

import java.util.ArrayList;

public class TransferScreenFragment extends Fragment implements RemoreFilesInterface{
    private final String device;
    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;
    boolean sender;
    String remote;
    public TransferScreenFragment(boolean sender, String remote, String device) {
    this.sender=sender;
    this.remote=remote;
    this.device=device;
    }

    public static TransferScreenFragment newInstance(boolean sender,String remote,String device) {
        return new TransferScreenFragment(sender,remote,device);
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root= inflater.inflate(R.layout.fragment_transfer_screen, container, false);
        root.findViewById(R.id.send_file).setOnClickListener(v -> pickFiles());
        ((TextView)root.findViewById(R.id.rem_dev_name)).setText("Connected to "+device);
        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter=new RecyclerViewAdapter(sender);
        //adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
        LocalPathProvider.initialize(getActivity().getContentResolver(), this::getContext);

        if(sender)
        {
            SenderApp instance = SenderApp.getInstance();
            instance.setRemoreFilesInterface(this);
            instance.startRemoteObserver(remote);
            instance.initialize(getActivity().getContentResolver(),remote,adapter);
            instance.startTransfer();
        }
        else {
            ReceiverApp receiverApp= ReceiverApp.getInstance(null,null);
            receiverApp.setRemoreFilesInterface(this);
            receiverApp.initialize(getActivity().getContentResolver(),adapter);
        }
        return root;
    }
    void pickFiles()
    {
        Intent i = new Intent();
        i.setType("*/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        startActivityForResult(Intent.createChooser(i,"Select images to add"),100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100&&resultCode== Activity.RESULT_OK&&data!=null)
        {
            ArrayList<Uri> arrayList = new ArrayList<>();
            if (data.getData() != null) {
                arrayList.add(data.getData());
            } else if (data.getClipData() != null) {
                ClipData clipData = data.getClipData();

                for (int i = 0; i < clipData.getItemCount(); i++)
                    arrayList.add(clipData.getItemAt(i).getUri());
            }
            if (arrayList.size() > 0)
                processSelected(arrayList);
        }
    }

    void processSelected(ArrayList<Uri> arrayList)
    {
        ContentResolver contentResolver = getActivity().getContentResolver();
        ArrayList<SharedItem> arrayList1=new ArrayList<>();
        String id = IDProvider.getNewId();
        for(int i=0;i<arrayList.size();i++)
        {
            Cursor cursor = contentResolver.query(arrayList.get(i),null,null,null,null);
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            cursor.moveToFirst();
            arrayList1.add(new SharedItem(arrayList.get(i),cursor.getString(nameIndex),cursor.getLong(sizeIndex), contentResolver.getType(arrayList.get(i)),id+i));
            cursor.close();
        }

        if(arrayList1.size()>0)
            updateList(arrayList1);

    }

    void updateList(ArrayList<SharedItem> list)
    {
        adapter.addItems(list);
        if(sender)
        {
            SenderApp.getInstance().pushFiles(list);
        }
        else ReceiverApp.getInstance(null,null).pushFiles(list);
    }

    @Override
    public void onNewFilesAvailable(ArrayList<SharedItem> sharedItems) {
        adapter.addItems(sharedItems);
        Log.d("HandlerThread","update items");
    }
}