package com.rbkmoney.faultdetector.data;

import io.micrometer.core.instrument.Gauge;
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

    private final MeterRegistry meterRegistry;

    private final Map<String, List<Gauge>> serviceMetersMap = new ConcurrentHashMap<>();

    private final List<MeterBinder> meters = new CopyOnWriteArrayList<>();

    public void addAggregatesMetrics(String serviceId) {
        if (!serviceMetersMap.containsKey(serviceId)) {
            log.info("Add gauge metrics for the service {} get started", serviceId);
            MeterBinder faultDetectorMetricsBinder =
                    new FaultDetectorMetricsBinder(aggregatesMap, serviceMetersMap, serviceId);
            faultDetectorMetricsBinder.bindTo(meterRegistry);
            meters.add(faultDetectorMetricsBinder);
            log.info("Gauge metrics for the service {} was added. Service meter map size is {}. " +
                            "Registry meter size is {}. Meter registry config: {}",
                    serviceId, serviceMetersMap.size(), meterRegistry.getMeters().size(),
                    meterRegistry.config());
        }
    }

}
