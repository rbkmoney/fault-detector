package com.rbkmoney.faultdetector.data;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.statsd.StatsdMeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class FaultDetectorMetrics {

    private final Map<String, ServiceAggregates> aggregatesMap;

    private final Map<String, ServiceSettings> serviceConfigMap;

    private final StatsdMeterRegistry fdStatsdMeterRegistry;

    private final Map<String, List<Gauge>> serviceMetersMap = new ConcurrentHashMap<>();

    private final List<MeterBinder> meters = new ArrayList<>();

    public void addAggregatesMetrics(String serviceId) {
        log.info("Add gauge metrics for the service {} get started", serviceId);
        if (serviceMetersMap.containsKey(serviceId)
                && serviceMetersMap.get(serviceId) != null
                && serviceMetersMap.get(serviceId).stream().allMatch(gauge -> gauge != null) ) {
            log.info("A gauge metrics for the service {} already exists", serviceId);
        } else {
            try {
                MeterBinder faultDetectorMetricsBinder =
                        new FaultDetectorMetricsBinder(aggregatesMap, serviceConfigMap, serviceMetersMap, serviceId);
                meters.add(faultDetectorMetricsBinder);
                faultDetectorMetricsBinder.bindTo(fdStatsdMeterRegistry);
            } catch (Exception ex) {
                log.error("Error received while adding metrics for service {}", serviceId, ex);
            }

            log.info("Gauge metrics for the service {} was added. Service meter map size is {}. " +
                            "Registry meter size is {}. Meter registry config: {}",
                    serviceId, serviceMetersMap.size(), fdStatsdMeterRegistry.getMeters().size(),
                    fdStatsdMeterRegistry.config());
        }
    }

    public void removeAggregatesMetrics(String serviceId) {
        List<Gauge> serviceMeters = serviceMetersMap.get(serviceId);
        if (serviceMeters != null && serviceId != null) {
            serviceMeters.forEach(fdStatsdMeterRegistry::remove);
            serviceMetersMap.get(serviceId).clear();
            serviceMetersMap.remove(serviceId);
            log.info("Remove gauge metrics for the service {} complete", serviceId);
        } else {
            log.info("Remove gauge for service {} could not be performed", serviceId);
        }
    }

}
