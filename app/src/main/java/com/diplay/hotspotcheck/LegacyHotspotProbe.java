package com.diplay.hotspotcheck;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import java.lang.reflect.Method;

/** Read-only API-17 inventory. No hidden setter is invoked. */
public final class LegacyHotspotProbe {
 public static final class Snapshot {
  public final boolean serviceAvailable,changePermission,accessPermission,setterPresent,configurationGetterPresent,unknown;
  private Snapshot(boolean s,boolean c,boolean a,boolean w,boolean g,boolean u){serviceAvailable=s;changePermission=c;accessPermission=a;setterPresent=w;configurationGetterPresent=g;unknown=u;}
 }
 private LegacyHotspotProbe(){}
 public static Snapshot inspectSnapshot(Context c){
  boolean change=granted(c,"android.permission.CHANGE_WIFI_STATE"), access=granted(c,"android.permission.ACCESS_WIFI_STATE");
  Object service;
  try{service=c.getSystemService(Context.WIFI_SERVICE);}catch(Throwable t){return new Snapshot(false,change,access,false,false,true);}
  if(!(service instanceof WifiManager))return new Snapshot(false,change,access,false,false,false);
  Class<?> type=service.getClass();
  MethodResult setter=find(type,"setWifiApEnabled",new Class<?>[]{android.net.wifi.WifiConfiguration.class,boolean.class});
  MethodResult getter=find(type,"getWifiApConfiguration",new Class<?>[0]);
  return new Snapshot(true,change,access,setter.present,getter.present,setter.unknown||getter.unknown);
 }
 public static String inspect(Context c){
  StringBuilder o=new StringBuilder(); PackageManager pm=c.getPackageManager();
  o.append("Legacy SoftAP API probe (read-only)\nFEATURE_WIFI=").append(pm.hasSystemFeature(PackageManager.FEATURE_WIFI)).append('\n');
  o.append("FEATURE_WIFI_DIRECT=").append(pm.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)).append('\n');
  Snapshot s=inspectSnapshot(c);
  o.append("CHANGE_WIFI_STATE permission=").append(s.changePermission).append('\n');
  o.append("ACCESS_WIFI_STATE permission=").append(s.accessPermission).append('\n');
  o.append("WifiManager service=").append(s.serviceAvailable).append('\n');
  o.append("setWifiApEnabled=").append(s.setterPresent?"PRESENT":"ABSENT_OR_UNKNOWN").append('\n');
  o.append("getWifiApConfiguration=").append(s.configurationGetterPresent?"PRESENT":"ABSENT_OR_UNKNOWN").append('\n');
  o.append("snapshotUnknown=").append(s.unknown).append('\n');
  o.append("Method presence does not prove OEM authorization or AP-capable driver support.\n"); return o.toString();
 }
 private static final class MethodResult{boolean present,unknown;}
 private static MethodResult find(Class<?> t,String n,Class<?>[] a){
  MethodResult r=new MethodResult(); try{t.getMethod(n,a);r.present=true;}
  catch(NoSuchMethodException e){try{t.getDeclaredMethod(n,a);r.present=true;}catch(NoSuchMethodException absent){}catch(Throwable denied){r.unknown=true;}}
  catch(Throwable denied){r.unknown=true;} return r;
 }
 private static boolean granted(Context c,String p){try{return c.checkCallingOrSelfPermission(p)==PackageManager.PERMISSION_GRANTED;}catch(Throwable t){return false;}}
}