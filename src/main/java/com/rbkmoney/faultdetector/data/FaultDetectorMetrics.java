package com.rbkmoney.faultdetector.data;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class FaultDetectorMetrics {

    private final Map<String, ServiceAggregates> aggregatesMap;

    private final Map<String, ServiceSettings> serviceConfigMap;

    private final MeterRegistry registry;

    private final Map<String, List<Meter.Id>> serviceMetersMap = new ConcurrentHashMap<>();;

    public void addAggregatesMetrics(String serviceId) {
        if (serviceMetersMap.containsKey(serviceId)) {
            log.info("A gauge metrics for the service {} already exists", serviceId);
            return;
        } else {
            MeterBinder faultDetectorMetricsBinder =
                    new FaultDetectorMetricsBinder(aggregatesMap, serviceConfigMap, serviceMetersMap, serviceId);
            faultDetectorMetricsBinder.bindTo(registry);
            log.info("Add gauge metrics for the service {}", serviceId);
        }
    }

    public void removeAggregatesMetrics(String serviceId) {
        List<Meter.Id> serviceMeters = serviceMetersMap.get(serviceId);
        if (serviceMeters != null && serviceId != null) {
            serviceMeters.forEach(meterId -> registry.remove(meterId));
            serviceMetersMap.remove(serviceId);
            log.info("Remove gauge metrics for the service {} complete", serviceId);
        } else {
            log.info("Remove gauge for service {} could not be performed", serviceId);
        }
    }

}
