package com.max77.exactfarmingtest.ui;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.max77.exactfarmingtest.R;
import com.max77.exactfarmingtest.location.FusedLocationTracker;
import com.max77.exactfarmingtest.location.ILocationTracker;
import com.max77.exactfarmingtest.location.LocationInfo;
import com.max77.exactfarmingtest.tracker.IAreaTrackerService;
import com.max77.exactfarmingtest.tracker.TrivialAreaTrackerServiceStateListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final double TRACKER_REQUIRED_ACCURACY = 30;
    private static final double TRACKER_MIN_DISPLACEMENT = 5;
    private static final long GPS_SAMPLING_PERIOD = 0;

    private IAreaTrackerService mAreaTrackerService;
    private ILocationTracker mMyLocationTracker;

    private GoogleMap mMap;
    private MapHelper mMapHelper;

    private boolean isBound;

    private TextView tvStatus;

    private IAreaTrackerService.StateListener mTrackerStateListener = new TrivialAreaTrackerServiceStateListener() {
        @Override
        public void isInitialized() {
            showStatus(getString(R.string.status_tracker_initialized));

        }
    };

//    private final ServiceConnection mServiceConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            AreaTrackerService.LocalBinder binder = (AreaTrackerService.LocalBinder) service;
//            initAreaTrackerService(binder.getService());
//            isBound = true;
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            mAreaTrackerService = null;
//            isBound = false;
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        tvStatus = findViewById(R.id.status);

        showStatus(getString(R.string.status_starting));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMapHelper = new MapHelper(this, mMap);
        mMapHelper.setAccuracyRange(30, 80);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPermissionsAndSetup();
    }

    @Override
    protected void onStop() {
        stopMyPositionTracker();

//        if (isBound) {
//            unbindService(mServiceConnection);
//            isBound = false;
//        }

        super.onStop();
    }

    private void checkPermissionsAndSetup() {
        PermissionHandlerFragment
                .attach(getSupportFragmentManager())
                .requestPermissions((permissions, grantResult, numGranted) -> {
                            if (numGranted >= 1) {
//                                bindService(new Intent(this, AreaTrackerService.class),
//                                        mServiceConnection, Context.BIND_AUTO_CREATE);
                                startMyLocationTracker();
                            }
                        },
                        false,
                        Manifest.permission.ACCESS_FINE_LOCATION);
    }


    private void startMyLocationTracker() {
        stopMyPositionTracker();

        mMyLocationTracker = new FusedLocationTracker(this, 0);
        mMyLocationTracker.setListener(new ILocationTracker.Listener() {
            @Override
            public void onNewLocation(LocationInfo location) {
                handleNewLocation(location);
            }

            @Override
            public void onLocationNotAvailable() {

            }
        });

        mMyLocationTracker.startTracking();
    }

    private void handleNewLocation(LocationInfo newLocation) {
        if (newLocation == null)
            return;

        if (mMapHelper != null) {
            mMapHelper.setMyLocation(newLocation);
            mMapHelper.zoomOnMyLocationIfNeeded();
        }

        tvStatus.setText("R = " + newLocation.getAccuracy());
    }

    private void stopMyPositionTracker() {
        if (mMyLocationTracker != null) {
            mMyLocationTracker.destroy();
            mMyLocationTracker = null;
        }
    }

//    private void initAreaTrackerService(IAreaTrackerService service) {
//        mAreaTrackerService = service;
//        mAreaTrackerService.forceStateListenerForCurrentState(new TrivialAreaTrackerServiceStateListener() {
//            @Override
//            public void isCreated() {
//                mAreaTrackerService.initialize(
//                        new FusedLocationTracker(getApplicationContext(),
//                                GPS_SAMPLING_PERIOD),
//                        new AccuracyAndDisplacementBasedLocationFilter(TRACKER_REQUIRED_ACCURACY,
//                                TRACKER_MIN_DISPLACEMENT)
//                );
//            }
//        });
//    }

    private void showStatus(String status) {
        tvStatus.setText(status);
    }
}
