package com.dktechhub.shareit.filetransferapp.ui.main;

public interface TransferStateInterface {
        void onConnectionSuccess(String remote);
        void onConnectionFailed();
        void onTrasferComplete();
}
