package de.plinzen.android.rttmanager.ranging;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.rtt.RangingRequest;
import android.net.wifi.rtt.RangingResult;
import android.net.wifi.rtt.RangingResultCallback;
import android.net.wifi.rtt.WifiRttManager;
import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.Single;


class RttRangingManager {

    private final WifiRttManager rttManager;

    @SuppressLint("WrongConstant")
    RttRangingManager(final Context context) {
        rttManager = (WifiRttManager) context.getSystemService(Context.WIFI_RTT_RANGING_SERVICE);
    }


    public Single<List<RangingResult>> startRanging(
            @NonNull final ScanResult scanResult) {
        return Single.create(emitter -> {
            final RangingRequest request = new RangingRequest.Builder()
                    .addAccessPoint(scanResult)
                    .build();
            final RangingResultCallback callback = new RangingResultCallback() {
                @Override
                public void onRangingFailure(final int i) {
                    emitter.onError(new RuntimeException("The WiFi-Ranging failed with error code: " + i));
                }

                @Override
                public void onRangingResults(final List<RangingResult> list) {
                    emitter.onSuccess(list);
                }
            };
            rttManager.startRanging(request, callback, null);
        });
    }

}
