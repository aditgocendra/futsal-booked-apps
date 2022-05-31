package com.ark.futsalbookedapps.Notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import com.ark.futsalbookedapps.Globals.Data;
import com.ark.futsalbookedapps.Globals.ReferenceDatabase;
import com.ark.futsalbookedapps.R;
import com.ark.futsalbookedapps.Views.Users.Home;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class ServiceNotification extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        showNotification(message);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        ReferenceDatabase.referenceTokenNotification.child(Data.uid).child("token").setValue(token);

    }

    private void showNotification(RemoteMessage remoteMessage) {
        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");


        final String CHANNEL_ID = "NOTIFICATION";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notification new order",
                    NotificationManager.IMPORTANCE_HIGH
            );

            Intent intent = new Intent(this, Home.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // pending intent
            PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            Notification.Builder notifBuilder = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.logo_apps)
                    .setContentIntent(notifyPendingIntent);

            NotificationManagerCompat.from(this).notify(1, notifBuilder.build());
        }
    }
}
