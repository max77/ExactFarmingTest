package com.max77.exactfarmingtest.ui;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * SpeechToTextPOC project
 * Created by max77 on 20180112.
 */

public class PermissionHandlerFragment extends Fragment {

    private static final String FRAGMENT_TAG = PermissionHandlerFragment.class.getName();
    private int mRequestCode;
    private RequestPermissionsCallback mCallback;

    public static PermissionHandlerFragment attach(FragmentManager fragmentManager) {
        PermissionHandlerFragment fragment =
                (PermissionHandlerFragment) fragmentManager.findFragmentByTag(FRAGMENT_TAG);

        if (fragment == null) {
            fragment = new PermissionHandlerFragment();

            fragmentManager
                    .beginTransaction()
                    .add(fragment, FRAGMENT_TAG)
                    .commitNow();
        }

        return fragment;
    }

    public void requestPermissions(RequestPermissionsCallback callback, boolean atLeastOne, String... permissions) {
        int numGranted = 0;
        int[] grantResults = new int[permissions.length];

        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            grantResults[i] = ActivityCompat.checkSelfPermission(getContext(), permission);
            numGranted += grantResults[i] == PackageManager.PERMISSION_GRANTED ? 1 : 0;
        }

        mCallback = callback;

        if (numGranted == permissions.length || (atLeastOne && numGranted > 0)) {
            if (mCallback != null)
                mCallback.onRequestPermissionsResult(permissions, grantResults, numGranted);

            mCallback = null;

            return;
        }

        mRequestCode++;

        requestPermissions(permissions, mRequestCode);
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == mRequestCode && mCallback != null) {
            int numGranted = 0;

            for (int res : grantResults)
                numGranted += res == PackageManager.PERMISSION_GRANTED ? 1 : 0;

            mCallback.onRequestPermissionsResult(permissions, grantResults, numGranted);
            mCallback = null;
        }
    }

    public interface RequestPermissionsCallback {
        void onRequestPermissionsResult(String[] permissions, int[] grantResult, int numGranted);
    }
}
