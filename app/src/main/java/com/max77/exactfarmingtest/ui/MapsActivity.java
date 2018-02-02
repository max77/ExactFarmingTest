package com.max77.exactfarmingtest.ui;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.max77.exactfarmingtest.R;
import com.max77.exactfarmingtest.location.AccuracyAndDisplacementBasedLocationFilter;
import com.max77.exactfarmingtest.location.FusedLocationTracker;
import com.max77.exactfarmingtest.location.LocationInfo;
import com.max77.exactfarmingtest.location.LocationPath;
import com.max77.exactfarmingtest.service.AreaTrackerService;
import com.max77.exactfarmingtest.tracker.AreaTracker;
import com.max77.exactfarmingtest.tracker.IAreaTracker;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final double TRACKER_REQUIRED_ACCURACY = 30;
    private static final double TRACKER_MIN_DISPLACEMENT = 25;
    private static final long GPS_SAMPLING_PERIOD = 1000;
    private static final long LOCATION_TIMEOUT = 30000;

    private AreaTrackerService mAreaTrackerService;
    private AreaTracker.StateListener mForegroundAreaTrackerStateListener;

    private MapHelper mMapHelper;
    private TextView tvStatus;
    private TextView tvAccuracy;
    private Button btnButton;

    private boolean isBound;
    private boolean isFinishedByUser;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AreaTrackerService.LocalBinder binder = (AreaTrackerService.LocalBinder) service;
            mAreaTrackerService = binder.getService();
            setupAreaTrackerService(false);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAreaTrackerService = null;
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        tvStatus = findViewById(R.id.status);
        tvAccuracy = findViewById(R.id.accuracy);
        btnButton = findViewById(R.id.button);

        showStatus(getString(R.string.status_starting));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMapHelper = new MapHelper(this, googleMap, TRACKER_REQUIRED_ACCURACY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        isFinishedByUser = false;
        checkPermissionsAndBindService();
    }

    @Override
    protected void onStop() {
        if (isBound) {
            mAreaTrackerService.getAreaTracker().removeStateListener(mForegroundAreaTrackerStateListener);

            if (isFinishedByUser) {
                mAreaTrackerService.shutdown();
            } else {
                unbindService(mServiceConnection);
            }

            isBound = false;
        }

        super.onStop();
    }

    @Override
    public void onBackPressed() {
        isFinishedByUser = true;
        super.onBackPressed();
    }

    private void setupAreaTrackerService(boolean reset) {
        if (isBound) {
            mAreaTrackerService.getAreaTracker().removeStateListener(null);
        }

        mAreaTrackerService.setup(new FusedLocationTracker(getApplicationContext(),
                        GPS_SAMPLING_PERIOD),
                new AccuracyAndDisplacementBasedLocationFilter(TRACKER_REQUIRED_ACCURACY,
                        TRACKER_MIN_DISPLACEMENT),
                LOCATION_TIMEOUT,
                TRACKER_MIN_DISPLACEMENT * 2,
                reset);

        mForegroundAreaTrackerStateListener = new ForegroundAreaTrackerAreaStateListener();
        mAreaTrackerService.getAreaTracker().addStateListener(mForegroundAreaTrackerStateListener);
        mAreaTrackerService.getAreaTracker().forceStateListenerForCurrentState();
    }

    private void checkPermissionsAndBindService() {
        PermissionHandlerFragment
                .attach(getSupportFragmentManager())
                .requestPermissions((permissions, grantResult, numGranted) -> {
                            if (numGranted >= 1) {
                                bindService(new Intent(this, AreaTrackerService.class),
                                        mServiceConnection, Context.BIND_AUTO_CREATE);
                            }
                        },
                        false,
                        Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void showStatus(String status) {
        tvStatus.setText(status);
    }

    private void showAccuracy(double accuracy) {
        if (accuracy < 0) {
            tvAccuracy.setText("");
        } else {
            tvAccuracy.setText(getString(R.string.accuracy, accuracy));
            tvAccuracy.setTextColor(UIUtil.getColorForAccuracy(accuracy, TRACKER_REQUIRED_ACCURACY,
                    255, 0.5f));
        }
    }

    private void showButton(String text, int color, View.OnClickListener listener) {
        btnButton.setText(text);
        btnButton.setBackgroundColor(color);
        btnButton.setOnClickListener(listener);
    }

    private class ForegroundAreaTrackerAreaStateListener implements IAreaTracker.StateListener {
        boolean isFirstLocation = true;

        @Override
        public void isInitialized() {
            mMapHelper.clear();

            showAccuracy(-1);
            showStatus(getString(R.string.status_tracker_initialized));
            showButton(getString(R.string.start), getResources().getColor(R.color.color_button_start),
                    v -> mAreaTrackerService.getAreaTracker().startInitialLocationFix());
        }

        @Override
        public void isFixingInitialLocation(LocationInfo currentLocation, long startTime, long timeout) {
            if (currentLocation == null) {
                showStatus(getString(R.string.status_fixing_location_no_location));
            } else {
                mMapHelper.showMyLocation(currentLocation);
                showAccuracy(currentLocation.getAccuracy());

                if (isFirstLocation) {
                    isFirstLocation = false;

                    mMapHelper.centerOnLocation(currentLocation, currentLocation.getAccuracy());
                    showStatus(getString(R.string.status_fixing_location, currentLocation.getAccuracy()));
                }
            }

            showButton(getString(R.string.stop), getResources().getColor(R.color.color_button_stop),
                    v -> setupAreaTrackerService(true));
        }

        @Override
        public void isLocationFixed(LocationInfo currentLocation) {
            mMapHelper.centerOnLocation(currentLocation, currentLocation.getAccuracy());
            mMapHelper.showMyLocation(currentLocation);
            showAccuracy(currentLocation.getAccuracy());
            showStatus(getString(R.string.status_location_fixed));
            showButton(getString(R.string.track), getResources().getColor(R.color.color_button_track),
                    v -> mAreaTrackerService.getAreaTracker().startTracking());
        }

        @Override
        public void isTracking(LocationPath currentPath) {
            if (currentPath.size() > 0) {
                showPath(currentPath);
            }

            showStatus(getString(R.string.status_tracking, currentPath.length()));
            showButton(getString(R.string.finish), getResources().getColor(R.color.color_button_finish),
                    v -> mAreaTrackerService.getAreaTracker().finishTracking());
        }

        private void showPath(LocationPath currentPath) {
            List<LocationInfo> path = currentPath.getAllPoints();
            mMapHelper.drawPath(path, getResources().getColor(R.color.color_path));

            LocationInfo end = path.get(path.size() - 1);
            showAccuracy(end.getAccuracy());
            mMapHelper.showMyLocation(end);

            if (currentPath.size() > 1)
                mMapHelper.centerOnPoints(currentPath.getAllPoints());
        }

        @Override
        public void isFinished(LocationPath currentPath) {
            if (currentPath.size() > 0) {
                showPath(currentPath);

                if (currentPath.isClosed())
                    mMapHelper.drawArea(currentPath.getAllPoints(),
                            getResources().getColor(R.color.color_area));
            }

            showStatus(getString(R.string.status_tracking_finished, currentPath.area()));
            showButton(getString(R.string.restart), getResources().getColor(R.color.color_button_start),
                    v -> setupAreaTrackerService(true));
        }

        @Override
        public void isLocationNotAvailable() {
            showAccuracy(-1);
            showStatus(getString(R.string.status_location_not_available));
            showButton(getString(R.string.restart), getResources().getColor(R.color.color_button_start),
                    v -> setupAreaTrackerService(true));
        }

        @Override
        public void isLocationTimedOut(LocationInfo lastLocation) {
            showAccuracy(-1);
            showStatus(getString(R.string.status_location_timed_out));
            showButton(getString(R.string.restart), getResources().getColor(R.color.color_button_start),
                    v -> setupAreaTrackerService(true));
        }
    }
}
