package com.diplay.hotspotcheck;

import android.content.Context;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;

/** Read-only discovery of Ecarx SoftAP classes; never invokes vendor APIs. */
public final class EcarxVendorProbe {
    private static final String HOST =
            "com.ecarx.xui.adaptapi.wifiap.IWifiAPHost";
    private static final String WIFI6 =
            "com.ecarx.xui.adaptapi.wifiap.Wifi6Ap";

    private EcarxVendorProbe() { }

    public static String inspect(Context context) {
        StringBuilder out = new StringBuilder();
        out.append("Ecarx vendor SoftAP discovery (read-only)\n");
        out.append("Class loading uses initialize=false; no vendor method is invoked.\n");
        final ClassLoader loader;
        try {
            loader = context.getClassLoader();
        } catch (Throwable error) {
            return out.append("CLASS_LOADER=UNKNOWN (")
                    .append(error.getClass().getSimpleName()).append(")\n").toString();
        }
        inspectClass(out, loader, HOST, true);
        inspectClass(out, loader, WIFI6, false);
        out.append("Presence does not prove service registration, permission, ")
                .append("hardware support, or successful AP operation.\n");
        out.append("No vendor object was instantiated and no service was bound.\n");
        return out.toString();
    }

    private static void inspectClass(StringBuilder out, ClassLoader loader,
                                     String className, boolean host) {
        out.append("\n[").append(className).append("]\n");
        final Class<?> type;
        try {
            type = Class.forName(className, false, loader);
        } catch (ClassNotFoundException missing) {
            out.append("CLASS=ABSENT\n");
            return;
        } catch (Throwable denied) {
            out.append("CLASS=UNKNOWN (")
                    .append(denied.getClass().getSimpleName()).append(")\n");
            return;
        }
        out.append("CLASS=PRESENT\n");
        out.append("KIND=").append(type.isInterface() ? "interface" :
                (Modifier.isAbstract(type.getModifiers()) ? "abstract-class" : "class"))
                .append('\n');
        try {
            Method[] methods = type.getDeclaredMethods();
            Arrays.sort(methods, new Comparator<Method>() {
                @Override public int compare(Method a, Method b) {
                    return signature(a).compareTo(signature(b));
                }
            });
            int count = 0;
            for (Method method : methods) {
                String name = method.getName().toLowerCase(java.util.Locale.US);
                boolean relevant = name.contains("wifi6ap") ||
                        name.contains("wifiap") || name.contains("softap");
                if (!relevant) continue;
                if (!host && !(name.contains("setwifi6ap") ||
                        name.contains("getwifi6ap") || name.contains("iswifi6ap"))) continue;
                out.append("METHOD=").append(signature(method)).append('\n');
                if (++count >= 40) {
                    out.append("METHODS_TRUNCATED=true\n");
                    break;
                }
            }
            out.append("RELEVANT_METHOD_COUNT=").append(count).append('\n');
            if (count == 0) out.append("RELEVANT_METHODS=NOT_FOUND\n");
        } catch (Throwable error) {
            out.append("METHOD_INVENTORY=UNKNOWN (")
                    .append(error.getClass().getSimpleName()).append(")\n");
        }
    }

    private static String signature(Method method) {
        StringBuilder value = new StringBuilder();
        value.append(Modifier.toString(method.getModifiers())).append(' ')
                .append(method.getReturnType().getName()).append(' ')
                .append(method.getName()).append('(');
        Class<?>[] parameters = method.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) value.append(',');
            value.append(parameters[i].getName());
        }
        return value.append(')').toString().trim();
    }
}