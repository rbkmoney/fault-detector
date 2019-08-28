package com.rbkmoney.faultdetector.utils;

import com.rbkmoney.damsel.fault_detector.ServiceConfig;
import com.rbkmoney.faultdetector.data.ServiceSettings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SettingsMappingUtils {

    public static ServiceSettings getServiceSettings(ServiceConfig serviceConfig) {
        ServiceSettings serviceSettings = new ServiceSettings();
        serviceSettings.setOperationTimeLimit(serviceConfig.getOperationTimeLimit());
        serviceSettings.setSlidingWindow(serviceConfig.getSlidingWindow());
        serviceSettings.setPreAggregationSize(serviceConfig.getPreAggregationSize() < 1 ?
                1 : serviceConfig.getPreAggregationSize());
        return serviceSettings;
    }

}
