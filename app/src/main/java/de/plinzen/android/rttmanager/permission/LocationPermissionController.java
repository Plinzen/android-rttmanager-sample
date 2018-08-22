package de.plinzen.android.rttmanager.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import de.plinzen.android.rttmanager.R;

public class LocationPermissionController {

    private static final int REQUEST_LOCATION_PERMISSION = 8545;

    public boolean checkLocationPermissions(final Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission
                .ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
            @NonNull final int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            return verifyPermissions(grantResults);
        }
        return false;
    }

    public void requestLocationPermission(final Activity activity, final View snackbarContainer) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission
                        .ACCESS_FINE_LOCATION)) {
            Snackbar.make(snackbarContainer, R.string.permission_location_description, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, view -> requestPermissions(activity)).show();
        } else {
            requestPermissions(activity);
        }
    }

    private void requestPermissions(final Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSION);
    }

    private boolean verifyPermissions(int[] grantResults) {
        if (grantResults.length < 1) {
            return false;
        }

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}