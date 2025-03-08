package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import android.app.PendingIntent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.Calendar;
import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GeofenceBroadcastReceiver";

    @SuppressLint("LongLogTag")
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Error receiving geofence event: " + geofencingEvent.getErrorCode());
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

        // Logging Geofence IDs
        for (Geofence geofence : triggeringGeofences) {
            Log.d(TAG, "Triggered Geofence ID: " + geofence.getRequestId());
        }

        // Handle geofence transitions
        switch (geofenceTransition) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Log.d(TAG, "GEOFENCE_TRANSITION_ENTER");
                Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Entering JKLU campus", "Welcome to JKLU!", MapsActivity.class);

                if (isWithinTimeWindow()) {
                    // Launch AttendanceMarkingActivity using PendingIntent
                    Intent attendanceIntent = new Intent(context, AttendanceMarkingActivity.class);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntentWithParentStack(attendanceIntent);
                    PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                    try {
                        pendingIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        Log.e(TAG, "PendingIntent failed: " + e.getMessage());
                    }
                } else {
                    Toast.makeText(context, "Attendance can only be marked between 10:00 PM and 10:30 PM.", Toast.LENGTH_LONG).show();
                }
                break;

            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Log.d(TAG, "GEOFENCE_TRANSITION_DWELL");
                Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Inside JKLU campus", "You are inside the geofence.", MapsActivity.class);
                break;

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Log.d(TAG, "GEOFENCE_TRANSITION_EXIT");
                Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Exiting JKLU campus", "Goodbye!", MapsActivity.class);
                break;

            default:
                Log.e(TAG, "Unknown geofence transition: " + geofenceTransition);
        }
    }

    // Check if the current time is between 10:00 PM and 10:30 PM
    private boolean isWithinTimeWindow() {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        return hour == 22 && minute >= 0 && minute <= 30;
    }
}