package com.example.green.imageserviceapp;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progBar;
    private TextView progBarText;
    private int progrees = 0;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void startService(View view){
        Intent intent = new Intent(this, ImageService.class);
        startService(intent);
    }
    public void stopService(View view) {
        Intent intent = new Intent(this, ImageService.class);
        stopService(intent);
    }
}
