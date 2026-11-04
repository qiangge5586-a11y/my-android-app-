package cn.autohack.hondahack;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/** Inert placeholder. Vehicle broadcasts, location callbacks and timers are not recreated. */
public final class MyService extends Service {
    @Override public IBinder onBind(Intent intent) { return null; }
    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        stopSelf(startId);
        return START_NOT_STICKY;
    }
}
