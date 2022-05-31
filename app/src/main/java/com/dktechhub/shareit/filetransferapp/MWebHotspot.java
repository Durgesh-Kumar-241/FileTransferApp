package com.dktechhub.shareit.filetransferapp;


import android.content.Context;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MWebHotspot {
    WifiManager wifiManager;
    private static MWebHotspot instance;
    public static MWebHotspot getInstance(Context context)
    {
        if(instance==null)
            instance=new MWebHotspot(context);
        return instance;
    }

    public MWebHotspot(@NotNull Context context) {
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }


    public String getLocalIP() {

        if (wifiManager.isWifiEnabled()) {
            if (wifiManager.getConnectionInfo().getSupplicantState() == SupplicantState.COMPLETED) {
                return (getIpfromInt(wifiManager.getDhcpInfo().ipAddress));
            }

        } else if (isApOn()) {
            try {
                Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                        .getNetworkInterfaces();
                while (enumNetworkInterfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = enumNetworkInterfaces
                            .nextElement();
                    Enumeration<InetAddress> enumInetAddress = networkInterface
                            .getInetAddresses();
                    while (enumInetAddress.hasMoreElements()) {
                        InetAddress inetAddress = enumInetAddress.nextElement();

                        if (inetAddress.isSiteLocalAddress()) {
                            if (inetAddress.getHostAddress().contains("192.168")) {
                                return (inetAddress.getHostAddress());
                            }
                        }
                    }
                }

            } catch (SocketException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

            }


        }

        return "";

    }

    public boolean isApOn() {

        try {
            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);
        } catch (Throwable ignored) {
        }
        return false;

    }

    public String getIpfromInt(int ipA) {
        String ip = String.format("%d.%d.%d.%d", (ipA & 0xff), (ipA >> 8 & 0xff), (ipA >> 16 & 0xff), (ipA >> 24 & 0xff));
        return ip;
    }








}

