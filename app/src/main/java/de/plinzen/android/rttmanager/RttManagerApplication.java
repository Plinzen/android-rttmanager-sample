package de.plinzen.android.rttmanager;

import android.app.Application;

import timber.log.Timber;

public class RttManagerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
    }
}
