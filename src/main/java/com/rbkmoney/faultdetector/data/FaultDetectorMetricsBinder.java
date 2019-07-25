package com.rbkmoney.faultdetector.data;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Slf4j
@RequiredArgsConstructor
public class FaultDetectorMetricsBinder implements MeterBinder {

    private final ServiceAggregates serviceAggregates;

    private final String serviceId;

    private static final String GAUGE_PREFEX = ".aggregate.";

    private static final String FAILURE_GAUGE_NAME = GAUGE_PREFEX + "failure.rate";

    private static final String OPER_COUNT_GAUGE_NAME = GAUGE_PREFEX + "operations.count";

    private static final String SUCCESS_OPER_COUNT_GAUGE_NAME = GAUGE_PREFEX + "success.count";

    private static final String ERROR_OPER_COUNT_GAUGE_NAME = GAUGE_PREFEX + "error.count";

    private static final String OVERTIME_OPER_COUNT_GAUGE_NAME = GAUGE_PREFEX + "overtime.count";

    private static final String BASE_UNIT = "value";

    private static final String GAUGE_LOG_PATTERN = "Gauge {} was added";

    @Override
    public void bindTo(MeterRegistry registry) {
        registerFailureRateMetrics(registry);
        registerOperCountMetrics(registry);
        registerSuccessOperCountMetrics(registry);
        registerErrorOperCountMetrics(registry);
        registerOvertimeOperCountMetrics(registry);
        List<String> metersList = registry.getMeters().stream()
                .map(meter -> meter.getId() == null ? "Empty" : meter.getId().getName())
                .collect(Collectors.toList());
        log.info("Metric registry after adding gauges for the service {}: {}", serviceId, metersList);
    }

    private void registerFailureRateMetrics(MeterRegistry registry) {
        Gauge.builder(serviceId + FAILURE_GAUGE_NAME, serviceAggregates, ServiceAggregates::getFailureRate)
                .tags(emptyList())
                .description("The value of the availability metric for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .register(registry);
        log.info(GAUGE_LOG_PATTERN, serviceId + FAILURE_GAUGE_NAME);
    }

    private void registerOperCountMetrics(MeterRegistry registry) {
        Gauge.builder(serviceId + OPER_COUNT_GAUGE_NAME, serviceAggregates, ServiceAggregates::getOperationsCount)
                .tags(emptyList())
                .description("The value of operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .register(registry);
        log.info(GAUGE_LOG_PATTERN, serviceId + OPER_COUNT_GAUGE_NAME);
    }

    private void registerSuccessOperCountMetrics(MeterRegistry registry) {
        Gauge.builder(serviceId + SUCCESS_OPER_COUNT_GAUGE_NAME, serviceAggregates, ServiceAggregates::getSuccessOperationsCount)
                .tags(emptyList())
                .description("The value of success operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .register(registry);
        log.info(GAUGE_LOG_PATTERN, serviceId + SUCCESS_OPER_COUNT_GAUGE_NAME);
    }

    private void registerErrorOperCountMetrics(MeterRegistry registry) {
        Gauge.builder(serviceId + ERROR_OPER_COUNT_GAUGE_NAME, serviceAggregates, ServiceAggregates::getErrorOperationsCount)
                .tags(emptyList())
                .description("The value of error operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .register(registry);
        log.info(GAUGE_LOG_PATTERN, serviceId + ERROR_OPER_COUNT_GAUGE_NAME);
    }

    private void registerOvertimeOperCountMetrics(MeterRegistry registry) {
        Gauge.builder(serviceId + OVERTIME_OPER_COUNT_GAUGE_NAME, serviceAggregates, ServiceAggregates::getOvertimeOperationsCount)
                .tags(emptyList())
                .description("The value of overtime operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .register(registry);
        log.info(GAUGE_LOG_PATTERN, serviceId + OVERTIME_OPER_COUNT_GAUGE_NAME);
    }

}