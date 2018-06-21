package com.example.green.imageserviceapp;

import android.app.NotificationChannel;
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
    //members
    private BroadcastReceiver wifiReceiver;
    private IntentFilter filter;
    private List<File> androidImages;
    private TcpClient client;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * onCreate().
     * create the wifi broadcast listener and register it.
     */
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
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onReceive(Context context, Intent intent) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null) {
                    if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
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
        this.unregisterReceiver(this.wifiReceiver);
        Toast.makeText(this,"Service ending...", Toast.LENGTH_SHORT).show();
        Log.d("Service: ", "M: Disconnected");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startTransfer(Context context) {
        updateImageList();
        final int notify_id = 1;
        final NotificationCompat.Builder builder = new NotificationCompat.
                Builder(context, "default");
        final NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("default", "Progress bar", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Progress bar for image transfer");
        notificationManager.createNotificationChannel(channel);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setContentTitle("Transfer images");
        builder.setContentText("Transfer in progress...");
        try {
            this.client = new TcpClient(7999, "10.0.2.2");
        } catch (Exception e) {
            Log.e("Tcp", "C: Error", e);
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int count = 0;
                    int length = androidImages.size();
                    for (File pic : androidImages) {
                        //sends the message to the server
                        client.sendFile(pic);
                        Log.d("Tcp Client:", "sent file:" + pic.getName());
                        //updating the progress bar.
                        count = count + 100 / length;
                        builder.setProgress(100, count, false);
                        notificationManager.notify(notify_id, builder.build());
                    }
                    builder.setProgress(0, 0, false);
                    builder.setContentText("Transfer is complete!");
                    notificationManager.notify(notify_id, builder.build());
                } catch (Exception e) {
                    Log.e("Tcp", "C: Error", e);
                }
            }
        }).start();

     }
    public void getImage(File directory, List<File> picsFilesList) {
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                getImage(file, picsFilesList);
            } else if(isImage(file)) {
                picsFilesList.add(file);
            }
        }
    }
    public Boolean isImage(File f){
        return (f.getName().contains(".jpg") || f.getName().contains(".gif") ||
                f.getName().contains(".bmp") ||  f.getName().contains(".png"));
    }
    public void updateImageList() {
        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File[] fileOrDir = dcim.listFiles();
        List<File> picsFilesList = new ArrayList<>();
        for (File file : fileOrDir) {
            //check if dir
            if (file.isDirectory()) {
                getImage(file, picsFilesList);
            } else if(isImage(file)) { //check if file
                picsFilesList.add(file);
            }
        }
        androidImages = picsFilesList;
    }
}


