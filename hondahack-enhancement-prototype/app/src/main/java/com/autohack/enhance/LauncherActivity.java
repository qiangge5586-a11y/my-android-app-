package com.autohack.enhance;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class LauncherActivity extends Activity {
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        startActivity(new Intent(this, EnhancementActivity.class));
        finish();
    }
}
