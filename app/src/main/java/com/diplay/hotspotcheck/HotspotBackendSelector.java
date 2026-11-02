package com.diplay.hotspotcheck;

/** Ordered fail-closed decision. A candidate is never permission to start an AP. */
public final class HotspotBackendSelector {
 public enum Backend { ECARX_VENDOR, ANDROID_LEGACY_FRAMEWORK, NONE }
 public enum Status { CANDIDATE_REQUIRES_VENDOR_SERVICE_AND_PERMISSION_VALIDATION,
  CANDIDATE_REQUIRES_DEVICE_VALIDATION, BLOCKED_MISSING_PERMISSION, BLOCKED_API_UNAVAILABLE, UNKNOWN }
 public static final class Result {
  public final Backend backend; public final Status status; public final String reason;
  private Result(Backend b,Status s,String r){backend=b;status=s;reason=r;}
  @Override public String toString(){return backend.name()+" / "+status.name()+": "+reason;}
 }
 private HotspotBackendSelector(){}
 public static Result evaluate(EcarxVendorProbe.Snapshot v,LegacyHotspotProbe.Snapshot l){
  if(v==null||l==null)return new Result(Backend.NONE,Status.UNKNOWN,"Required read-only inventory missing.");
  if(v.unknown)return new Result(Backend.NONE,Status.UNKNOWN,"Ecarx discovery inconclusive; no fallback selected.");
  if(v.completeCandidate())return new Result(Backend.ECARX_VENDOR,
   Status.CANDIDATE_REQUIRES_VENDOR_SERVICE_AND_PERMISSION_VALIDATION,
   "Class/method candidate only; service, binder, signature permission and firmware authorization unverified. Nothing invoked.");
  if(l.unknown)return new Result(Backend.NONE,Status.UNKNOWN,"Legacy framework inventory inconclusive.");
  if(!l.serviceAvailable||!l.setterPresent||!l.configurationGetterPresent)
   return new Result(Backend.NONE,Status.BLOCKED_API_UNAVAILABLE,"Legacy WifiManager service/API incomplete.");
  if(!l.changePermission||!l.accessPermission)
   return new Result(Backend.NONE,Status.BLOCKED_MISSING_PERMISSION,"Ordinary Wi-Fi permissions are not both granted.");
  return new Result(Backend.ANDROID_LEGACY_FRAMEWORK,Status.CANDIDATE_REQUIRES_DEVICE_VALIDATION,
   "Legacy API candidate only; OEM enforcement, driver AP mode, radio concurrency, config readback and client association unverified. Nothing started.");
 }
}