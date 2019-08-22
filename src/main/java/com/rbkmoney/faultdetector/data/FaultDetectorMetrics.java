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
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class FaultDetectorMetrics {

    private final Map<String, ServiceAggregates> aggregatesMap;

    private final MeterRegistry registry;

    private final Map<String, MeterBinder> serviceMeterBinders;

    private final Map<String, ServiceSettings> serviceConfigMap;

    private final Map<String, List<Meter.Id>> serviceMetersMap = new ConcurrentHashMap<>();;

    private List<String> registerServiceList = new CopyOnWriteArrayList<>();

    public void addAggregatesMetrics(String serviceId) {
        if (registerServiceList.contains(serviceId)) {
            log.info("A gauge metrics for the service {} already exists", serviceId);
            return;
        }

        registerServiceList.add(serviceId);
        ServiceAggregates serviceAggregates = aggregatesMap.get(serviceId);
        ServiceSettings serviceSettings = serviceConfigMap.get(serviceId);
        MeterBinder faultDetectorMetricsBinder =
                new FaultDetectorMetricsBinder(serviceAggregates, serviceId, serviceMetersMap, serviceSettings);
        faultDetectorMetricsBinder.bindTo(registry);
        serviceMeterBinders.put(serviceId, faultDetectorMetricsBinder);

        log.info("Add gauge metrics for the service {}", serviceId);
    }

    public void removeAggregatesMetrics(String serviceId) {
        List<Meter.Id> serviceMeters = serviceMetersMap.get(serviceId);
        if (serviceMeters != null && serviceId != null) {
            serviceMeters.forEach(meterId -> registry.remove(meterId));
            log.info("Remove gauge metrics for the service {} complete", serviceId);
        } else {
            log.info("Remove gauge for service {} could not be performed", serviceId);
        }
    }

    public boolean isExistServiceMetrics(String serviceId) {
        return serviceMetersMap.containsKey(serviceId);
    }

}
