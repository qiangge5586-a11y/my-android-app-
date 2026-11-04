package cn.autohack.hondahack;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.LinearLayout;

/** A visual-only meter mock. Values are fixed demo data, never vehicle telemetry. */
public final class MeterActivity extends Activity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        root.setPadding(pad, pad, pad, pad);
        TextView title = new TextView(this);
        title.setText("仪表盘预览");
        title.setTextSize(23);
        root.addView(title);
        TextView warning = new TextView(this);
        warning.setText("演示数据 · 未连接车辆 · 不代表真实车速或车辆状态");
        warning.setPadding(0, dp(6), 0, dp(8));
        root.addView(warning);
        root.addView(new GaugeView());
        TextView values = new TextView(this);
        values.setText("水温：86°C\n电压：13.8V\n平均油耗：6.8 L/100km\n剩余续航：演示值 420 km");
        values.setTextSize(18);
        values.setPadding(dp(8), dp(10), dp(8), dp(10));
        root.addView(values);
        TextView note = new TextView(this);
        note.setText("当前版本只绘制仪表样式，不读取 OBD、CAN、厂商 AIDL、共享文件或系统车辆服务。");
        root.addView(note);
        setContentView(root);
    }

    private final class GaugeView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        GaugeView() { super(MeterActivity.this); setMinimumHeight(dp(250)); }
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float cx = getWidth() / 2f;
            float cy = Math.max(dp(120), getHeight() * 0.47f);
            float radius = Math.min(getWidth() * 0.34f, dp(118));
            RectF arc = new RectF(cx-radius, cy-radius, cx+radius, cy+radius);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(15));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(0xffdddddd);
            canvas.drawArc(arc, 135, 270, false, paint);
            paint.setColor(0xff4b86c6);
            canvas.drawArc(arc, 135, 270 * (72f/220f), false, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xff222222);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(dp(46));
            canvas.drawText("72", cx, cy+dp(8), paint);
            paint.setTextSize(dp(16));
            canvas.drawText("km/h · DEMO", cx, cy+dp(34), paint);
            paint.setTextSize(dp(12));
            canvas.drawText("0", cx-radius+dp(4), cy+radius/2, paint);
            canvas.drawText("220", cx+radius-dp(8), cy+radius/2, paint);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
