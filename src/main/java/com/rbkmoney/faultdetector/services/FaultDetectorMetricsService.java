package com.rbkmoney.faultdetector.services;

import com.rbkmoney.faultdetector.data.ServiceAggregates;
import com.rbkmoney.faultdetector.handlers.Handler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Lazy
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "management.metrics.export.statsd.enabled", havingValue = "true")
public class FaultDetectorMetricsService {

    private final Map<String, ServiceAggregates> aggregatesMap;

    private final Handler<String> addProviderMetricsHandler;

    private final Set<String> services = new HashSet<>();

    @Scheduled(fixedRateString = "${operations.metricsCheckPeriod}",
            initialDelayString = "${operations.metricsInitialDelayPeriod}")
    private void checkMetrics() {
        for (String serviceId : aggregatesMap.keySet()) {
            if (!services.contains(serviceId)) {
                addProviderMetricsHandler.handle(serviceId);
                services.add(serviceId);
            }
        }
    }

}
