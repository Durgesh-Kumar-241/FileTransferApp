package com.dktechhub.shareit.filetransferapp.ui.main;

public interface TransferStateInterface {
        void onConnectionSuccess(String remote, String device);
        void onConnectionFailed();
        void onTrasferComplete();
}
