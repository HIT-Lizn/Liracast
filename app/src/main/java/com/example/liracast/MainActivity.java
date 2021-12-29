package com.example.liracast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.liracast.global.ResourceManager;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private final int REQUEST_CODE_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        ResourceManager.getInstance().setContext(getApplicationContext());

        Button sourceButton = findViewById(R.id.main_button_source);
        sourceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SourceActivity.class);
                startActivity(intent);
            }
        });

        Button sinkButton = findViewById(R.id.main_button_sink);
        sinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SinkActivity.class);
                startActivity(intent);
            }
        });

        requestPermission();
    }

    public void requestPermission() {
        Log.d(TAG, "requestPermission");
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "permission: " + permissions.toString());
    }
}