package com.diplay.hotspotcheck;

/**
 * Stage 32 API-17 backend gate.
 *
 * This is a decision helper, not a hotspot starter. It deliberately does not
 * invoke WifiManager setters or hidden methods. Method presence plus ordinary
 * permissions is not proof of OEM authorization or driver support.
 */
public final class SoftApBackendDecision {
    public enum Status {
        CANDIDATE_REQUIRES_DEVICE_VALIDATION,
        BLOCKED_MISSING_PERMISSION,
        BLOCKED_API_UNAVAILABLE,
        UNKNOWN
    }

    public static final class Input {
        public final boolean wifiManagerAvailable;
        public final boolean setWifiApEnabledPresent;
        public final boolean getWifiApConfigurationPresent;
        public final boolean changeWifiStateGranted;
        public final boolean accessWifiStateGranted;

        public Input(boolean wifiManagerAvailable,
                     boolean setWifiApEnabledPresent,
                     boolean getWifiApConfigurationPresent,
                     boolean changeWifiStateGranted,
                     boolean accessWifiStateGranted) {
            this.wifiManagerAvailable = wifiManagerAvailable;
            this.setWifiApEnabledPresent = setWifiApEnabledPresent;
            this.getWifiApConfigurationPresent = getWifiApConfigurationPresent;
            this.changeWifiStateGranted = changeWifiStateGranted;
            this.accessWifiStateGranted = accessWifiStateGranted;
        }
    }

    public static final class Result {
        public final Status status;
        public final String reason;

        private Result(Status status, String reason) {
            this.status = status;
            this.reason = reason;
        }

        @Override public String toString() {
            return status.name() + ": " + reason;
        }
    }

    private SoftApBackendDecision() { }

    public static Result evaluate(Input input) {
        if (input == null) {
            return new Result(Status.UNKNOWN, "No capability inventory was supplied.");
        }
        if (!input.wifiManagerAvailable) {
            return new Result(Status.BLOCKED_API_UNAVAILABLE,
                    "WifiManager service is unavailable.");
        }
        if (!input.accessWifiStateGranted || !input.changeWifiStateGranted) {
            return new Result(Status.BLOCKED_MISSING_PERMISSION,
                    "Required ordinary Wi-Fi permissions are not both granted.");
        }
        if (!input.setWifiApEnabledPresent || !input.getWifiApConfigurationPresent) {
            return new Result(Status.BLOCKED_API_UNAVAILABLE,
                    "Legacy AP setter or configuration readback method is absent.");
        }
        return new Result(Status.CANDIDATE_REQUIRES_DEVICE_VALIDATION,
                "Legacy API signatures are visible. OEM permission enforcement, " +
                "framework implementation, AP-capable driver, regulatory support, " +
                "and real client association are still unverified. No AP was started.");
    }
}
