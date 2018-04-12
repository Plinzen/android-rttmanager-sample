package de.plinzen.android.rttmanager.wifi;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.rtt.WifiRttManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.continental.android.rttmanager.BuildConfig;
import de.continental.android.rttmanager.R;
import de.plinzen.android.rttmanager.permission.LocationPermissionController;
import de.plinzen.android.rttmanager.ranging.SelectedActivity;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private class ScanWifiNetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final List<ScanResult> wifiNetworks = wifiManager.getScanResults();
            Timber.d("received scan result. %s, size: %d", intent.toString(), wifiNetworks.size());
            lblSearchHint.setVisibility(View.GONE);
            wifiNetworkAdapter.setWifiNetworks(wifiManager.getScanResults());
        }
    }

    private static final int REQUEST_ENABLE_LOCATION = 8956;

    public static boolean isLocationEnabled(@NonNull final Context context) {
        final int locationEnabled = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure
                .LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
        return locationEnabled != Settings.Secure.LOCATION_MODE_OFF;
    }

    @BindView(R.id.coordinator)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.lblSearchHint)
    TextView lblSearchHint;
    @BindView(R.id.listWifiNetworks)
    RecyclerView listWifiNetworks;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.txtCapabilities)
    TextView txtCapabilities;
    @BindView(R.id.txtDeviceToAPSupported)
    TextView txtDeviceToApSupported;
    private LocationPermissionController permissionController;
    private WifiRttManager rttManager;
    private WifiManager wifiManager;
    private WifiNetworkAdapter wifiNetworkAdapter;
    private ScanWifiNetworkReceiver wifiNetworkReceiver;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(getString(R.string.version_info_app, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
        return true;
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions,
            @NonNull final int[] grantResults) {
        if (permissionController.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            startWifiScan();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_ENABLE_LOCATION) {
            if (resultCode == RESULT_OK) {
                startWifiScan();
            } else {
                Snackbar.make(coordinatorLayout, R.string.location_service_disabled, Snackbar.LENGTH_SHORT).setAction
                        (android.R.string.ok, view -> startEnableLocationServicesActivity()).show();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        permissionController = new LocationPermissionController();
        fab.setOnClickListener(view -> startWifiScan());
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiNetworkReceiver = new ScanWifiNetworkReceiver();
        rttManager = (WifiRttManager) getSystemService(Context.WIFI_RTT_RANGING_SERVICE);
        initUI();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(wifiNetworkReceiver);
        } catch (IllegalArgumentException e) {
            // Nothing to do.
        }
    }

    private void handleLocationServiceDisabled() {
        Snackbar.make(coordinatorLayout, R.string.location_service_disabled, Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok, view -> startEnableLocationServicesActivity())
                .show();
    }

    private void initUI() {
        txtDeviceToApSupported.setText(String.valueOf(wifiManager.isDeviceToApRttSupported()));
        txtCapabilities.setText(getString(R.string.rtt_available, rttManager.isAvailable()));
        listWifiNetworks.setLayoutManager(new LinearLayoutManager(this));
        listWifiNetworks.setItemAnimator(new DefaultItemAnimator());
        listWifiNetworks.setHasFixedSize(true);
        listWifiNetworks.setVisibility(View.GONE);
        wifiNetworkAdapter = new WifiNetworkAdapter(getApplicationContext());
        listWifiNetworks.setAdapter(wifiNetworkAdapter);
        wifiNetworkAdapter.setClickListener(wifiNetwork -> {
            startActivity(SelectedActivity.builtIntent(wifiNetwork, getApplicationContext()));
        });
        lblSearchHint.setVisibility(View.VISIBLE);
    }

    private void startEnableLocationServicesActivity() {
        Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(enableLocationIntent, REQUEST_ENABLE_LOCATION);
    }

    private void startWifiScan() {
        if (!permissionController.checkLocationPermissions(getApplicationContext())) {
            permissionController.requestLocationPermission(this, coordinatorLayout);
            return;
        }
        if (!isLocationEnabled(getApplicationContext())) {
            handleLocationServiceDisabled();
            return;
        }
        if (!wifiManager.isWifiEnabled()) {
            Snackbar.make(coordinatorLayout, R.string.enable_wifi, Snackbar.LENGTH_LONG).show();
            return;
        }
        listWifiNetworks.setVisibility(View.VISIBLE);
        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiNetworkReceiver, filter);
        final boolean successful = wifiManager.startScan();
        Timber.d("Started scan successful: %b", successful);
    }
}
