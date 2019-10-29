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

    private final Map<String, List<Meter.Id>> serviceMetersMap = new ConcurrentHashMap<>();

    public void addAggregatesMetrics(String serviceId) {
        log.info("Add gauge metrics for the service {} get started");
        if (serviceMetersMap.containsKey(serviceId)) {
            log.debug("A gauge metrics for the service {} already exists", serviceId);
        } else {
            try {
                MeterBinder faultDetectorMetricsBinder =
                        new FaultDetectorMetricsBinder(aggregatesMap, serviceConfigMap, serviceMetersMap, serviceId);
                faultDetectorMetricsBinder.bindTo(registry);
            } catch (Exception ex) {
                log.error("Error received while adding metrics for service {}", serviceId, ex);
            }

            log.info("Gauge metrics for the service {} was added. Service meter map size is {}. Registry meter size is {}",
                    serviceId, serviceMetersMap.size(), registry.getMeters().size());
        }
    }

    public void removeAggregatesMetrics(String serviceId) {
        List<Meter.Id> serviceMeters = serviceMetersMap.get(serviceId);
        if (serviceMeters != null && serviceId != null) {
            serviceMeters.forEach(registry::remove);
            serviceMetersMap.get(serviceId).clear();
            serviceMetersMap.remove(serviceId);
            log.info("Remove gauge metrics for the service {} complete", serviceId);
        } else {
            log.info("Remove gauge for service {} could not be performed", serviceId);
        }
    }

}
