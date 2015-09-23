package com.etiennelawlor.loop;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import io.fabric.sdk.android.Fabric;
import java.io.File;

import timber.log.Timber;

/**
 * Created by etiennelawlor on 5/23/15.
 */
public class LoopApplication extends Application {

    // region Static Variables
    private static LoopApplication sCurrentApplication = null;
    // endregion

    // region Member Variables
    private RefWatcher mRefWatcher;
    // endregion

    // region Callbacks
    // endregion

    @Override
    public void onCreate() {
        super.onCreate();

        initializeFabric();
        initializeLeakCanary();
        initializeTimber();
        initializeFlurry();

        sCurrentApplication = this;

    }

    // region Helper Methods
    public static LoopApplication get() {
        return sCurrentApplication;
    }

    public static File getCacheDirectory()  {
        return sCurrentApplication.getCacheDir();
    }

    public static RefWatcher getRefWatcher(Context context) {
        LoopApplication application = (LoopApplication) context.getApplicationContext();
        return application.mRefWatcher;
    }

    private void initializeFabric(){
        if (!Fabric.isInitialized()) {
            final Fabric fabric = new Fabric.Builder(this)
                    .kits(new Crashlytics())
                    .debuggable(true)
                    .build();

            Fabric.with(fabric);
        }
    }

    private void initializeLeakCanary(){
        mRefWatcher = LeakCanary.install(this);
    }

    private void initializeTimber(){
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }
    }

    private void initializeFlurry(){
        FlurryAgent.setLogEnabled(false);

        FlurryAgent.init(this, getString(R.string.flurry_api_key));
    }
    // endregion

    // region Inner Classes

    /** A tree which logs important information for crash reporting. */
    private static class CrashReportingTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }

//            FakeCrashLibrary.log(priority, tag, message);
//
//            if (t != null) {
//                if (priority == Log.ERROR) {
//                    FakeCrashLibrary.logError(t);
//                } else if (priority == Log.WARN) {
//                    FakeCrashLibrary.logWarning(t);
//                }
//            }

            Crashlytics.log(priority, tag, message);

            if (t != null) {
                if (priority == Log.ERROR) {
                    Crashlytics.logException(t);
                } else if (priority == Log.INFO) {
                    Crashlytics.log(message);
                }
            }
        }
    }

    // endregion
}