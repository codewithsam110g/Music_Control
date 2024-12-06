package com.team10.music_control;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

public class BluetoothService extends Service {

    private static final String CHANNEL_ID = "BluetoothServiceChannel";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private Boolean isPlaying = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getStringExtra("MAC_ADDRESS") == null) {
            Toast.makeText(this, "No MAC address provided. Stopping service.", Toast.LENGTH_SHORT).show();
            stopSelf(); // Stop the service if there's no MAC address
            return START_NOT_STICKY;
        }

        String macAddress = intent.getStringExtra("MAC_ADDRESS");
        connectToBluetoothDevice(macAddress);
        listenForCommands();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Bluetooth Media Controller")
                .setContentText("Listening for Bluetooth commands...")
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's icon
                .build();

        startForeground(1, notification);
        return START_STICKY;
    }


    @SuppressLint("MissingPermission")
    private void connectToBluetoothDevice(String macAddress) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);

        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
            bluetoothSocket.connect();
            inputStream = bluetoothSocket.getInputStream();
            Toast.makeText(this, "Connected to HC-05", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
            stopSelf();
        }
    }

    private void listenForCommands() {
        new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                int bytes;

                while (true) {
                    bytes = inputStream.read(buffer);
                    String command = new String(buffer, 0, bytes).trim();

                    processCommand(command);
                }
            } catch (IOException e) {
                e.printStackTrace();
                stopSelf();
            }
        }).start();
    }

    private void processCommand(String command) {
        MediaSessionManager mediaSessionManager = (MediaSessionManager) getSystemService(MEDIA_SESSION_SERVICE);
        ComponentName notificationListener = new ComponentName(this, MediaNotificationListener.class);

        // Ensure access to active media sessions
        List<MediaController> controllers = mediaSessionManager.getActiveSessions(notificationListener);
        if (controllers.isEmpty()) return;

        MediaController mediaController = controllers.get(0);
        MediaController.TransportControls controls = mediaController.getTransportControls();

        new android.os.Handler(getMainLooper()).post(() ->
                Toast.makeText(this, "Command received: " + command, Toast.LENGTH_SHORT).show()
        );

        // Execute command actions on the main thread
        new android.os.Handler(getMainLooper()).post(() -> {
            switch (command) {
                case "1":
                    if (isPlaying) {
                        controls.pause();
                    } else {
                        controls.play();
                    }
                    isPlaying = !isPlaying;
                    break;
                case "4":
                    controls.skipToNext();
                    break;
                case "5":
                    controls.skipToPrevious();
                    break;
                case "2":
                    adjustVolume(true);
                    break;
                case "3":
                    adjustVolume(false);
                    break;
                default:
                    Toast.makeText(this, "Unknown command: " + command, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void adjustVolume(boolean increase) {

        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager != null) {
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            int newVolume = increase ? Math.min(currentVolume + 1, maxVolume) : Math.max(currentVolume - 1, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, AudioManager.FLAG_SHOW_UI);
            Toast.makeText(this, "Volume set to: " + newVolume, Toast.LENGTH_SHORT).show();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Bluetooth Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
