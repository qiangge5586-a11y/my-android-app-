package cn.autohack.hondahack;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/** Safe shell; original opaque/native initialization is intentionally omitted. */
public final class MyApplication extends Application {
    private SharedPreferences preferences;
    @Override public void onCreate() {
        super.onCreate();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Original DEX reads enable_log_file here. Logging is not recreated pending review.
    }
    public SharedPreferences preferences() {
        if (preferences == null) preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences;
    }
}
