package com.rbkmoney.faultdetector.data;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.emptyList;

@Slf4j
@Component
@RequiredArgsConstructor
public class FaultDetectorMetrics {

    private final Map<String, ServiceAggregates> aggregatesMap;

    private final MeterRegistry registry;

    private Map<String, List<Gauge>> aggregatesMetricsMap = new HashMap<>();

    public void addAggregatesMetrics(String serviceId) {
        List<Gauge> aggregatesMetricsList = new CopyOnWriteArrayList<>();
        ServiceAggregates serviceAggregates = aggregatesMap.get(serviceId);

        String failureGaugeName = "fault.detector.aggregate.failure.rate." + serviceId;
        Gauge failureGaude = Gauge.builder(failureGaugeName, serviceAggregates, (aggregates) -> aggregates.getFailureRate())
                .tags(emptyList())
                .description("The value of the availability metric for the service " + serviceId)
                .baseUnit("value")
                .register(registry);
        aggregatesMetricsList.add(failureGaude);

        String operCountGaugeName = "fault.detector.aggregate.operations.count." + serviceId;
        Gauge operCountGaude = Gauge.builder(operCountGaugeName, serviceAggregates, (aggregates) -> aggregates.getOperationsCount())
                .tags(emptyList())
                .description("The value of operations count for the service " + serviceId)
                .baseUnit("value")
                .register(registry);
        aggregatesMetricsList.add(operCountGaude);

        String successOperCountGaugeName = "fault.detector.aggregate.success.operations.count." + serviceId;
        Gauge successOperCountGaude = Gauge.builder(successOperCountGaugeName, serviceAggregates, (aggregates) -> aggregates.getSuccessOperationsCount())
                .tags(emptyList())
                .description("The value of success operations count for the service " + serviceId)
                .baseUnit("value")
                .register(registry);
        aggregatesMetricsList.add(successOperCountGaude);

        String errorOperCountGaugeName = "fault.detector.aggregate.error.operations.count." + serviceId;
        Gauge errorOperCountGaude = Gauge.builder(errorOperCountGaugeName, serviceAggregates, (aggregates) -> aggregates.getErrorOperationsCount())
                .tags(emptyList())
                .description("The value of error operations count for the service " + serviceId)
                .baseUnit("value")
                .register(registry);
        aggregatesMetricsList.add(errorOperCountGaude);

        String overtimeOperCountGaugeName = "fault.detector.aggregate.error.operations.count." + serviceId;
        Gauge overtimeOperCountGaude = Gauge.builder(overtimeOperCountGaugeName, serviceAggregates, (aggregates) -> aggregates.getErrorOperationsCount())
                .tags(emptyList())
                .description("The value of overtime operations count for the service " + serviceId)
                .baseUnit("value")
                .register(registry);
        aggregatesMetricsList.add(overtimeOperCountGaude);

        aggregatesMetricsMap.put(serviceId, aggregatesMetricsList);
        log.info("Add gauge metrics for the service {}", serviceId);
    }

    public void removeAggregatesMetrics(String serviceId) {
        aggregatesMetricsMap.remove(serviceId);
        log.info("Remove gauge metrics for the service {}", serviceId);
    }

}
