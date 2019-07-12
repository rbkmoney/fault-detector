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

    private static final String GAUGE_PREFEX = "fault.detector.aggregate.";

    private static final String FAILURE_GAUGE_NAME = GAUGE_PREFEX + "failure.rate.";

    private static final String OPER_COUNT_GAUGE_NAME = GAUGE_PREFEX + "operations.count.";

    private static final String SUCCESS_OPER_COUNT_GAUGE_NAME = GAUGE_PREFEX + "success.count.";

    private static final String ERROR_OPER_COUNT_GAUGE_NAME = GAUGE_PREFEX + "error.count.";

    private static final String OVERTIME_OPER_COUNT_GAUGE_NAME = GAUGE_PREFEX + "overtime.count.";

    private static final String BASE_UNIT = "value";

    public void addAggregatesMetrics(String serviceId) {
        List<Gauge> aggregatesMetricsList = new CopyOnWriteArrayList<>();
        ServiceAggregates serviceAggregates = aggregatesMap.get(serviceId);

        Gauge failureGaude = Gauge.builder(FAILURE_GAUGE_NAME, serviceAggregates, ServiceAggregates::getFailureRate)
                .tags(emptyList())
                .description("The value of the availability metric for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .register(registry);
        aggregatesMetricsList.add(failureGaude);

        Gauge operCountGaude = Gauge.builder(OPER_COUNT_GAUGE_NAME, serviceAggregates, ServiceAggregates::getOperationsCount)
                .tags(emptyList())
                .description("The value of operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .register(registry);
        aggregatesMetricsList.add(operCountGaude);

        Gauge successOperCountGaude = Gauge.builder(SUCCESS_OPER_COUNT_GAUGE_NAME, serviceAggregates, ServiceAggregates::getSuccessOperationsCount)
                .tags(emptyList())
                .description("The value of success operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .register(registry);
        aggregatesMetricsList.add(successOperCountGaude);

        Gauge errorOperCountGaude = Gauge.builder(ERROR_OPER_COUNT_GAUGE_NAME, serviceAggregates, ServiceAggregates::getErrorOperationsCount)
                .tags(emptyList())
                .description("The value of error operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .register(registry);
        aggregatesMetricsList.add(errorOperCountGaude);

        Gauge overtimeOperCountGaude = Gauge.builder(OVERTIME_OPER_COUNT_GAUGE_NAME, serviceAggregates, (aggregates) -> aggregates.getErrorOperationsCount())
                .tags(emptyList())
                .description("The value of overtime operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
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
