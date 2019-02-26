package com.rbkmoney.faultdetector.utils;

import com.rbkmoney.damsel.fault_detector.ServiceConfig;
import com.rbkmoney.faultdetector.data.ServiceSettings;

public final class SettingsUtils {

    public static ServiceSettings getServiceSettings(ServiceConfig serviceConfig) {
        ServiceSettings serviceSettings = new ServiceSettings();
        serviceSettings.setOperationTimeLimit(serviceConfig.getOperationTimeLimit());
        serviceSettings.setSlidingWindow(serviceConfig.getSlidingWindow());
        serviceSettings.setPreAggregationSize(serviceConfig.getPreAggregationSize());
        return serviceSettings;
    }

    private SettingsUtils() {}

}
