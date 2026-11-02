package com.diplay.hotspotcheck;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Read-only API-17-compatible inventory of legacy SoftAP entry points.
 *
 * This class deliberately never invokes hidden setters, starts hostapd,
 * toggles Wi-Fi, changes interface state, or launches a privileged shell.
 * Presence of a method is not proof that the caller is authorized to use it.
 */
public final class LegacyHotspotProbe {
    private LegacyHotspotProbe() { }

    public static String inspect(Context context) {
        StringBuilder out = new StringBuilder();
        PackageManager pm = context.getPackageManager();
        out.append("Legacy SoftAP API probe (read-only)\n");
        out.append("FEATURE_WIFI=")
           .append(pm.hasSystemFeature(PackageManager.FEATURE_WIFI)).append('\n');
        out.append("FEATURE_WIFI_DIRECT=")
           .append(pm.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)).append('\n');
        out.append("CHANGE_WIFI_STATE permission=")
           .append(granted(context, "android.permission.CHANGE_WIFI_STATE")).append('\n');
        out.append("ACCESS_WIFI_STATE permission=")
           .append(granted(context, "android.permission.ACCESS_WIFI_STATE")).append('\n');

        Object service = context.getSystemService(Context.WIFI_SERVICE);
        if (!(service instanceof WifiManager)) {
            out.append("WifiManager service unavailable\n");
            return out.toString();
        }

        WifiManager wifi = (WifiManager) service;
        try {
            out.append("wifiEnabled=").append(wifi.isWifiEnabled()).append('\n');
        } catch (Throwable t) {
            out.append("wifiEnabled=UNKNOWN (")
               .append(t.getClass().getSimpleName()).append(")\n");
        }

        Class<?> type = wifi.getClass();
        out.append("WifiManager implementation=")
           .append(type.getName()).append('\n');
        appendMethod(out, type, "getWifiApState", new Class<?>[0]);
        appendMethod(out, type, "getWifiApConfiguration", new Class<?>[0]);
        appendMethod(out, type, "isWifiApEnabled", new Class<?>[0]);
        appendMethod(out, type, "setWifiApEnabled",
                new Class<?>[] { android.net.wifi.WifiConfiguration.class, boolean.class });
        appendMethod(out, type, "getDhcpInfo", new Class<?>[0]);

        out.append("Interpretation: API presence is only a capability hint; ")
           .append("runtime permission, vendor implementation, driver AP mode, ")
           .append("and successful client association remain unverified.\n");
        return out.toString();
    }

    private static boolean granted(Context context, String permission) {
        try {
            return context.checkCallingOrSelfPermission(permission)
                    == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void appendMethod(StringBuilder out, Class<?> type,
                                     String name, Class<?>[] parameters) {
        try {
            Method method = type.getMethod(name, parameters);
            out.append(name).append("=PRESENT (")
               .append(method.getReturnType().getName()).append(")\n");
        } catch (NoSuchMethodException missing) {
            try {
                Method method = type.getDeclaredMethod(name, parameters);
                out.append(name).append("=DECLARED_HIDDEN (")
                   .append(method.getReturnType().getName()).append(")\n");
            } catch (Throwable absent) {
                out.append(name).append("=ABSENT\n");
            }
        } catch (Throwable denied) {
            out.append(name).append("=UNKNOWN (")
               .append(denied.getClass().getSimpleName()).append(")\n");
        }
    }
}
