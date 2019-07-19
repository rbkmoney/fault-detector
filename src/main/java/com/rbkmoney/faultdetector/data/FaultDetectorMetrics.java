package com.rbkmoney.faultdetector.data;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class FaultDetectorMetrics {

    private final Map<String, ServiceAggregates> aggregatesMap;

    private final MeterRegistry registry;

    private List<String> registerServiceList = new CopyOnWriteArrayList<>();

    public void addAggregatesMetrics(String serviceId) {
        if (registerServiceList.contains(serviceId)) {
            log.info("A gauge metrics for the service {} already exists", serviceId);
            return;
        }

        registerServiceList.add(serviceId);
        ServiceAggregates serviceAggregates = aggregatesMap.get(serviceId);
        new FaultDetectorMetricsBinder(serviceAggregates, serviceId).bindTo(registry);

        log.info("Add gauge metrics for the service {}", serviceId);
    }

    public void removeAggregatesMetrics(String serviceId) {
        if (registry.getMeters() != null && serviceId != null) {
            registry.getMeters().removeIf(meter -> meter.getId().getName().contains(serviceId));
            log.info("Remove gauge metrics for the service {} complete", serviceId);
        } else {
            log.info("Remove gauge for service {} could not be performed", serviceId);
        }
    }

}
