package com.dktechhub.shareit.filetransferapp.ui.main;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dktechhub.shareit.filetransferapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TransferCompleteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransferCompleteFragment extends Fragment {


    public TransferCompleteFragment() {
        // Required empty public constructor
    }
    // TODO: Rename and change types and number of parameters
    public static TransferCompleteFragment newInstance() {
        TransferCompleteFragment fragment = new TransferCompleteFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transfer_complete, container, false);
    }
}