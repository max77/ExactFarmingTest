package com.max77.exactfarmingtest.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.RingtoneManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.max77.exactfarmingtest.BuildConfig;
import com.max77.exactfarmingtest.R;
import com.max77.exactfarmingtest.location.ILocationFilter;
import com.max77.exactfarmingtest.location.ILocationTracker;
import com.max77.exactfarmingtest.location.LocationInfo;
import com.max77.exactfarmingtest.location.LocationPath;
import com.max77.exactfarmingtest.tracker.AreaTracker;
import com.max77.exactfarmingtest.tracker.IAreaTracker;
import com.max77.exactfarmingtest.ui.MapsActivity;

public class AreaTrackerService extends Service {
    public class LocalBinder extends Binder {
        public AreaTrackerService getService() {
            return AreaTrackerService.this;
        }
    }

    private static final String TAG = BuildConfig.TAG + AreaTrackerService.class.getSimpleName();
    private static final String CHANNEL_ID = "channel_01";
    private static final int NOTIFICATION_ID = 12345678;

    private final IBinder mBinder = new LocalBinder();
    private boolean isChangingConfiguration = false;
    private boolean isShuttingDown = false;
    private NotificationManager mNotificationManager;

    private AreaTracker mAreaTracker;
    private AreaTracker.StateListener mBackgroundAreaTrackerStateListener;

    public AreaTrackerService() {
    }

    @Override
    public void onCreate() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "tracker service started");
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        isChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind()");
        startService(new Intent(this, AreaTrackerService.class));
        leaveForeground();
        isChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, "onRebind()");
        leaveForeground();
        isChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind()");

        if (!isChangingConfiguration && !isShuttingDown) {
            goForeground();
        }

        return true; // ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void onDestroy() {
        if (mAreaTracker != null) {
            mAreaTracker.destroy();
        }

        Log.i(TAG, "onDestroy()");
    }

    public void setup(ILocationTracker locationTracker, ILocationFilter locationFilter,
                      long locationTimeout, double pathClosureThreshold, boolean reset) {
        if (reset || mAreaTracker == null) {
            mAreaTracker = new AreaTracker(locationTracker, locationFilter, locationTimeout, pathClosureThreshold);
        }
    }

    public void shutdown() {
        isShuttingDown = true;
        stopForeground(true);
        stopSelf();
    }

    public AreaTracker getAreaTracker() {
        return mAreaTracker;
    }

    private Notification getNotification(String text, boolean sound) {
        Intent mainIntent = new Intent(this, MapsActivity.class);

        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentIntent(activityPendingIntent)
                .setContentText(text)
                .setContentTitle(getString(R.string.app_name))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis());

        if (sound) {
            builder.setVibrate(new long[]{100, 200, 100, 600})
                    .setSound(RingtoneManager.getActualDefaultRingtoneUri(this,
                            RingtoneManager.TYPE_NOTIFICATION));
        }

        return builder.build();
    }

    private void goForeground() {
        Log.i(TAG, "goForeground()");

        startForeground(NOTIFICATION_ID, getNotification("", false));

        mBackgroundAreaTrackerStateListener = new BackgroundAreaTrackerAreaStateListener();
        mAreaTracker.addStateListener(mBackgroundAreaTrackerStateListener);
        mAreaTracker.forceStateListenerForCurrentState();
    }

    private void leaveForeground() {
        Log.i(TAG, "leaveForeground()");

        if (mAreaTracker != null) {
            mAreaTracker.removeStateListener(mBackgroundAreaTrackerStateListener);
        }

        stopForeground(true);
    }

    public boolean serviceIsRunningInForeground() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

    private class BackgroundAreaTrackerAreaStateListener implements IAreaTracker.StateListener {
        @Override
        public void isInitialized() {
            showNotification(getString(R.string.status_tracker_initialized), false);
        }

        @Override
        public void isFixingInitialLocation(LocationInfo currentLocation, long startTime, long timeout) {
            showNotification(currentLocation != null ?
                    getString(R.string.status_fixing_location, currentLocation.getAccuracy()) :
                    getString(R.string.status_fixing_location_no_location), false);
        }

        @Override
        public void isLocationFixed(LocationInfo currentLocation) {
            showNotification(getString(R.string.status_location_fixed), true);
        }

        @Override
        public void isTracking(LocationPath currentPath) {
            showNotification(getString(R.string.status_tracking, currentPath.length()), false);
        }

        @Override
        public void isFinished(LocationPath currentPath) {
            showNotification(getString(R.string.status_tracking_finished, currentPath.area()), true);
        }

        @Override
        public void isLocationNotAvailable() {
            showNotification(getString(R.string.status_location_not_available), true);
        }

        @Override
        public void isLocationTimedOut(LocationInfo lastLocation) {
            showNotification(getString(R.string.status_location_timed_out), true);
        }

        private void showNotification(String text, boolean playSound) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification(text, playSound));
        }
    }
}
