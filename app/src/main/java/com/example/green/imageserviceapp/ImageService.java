package com.example.green.imageserviceapp;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import static android.app.Activity.RESULT_OK;

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

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default").
                setVibrate(new long[]{0, 100, 100, 100, 100, 100})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Content Title")
                .setContentText("Content Text");

        final int notify_id = 1;
        builder.setContentTitle("Transfer images");
        builder.setContentText("Transfer in progress...");

         try {
             TcpClient tcpClient = new TcpClient(8500, "10.0.2.2");
             File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
             if (dcim == null) {
                 return;
             }
             File[] pics = dcim.listFiles();
             int count = 1;
             if (pics != null) {
                 for (File pic : pics) {
                     //sends the message to the server
                     tcpClient.sendFile(pic);
                     try {
                         //updating the progress bar.
                         builder.setProgress(pics.length, count, false);
                         notificationManager.notify(notify_id, builder.build());
                         builder.setContentText(String.valueOf(count) + "%");
                         count++;
                     } catch (NullPointerException ne) {
                         Log.e("Notify", "C: Error", ne);
                     }
                 }
                 builder.setProgress(0, 0, false);
                 builder.setContentText("Transfer is complete!");
                 notificationManager.notify(notify_id, builder.build());
             }
         } catch (Exception e) {
             Log.e("Tcp", "C: Error", e);

         }
     }
}


