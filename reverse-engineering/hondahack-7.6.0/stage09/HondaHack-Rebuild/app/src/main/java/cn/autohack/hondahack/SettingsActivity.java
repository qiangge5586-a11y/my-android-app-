package cn.autohack.hondahack;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

/** Only safe local settings are exposed at this stage. */
public final class SettingsActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);
        TextView heading = new TextView(this);
        heading.setText("Safe local preferences");
        heading.setTextSize(20);
        root.addView(heading);
        TextView info = new TextView(this);
        info.setText("Original preference keys are being audited. High-risk vehicle/system options are not exposed.");
        root.addView(info);
        Switch safeMode = new Switch(this);
        safeMode.setText("Keep safe mode enabled");
        safeMode.setChecked(true);
        safeMode.setEnabled(false);
        root.addView(safeMode, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(root);
    }
}
