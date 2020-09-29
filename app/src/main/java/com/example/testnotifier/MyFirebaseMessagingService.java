package com.example.testnotifier;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingService";

    @Override
    public void onNewToken(@NonNull String refreshedToken) {
        super.onNewToken(refreshedToken);
        if (!refreshedToken.isEmpty()) {
            Log.d(TAG, "Refreshed token: " + refreshedToken);
            sendDataToActivity(refreshedToken);

            // If you want to send messages to this application instance or
            // manage this apps subscriptions on the server side, send the
            // Instance ID token to your app server.
            sendRegistrationToServer(refreshedToken);
        }
    }

    private void sendDataToActivity(String token)
    {
        Intent sendToken = new Intent();
        sendToken.setAction("NEW_FCM_TOKEN");
        sendToken.putExtra( "TOKEN",token);
        sendBroadcast(sendToken);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        String body = "", body2 = "", body3 = "", title = "", link = "", from = "";
        boolean sound = false, vibrate = false;

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Object obj = remoteMessage.getData().get("body");
            body = obj != null ? obj.toString() : "";
            obj = remoteMessage.getData().get("body2");
            body2 = obj != null ? obj.toString() : "";
            obj = remoteMessage.getData().get("body3");
            body3 = obj != null ? obj.toString() : "";
            obj = remoteMessage.getData().get("title");
            title = obj != null ? obj.toString() : "";
            obj = remoteMessage.getData().get("sound");
            sound = !(obj != null && obj.toString().equals("0"));
            obj = remoteMessage.getData().get("vibrate");
            vibrate = !(obj != null && obj.toString().equals("0"));
            obj = remoteMessage.getData().get("link");
            link = obj != null ? obj.toString() : "";
            from = Objects.requireNonNull(remoteMessage.getFrom()).replace("/topics/", "");
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            body = remoteMessage.getNotification().getBody();
            body2 = "";
            body3 = "";
            title = remoteMessage.getNotification().getTitle();
            title = (title != null) ? title : "";
            String snd = remoteMessage.getNotification().getSound();
            sound = !(snd != null && snd.toString().equals("0"));
            vibrate = false;
            Uri lnk = remoteMessage.getNotification().getLink();
            link = lnk != null ? lnk.toString() : "";
            from = Objects.requireNonNull(remoteMessage.getFrom()).replace("/topics/", "");
        }

        if (!(body != null && body.isEmpty())) {
            Log.d(TAG, "Message data payload:"
                    + "\nfrom: " + from
                    + "\nbody: " + body
                    + "\nbody2: " + body2
                    + "\nbody3: " + body3
                    + "\ntitle: " + title
                    + "\nsound: " + (sound ? "1" : "0")
                    + "\nvibrate: " + (vibrate ? "1" : "0")
                    + "\nlink: " + link
            );

            sendNotification(body, body2, body3, title, sound, vibrate, link, from);

            Intent local = new Intent("testnotifier.message.new");
            local.putExtra("from", from);
            LocalBroadcastManager.getInstance(this).sendBroadcast(local);
        }
    }

    private void sendNotification(String body, String body2, String body3, String title, boolean sound, boolean vibrate, String link, String from) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (!link.isEmpty()) {
            intent.setData(Uri.parse(link));
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);

        boolean useInbox = !body2.isEmpty();

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentIntent(pendingIntent);

        if (useInbox) {
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.addLine(body);
            inboxStyle.addLine(body2);
            if (!body3.isEmpty()) {
                inboxStyle.addLine(body3);
            }
            ;
            notificationBuilder.setStyle(inboxStyle);
            notificationBuilder.setContentTitle(title).setSubText(from);
        } else {
            notificationBuilder.setContentTitle(title).setContentText(body).setSubText(from);
        }

        if (sound) {
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            notificationBuilder.setSound(defaultSoundUri);
        }

        if (vibrate) {
            long[] pattern = {500, 500, 500, 500, 500};
            notificationBuilder.setVibrate(pattern);
        }

        notificationBuilder.setLights(R.color.colorPrimary & 0xff, 500, 400);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        NotificationChannel channel = new NotificationChannel(channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT);
        Objects.requireNonNull(notificationManager).createNotificationChannel(channel);

        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher_round);

        Random r = new Random();
        int rand = r.nextInt(10000 - 1) + 1;
        notificationManager.notify(rand /* ID of notification */, notificationBuilder.build());
    }

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }
}
