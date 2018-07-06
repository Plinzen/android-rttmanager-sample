package de.plinzen.android.rttmanager.ranging;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.rtt.RangingResult;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.plinzen.android.rttmanager.R;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class SelectedActivity extends AppCompatActivity {

    private static final String EXTRA_WIFI_NETWORK = "WIFI_NETWORK";

    public static Intent builtIntent(final ScanResult wifiNetwork, Context context) {
        Intent intent = new Intent(context, SelectedActivity.class);
        intent.putExtra(EXTRA_WIFI_NETWORK, wifiNetwork);
        return intent;
    }

    @BindView(R.id.logView)
    TextView logView;
    @BindView(R.id.startButton)
    Button startButton;
    @BindView(R.id.stopButton)
    Button stopButton;
    private Disposable rangingDisposable;
    private RttRangingManager rangingManager;
    private ScanResult wifiNetwork;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected);
        ButterKnife.bind(this);
        rangingManager = new RttRangingManager(getApplicationContext());
        readIntentExtras();
        initUI();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRanging();
    }

    private String buildLogString(final RangingResult result) {
        String resultString = getString(R.string.log, result.getRangingTimestampMillis(), result.getRssi(), result
                .getDistanceMm(), logView.getText()
                .toString());
        if (resultString.length() > 5000) {
            return resultString.substring(0, 5000);
        }
        return resultString;
    }


    private void initStartButtonListener() {
        startButton.setOnClickListener(view -> onStartButtonClicked());

    }

    private void initStopButtonListener() {
        stopButton.setOnClickListener(view -> stopRanging());

    }

    private void initUI() {
        setTitle(getString(R.string.selected_activity_title, wifiNetwork.SSID));
        initStartButtonListener();
        initStopButtonListener();
    }

    private void onStartButtonClicked() {
        logView.setText("");

        rangingDisposable = rangingManager.startRanging(wifiNetwork)
                .repeat()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::writeOutput,
                        throwable -> {
                            Timber.e(throwable, "An unexpected error occurred while start ranging.");
                            Snackbar.make(logView, throwable.getMessage(), Snackbar.LENGTH_LONG).show();
                        });
    }

    private void readIntentExtras() {
        Bundle extras = getIntent().getExtras();
        wifiNetwork = (ScanResult) extras.get(EXTRA_WIFI_NETWORK);
    }

    private void stopRanging() {
        if (rangingDisposable == null) {
            return;
        }
        rangingDisposable.dispose();
    }

    private void writeOutput(@NonNull final List<RangingResult> result) {
        if (result.isEmpty()) {
            Timber.d("EMPTY ranging result received.");
            return;
        }
        for (RangingResult res : result) {
            logView.setText(buildLogString(res));
            Timber.d("Result: %d RSSI: %d Distance: %d mm", res.getRangingTimestampMillis(), res.getRssi(), res
                    .getDistanceMm());
        }
    }

}
