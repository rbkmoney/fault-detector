package com.rbkmoney.faultdetector.handlers;

import com.rbkmoney.faultdetector.binders.FaultDetectorMetricsBinder;
import com.rbkmoney.faultdetector.data.ServiceAggregates;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.statsd.StatsdMeterRegistry;
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
public class AddProviderMetricsHandler implements Handler<String> {

    private final Map<String, ServiceAggregates> aggregatesMap;

    private final StatsdMeterRegistry statsdMeterRegistry;

    private final Map<String, List<Gauge>> serviceMetersMap = new ConcurrentHashMap<>();

    private final List<FaultDetectorMetricsBinder> meters = new CopyOnWriteArrayList<>();

    @Override
    public void handle(String serviceId) {
        log.warn("Add gauge metrics for the service {} get started", serviceId);
        FaultDetectorMetricsBinder faultDetectorMetricsBinder =
                new FaultDetectorMetricsBinder(aggregatesMap, serviceId);
        faultDetectorMetricsBinder.bindTo(statsdMeterRegistry);
        meters.add(faultDetectorMetricsBinder);
        log.debug("Gauge metrics for the service {} was added. Service meter map size is {}. " +
                        "Registry meter size is {}. Meter registry config: {}",
                serviceId, serviceMetersMap.size(), statsdMeterRegistry.getMeters().size(),
                statsdMeterRegistry.config());
    }

}
