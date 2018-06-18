package com.example.green.imageserviceapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class ImageService extends Service {
    BroadcastReceiver wifiReceiver;
    IntentFilter filter;
    List<File> AndroidImages;

    public ImageService() { }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        filter = new IntentFilter();
        filter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        wifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null) {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        //get the different network states
                        //
                        if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                            startTransfer();

                        }
                    }
                }
            }
        };
        // Here put the Code of Service
        this.registerReceiver(this.wifiReceiver, filter);
    }

    public int onStartCommand(Intent intent, int flag, int startId) {
        Toast.makeText(this,"Service starting...", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    public void onDestroy() {
        Toast.makeText(this,"Service ending...", Toast.LENGTH_SHORT).show();
    }
     public void startTransfer() {
         try {
             TcpClient tcpClient = new TcpClient(8500, "10.0.2.2");
             File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
             if (dcim == null) {
                 return;
             }
             File[] pics = dcim.listFiles();
             int count = 0;
             if (pics != null) {
                 for (File pic : pics) {
                     //sends the message to the server
                     tcpClient.sendFile(pic);
                 }
             }
         } catch (Exception e) {
             Log.e("Tcp","C: Error",e);
         }

     }


}


