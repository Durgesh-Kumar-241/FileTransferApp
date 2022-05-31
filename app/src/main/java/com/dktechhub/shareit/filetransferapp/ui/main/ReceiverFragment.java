package com.dktechhub.shareit.filetransferapp.ui.main;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dktechhub.shareit.filetransferapp.MWebHotspot;
import com.dktechhub.shareit.filetransferapp.R;

public class ReceiverFragment extends Fragment {

    public static ReceiverFragment newInstance(TransferStateInterface transferStateInterface) {
        return new ReceiverFragment(transferStateInterface);
    }
    TransferStateInterface transferStateInterface;
    public ReceiverFragment(TransferStateInterface transferStateInterface)
    {
        this.transferStateInterface=transferStateInterface;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root =inflater.inflate(R.layout.fragment_main, container, false);
        TextView tv =root.findViewById(R.id.ip_local);
        tv.setText(MWebHotspot.getInstance(getContext()).getLocalIP());
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

}