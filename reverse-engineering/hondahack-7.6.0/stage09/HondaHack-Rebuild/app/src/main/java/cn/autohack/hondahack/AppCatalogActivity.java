package cn.autohack.hondahack;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.Collator;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Lists launcher-visible apps and launches them through Android's public API. */
public final class AppCatalogActivity extends Activity {
    private LinearLayout list;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        list.setPadding(pad, pad, pad, pad);
        scroll.addView(list);
        setContentView(scroll);
        renderApps();
    }

    private void renderApps() {
        list.removeAllViews();
        TextView title = new TextView(this);
        title.setText("应用管理 / 可启动应用");
        title.setTextSize(22);
        list.addView(title);

        final PackageManager pm = getPackageManager();
        Intent query = new Intent(Intent.ACTION_MAIN);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> apps = pm.queryIntentActivities(query, 0);
        Collections.sort(apps, (left, right) -> Collator.getInstance(Locale.getDefault())
                .compare(left.loadLabel(pm).toString(), right.loadLabel(pm).toString()));

        TextView count = new TextView(this);
        count.setText("共找到 " + apps.size() + " 个可启动入口");
        count.setPadding(0, dp(8), 0, dp(12));
        list.addView(count);

        for (final ResolveInfo info : apps) {
            TextView row = new TextView(this);
            row.setText(info.loadLabel(pm) + "\n" + info.activityInfo.packageName);
            row.setTextSize(15);
            row.setPadding(dp(10), dp(12), dp(10), dp(12));
            row.setBackgroundColor(0xfff1f1f1);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = dp(6);
            list.addView(row, lp);
            row.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    Intent launch = pm.getLaunchIntentForPackage(info.activityInfo.packageName);
                    if (launch != null) {
                        try { startActivity(launch); }
                        catch (RuntimeException ex) {
                            Toast.makeText(AppCatalogActivity.this, "无法启动该应用", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
