package com.example.green.imageserviceapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ImageService extends Service {
    public ImageService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
