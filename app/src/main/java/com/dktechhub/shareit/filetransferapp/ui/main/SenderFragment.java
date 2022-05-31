package com.dktechhub.shareit.filetransferapp.ui.main;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dktechhub.shareit.filetransferapp.R;
import com.dktechhub.shareit.filetransferapp.SenderApp.ConnectThread;

public class SenderFragment extends Fragment {
    TransferStateInterface transferStateInterface;
    public SenderFragment(TransferStateInterface transferStateInterface) {
        // Required empty public constructor
        this.transferStateInterface=transferStateInterface;
    }


    public static SenderFragment newInstance(TransferStateInterface transferStateInterface) {
        SenderFragment fragment = new SenderFragment(transferStateInterface);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    EditText ipE;
    Button connect;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sender, container, false);
        connect = root.findViewById(R.id.button_conn);
        connect.setOnClickListener(v -> tryToConnect());
        ipE = root.findViewById(R.id.in_rem_ip);
        return  root;
    }

    void tryToConnect()
    {   String remote = ipE.getText().toString();
        if(remote.length()==0)
        {
            Toast.makeText(getContext(), "Enter receiver device address..", Toast.LENGTH_SHORT).show();
            return;
        }
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Connecting...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgress(1);
        progressDialog.show();
        new ConnectThread(remote, new ConnectThread.ConnecterInterface() {
            @Override
            public void onConnectionSuccess() {
                //Toast.makeText(getContext(), "Connected successfully", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                transferStateInterface.onConnectionSuccess(remote);
            }

            @Override
            public void onConnectionFailed(String message) {
                //Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                connect.setText("Try again");
            }

            @Override
            public void onProgress(int progress) {
                progressDialog.setProgress(progress);
            }
        }).start();
    }


}