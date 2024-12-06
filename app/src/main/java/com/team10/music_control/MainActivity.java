package com.team10.music_control;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.MEDIA_CONTENT_CONTROL)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.MEDIA_CONTENT_CONTROL},
                    2);
        }

        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        startActivity(intent);







        Button connectButton = findViewById(R.id.btn_connect);
        connectButton.setOnClickListener(v -> {
            // Start the Bluetooth service with the MAC address of your HC-05 Bluetooth module
            Intent serviceIntent = new Intent(MainActivity.this, BluetoothService.class);
            serviceIntent.putExtra("MAC_ADDRESS", "00:20:12:08:BB:8C"); // Replace with your HC-05 MAC address
            startForegroundService(serviceIntent);
        });

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
            Toast.makeText(this, "Volume increased", Toast.LENGTH_SHORT).show();
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
        } else {
            Toast.makeText(this, "AudioManager is null", Toast.LENGTH_SHORT).show();
        }

    }
}
