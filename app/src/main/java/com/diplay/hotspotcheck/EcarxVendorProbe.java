package com.diplay.hotspotcheck;

import android.content.Context;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

/** Read-only discovery of Ecarx SoftAP classes; never invokes vendor APIs. */
public final class EcarxVendorProbe {
    private static final String HOST =
            "com.ecarx.xui.adaptapi.wifiap.IWifiAPHost";
    private static final String WIFI6 =
            "com.ecarx.xui.adaptapi.wifiap.Wifi6Ap";

    public static final class Snapshot {
        public final boolean hostPresent;
        public final boolean wifi6Present;
        public final boolean unknown;
        public final boolean enableMethodPresent;
        public final boolean configurationMethodPresent;
        private Snapshot(boolean host, boolean wifi6, boolean unknown,
                         boolean enable, boolean configuration) {
            this.hostPresent = host;
            this.wifi6Present = wifi6;
            this.unknown = unknown;
            this.enableMethodPresent = enable;
            this.configurationMethodPresent = configuration;
        }
        public boolean completeCandidate() {
            return hostPresent && wifi6Present && enableMethodPresent &&
                    configurationMethodPresent && !unknown;
        }
    }

    private EcarxVendorProbe() { }

    public static Snapshot inspectSnapshot(Context context) {
        ClassLoader loader;
        try {
            loader = context.getClassLoader();
        } catch (Throwable error) {
            return new Snapshot(false, false, true, false, false);
        }
        ClassResult host = inspectClass(loader, HOST, true);
        ClassResult wifi6 = inspectClass(loader, WIFI6, false);
        return new Snapshot(host.present, wifi6.present,
                host.unknown || wifi6.unknown,
                host.enable || wifi6.enable,
                host.configuration || wifi6.configuration);
    }

    public static String inspect(Context context) {
        StringBuilder out = new StringBuilder();
        out.append("Ecarx vendor SoftAP discovery (read-only)\n");
        out.append("Class loading uses initialize=false; no vendor method is invoked.\n");
        ClassLoader loader;
        try {
            loader = context.getClassLoader();
        } catch (Throwable error) {
            return out.append("CLASS_LOADER=UNKNOWN (")
                    .append(error.getClass().getSimpleName()).append(")\n").toString();
        }
        appendClass(out, loader, HOST, true);
        appendClass(out, loader, WIFI6, false);
        Snapshot snapshot = inspectSnapshot(context);
        out.append("ENABLE_METHOD_CANDIDATE=").append(snapshot.enableMethodPresent).append('\n');
        out.append("CONFIG_METHOD_CANDIDATE=").append(snapshot.configurationMethodPresent).append('\n');
        out.append("DISCOVERY_UNKNOWN=").append(snapshot.unknown).append('\n');
        out.append("No vendor object was instantiated and no service was bound.\n");
        return out.toString();
    }

    private static final class ClassResult {
        boolean present, unknown, enable, configuration;
    }

    private static ClassResult inspectClass(ClassLoader loader, String name, boolean host) {
        ClassResult result = new ClassResult();
        try {
            Class<?> type = Class.forName(name, false, loader);
            result.present = true;
            for (Method method : type.getDeclaredMethods()) {
                String n = method.getName().toLowerCase(java.util.Locale.US);
                if (n.contains("setwifi6apenabled") || n.contains("setwifiapenabled"))
                    result.enable = true;
                if (n.contains("setwifi6apconfiguration") || n.contains("setwifiapconfiguration"))
                    result.configuration = true;
                if (host && n.contains("setwifi6apenabled")) result.enable = true;
            }
        } catch (ClassNotFoundException absent) {
            result.present = false;
        } catch (Throwable error) {
            result.unknown = true;
        }
        return result;
    }

    private static void appendClass(StringBuilder out, ClassLoader loader,
                                    String className, boolean host) {
        out.append("\n[").append(className).append("]\n");
        try {
            Class<?> type = Class.forName(className, false, loader);
            out.append("CLASS=PRESENT\n");
            out.append("KIND=").append(type.isInterface() ? "interface" :
                    (java.lang.reflect.Modifier.isAbstract(type.getModifiers())
                    ? "abstract-class" : "class")).append('\n');
            Method[] methods = type.getDeclaredMethods();
            Arrays.sort(methods, new Comparator<Method>() {
                @Override public int compare(Method a, Method b) {
                    return signature(a).compareTo(signature(b));
                }
            });
            int count = 0;
            for (Method method : methods) {
                String n = method.getName().toLowerCase(java.util.Locale.US);
                boolean relevant = n.contains("wifi6ap") || n.contains("wifiap") || n.contains("softap");
                if (!relevant) continue;
                out.append("METHOD=").append(signature(method)).append('\n');
                if (++count >= 40) { out.append("METHODS_TRUNCATED=true\n"); break; }
            }
            out.append("RELEVANT_METHOD_COUNT=").append(count).append('\n');
        } catch (ClassNotFoundException absent) {
            out.append("CLASS=ABSENT\n");
        } catch (Throwable denied) {
            out.append("CLASS=UNKNOWN (").append(denied.getClass().getSimpleName()).append(")\n");
        }
    }

    private static String signature(Method method) {
        StringBuilder value = new StringBuilder();
        value.append(java.lang.reflect.Modifier.toString(method.getModifiers())).append(' ')
                .append(method.getReturnType().getName()).append(' ')
                .append(method.getName()).append('(');
        Class<?>[] p = method.getParameterTypes();
        for (int i = 0; i < p.length; i++) {
            if (i > 0) value.append(',');
            value.append(p[i].getName());
        }
        return value.append(')').toString().trim();
    }
}