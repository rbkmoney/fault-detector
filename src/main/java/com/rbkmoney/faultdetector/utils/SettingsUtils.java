package com.rbkmoney.faultdetector.utils;

import com.rbkmoney.damsel.fault_detector.ServiceConfig;
import com.rbkmoney.faultdetector.data.ServiceSettings;

public final class SettingsUtils {

    public static ServiceSettings getServiceSettings(ServiceConfig serviceConfig) {
        ServiceSettings serviceSettings = new ServiceSettings();
        serviceSettings.setOperationLifetime(serviceConfig.getOperationLifetime());
        serviceSettings.setSlidingWindow(serviceConfig.getSlidingWindow());
        serviceSettings.setTimeoutDelta(serviceConfig.getTimeoutDelta());
        serviceSettings.setHoveringOperationErrorDelay(serviceConfig.getHoveringOperationErrorDelay());
        return serviceSettings;
    }

    private SettingsUtils() {}

}
