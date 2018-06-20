package com.example.green.imageserviceapp;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
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

    }

    public int onStartCommand(Intent intent, int flag, int startId) {
        Toast.makeText(this,"Service starting...", Toast.LENGTH_SHORT).show();
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
                            startTransfer(context);
                        }
                    }
                }
            }
        };
        this.registerReceiver(this.wifiReceiver, filter);
        return START_STICKY;
    }

    public void onDestroy() {
        Toast.makeText(this,"Service ending...", Toast.LENGTH_SHORT).show();
    }
    public void startTransfer(Context context) {
        updateImageList();
        final int notify_id = 1;
        final NotificationCompat.Builder builder = new NotificationCompat.
                Builder(context, "default");
        final NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setContentTitle("Transfer images");
        builder.setContentText("Transfer in progress...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TcpClient tcpClient = new TcpClient(8500, "10.0.2.2");
                    int count = 0;
                    int length = AndroidImages.size();
                    if (AndroidImages != null) {
                        for (File pic : AndroidImages) {
                            //sends the message to the server
                            tcpClient.sendFile(pic);
                            Log.d("Tcp Client:", "sent file:" + pic.getName());
                            //updating the progress bar.
                            count = count + 100 / length;
                            builder.setProgress(100, count, false);
                            notificationManager.notify(notify_id, builder.build());

                        }
                        builder.setProgress(0, 0, false);
                        builder.setContentText("Transfer is complete!");
                        notificationManager.notify(notify_id, builder.build());
                    }
                } catch (Exception e) {
                    Log.e("Tcp", "C: Error", e);
                }
            }
        }).start();

     }
    public void getImage(File directory, List<File> picsFilesList) {
        File[] files = directory.listFiles();
        int len = files.length;
        for (File file : files) {
            if (file.isDirectory()) {
                getImage(file, picsFilesList);
            } else if(file.toString().contains(".jpg")) {
                picsFilesList.add(file);
            }
        }
    }
    public void updateImageList() {
        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File[] fileOrDir = dcim.listFiles();
        List<File> picsFilesList = new ArrayList<>();
        int len =fileOrDir.length;
        if (fileOrDir != null) {
            for (File file : fileOrDir) {
                //check if dir
                if (file.isDirectory()) {
                    getImage(file, picsFilesList);
                } else if(file.toString().contains(".jpg")) { //check if file
                    picsFilesList.add(file);
                }
            }
        }
        AndroidImages = picsFilesList;
    }
}


