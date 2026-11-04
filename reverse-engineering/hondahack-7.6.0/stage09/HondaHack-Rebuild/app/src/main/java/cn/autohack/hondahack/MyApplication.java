package cn.autohack.hondahack;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.WindowManager;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/** Safe shell; original opaque/native initialization is intentionally omitted. */
public final class MyApplication extends Application {
    private SharedPreferences preferences;

    @Override public void onCreate() {
        super.onCreate();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity activity, Bundle state) {
                applyScreenPolicy(activity);
            }
            @Override public void onActivityStarted(Activity activity) { }
            @Override public void onActivityResumed(Activity activity) {
                applyScreenPolicy(activity);
            }
            @Override public void onActivityPaused(Activity activity) {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            @Override public void onActivityStopped(Activity activity) { }
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) { }
            @Override public void onActivityDestroyed(Activity activity) { }
        });
        // Original DEX reads enable_log_file here. Logging is not recreated pending review.
    }

    private void applyScreenPolicy(Activity activity) {
        if (preferences().getBoolean("keep_screen_on", false)) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public SharedPreferences preferences() {
        if (preferences == null) preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences;
    }
}
