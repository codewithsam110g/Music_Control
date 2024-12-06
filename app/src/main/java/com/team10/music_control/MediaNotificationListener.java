package com.team10.music_control;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class MediaNotificationListener extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // No action needed unless you want to handle specific notifications
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        // Listener connected successfully
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // No action needed unless you want to handle when notifications are removed
    }
}
