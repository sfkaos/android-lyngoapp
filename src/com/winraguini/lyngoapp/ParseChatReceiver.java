package com.winraguini.lyngoapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseChatReceiver extends BroadcastReceiver {
    private static final String TAG = "ParseChatReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            String channel = intent.getExtras().getString("com.parse.Channel");
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));

            String chatParticipantID = json.getString("chatParticipantID");
            String to = json.getString("to");
            String message = json.getString("message");

            if (to.equals(ParseUser.getCurrentUser().getObjectId())) {

                String longText = "User wants to chat with you";

                // http://stackoverflow.com/questions/16045722/notification-not-showing
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(context)
                                .setContentTitle(longText)
                                .setContentText(message).setSmallIcon(R.drawable.ic_launcher);

                // Sets an ID for the notification
                int mNotificationId = 1;
                // Gets an instance of the NotificationManager service
                NotificationManager mNotifyMgr =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                // Builds the notification and issues it.
                mNotifyMgr.notify(mNotificationId, mBuilder.build());
                Log.d(TAG, "notification should have been received");

            }
            Log.d(TAG, "got action " + action + " on channel " + channel + " with:" + json.toString());

        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }
    }
}