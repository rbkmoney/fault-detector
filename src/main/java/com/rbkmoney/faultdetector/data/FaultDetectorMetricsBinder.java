package com.rbkmoney.faultdetector.data;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Slf4j
@RequiredArgsConstructor
public class FaultDetectorMetricsBinder implements MeterBinder {

    private final ServiceAggregates serviceAggregates;

    private final String serviceId;

    private final Map<String, List<Meter.Id>> serviceMetersMap;

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
        List<Meter.Id> meterIds = new ArrayList<>();
        meterIds.add(registerFailureRateMetrics(registry));
        meterIds.add(registerOperCountMetrics(registry));
        meterIds.add(registerSuccessOperCountMetrics(registry));
        meterIds.add(registerErrorOperCountMetrics(registry));
        meterIds.add(registerOvertimeOperCountMetrics(registry));
        serviceMetersMap.put(serviceId, meterIds);
        List<String> metersList = registry.getMeters().stream()
                .map(meter -> meter.getId() == null ? "Empty" : meter.getId().getName())
                .collect(Collectors.toList());
        log.info("Metric registry after adding gauges for the service {}: {}", serviceId, metersList);
    }

    private Meter.Id registerFailureRateMetrics(MeterRegistry registry) {
        Gauge registerFailureRate = Gauge.builder(serviceId + FAILURE_GAUGE_NAME, serviceAggregates, ServiceAggregates::getFailureRate)
                .tags(emptyList())
                .description("The value of the availability metric for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .register(registry);

        log.info(GAUGE_LOG_PATTERN, serviceId + FAILURE_GAUGE_NAME);
        return registerFailureRate.getId();
    }

    private Meter.Id registerOperCountMetrics(MeterRegistry registry) {
        Gauge registerOperCount = Gauge.builder(serviceId + OPER_COUNT_GAUGE_NAME, serviceAggregates, ServiceAggregates::getOperationsCount)
                .tags(emptyList())
                .description("The value of operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .register(registry);

        log.info(GAUGE_LOG_PATTERN, serviceId + OPER_COUNT_GAUGE_NAME);
        return registerOperCount.getId();
    }

    private Meter.Id registerSuccessOperCountMetrics(MeterRegistry registry) {
        Gauge registerSuccessOperCount = Gauge.builder(serviceId + SUCCESS_OPER_COUNT_GAUGE_NAME, serviceAggregates, ServiceAggregates::getSuccessOperationsCount)
                .tags(emptyList())
                .description("The value of success operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .register(registry);

        log.info(GAUGE_LOG_PATTERN, serviceId + SUCCESS_OPER_COUNT_GAUGE_NAME);
        return registerSuccessOperCount.getId();
    }

    private Meter.Id registerErrorOperCountMetrics(MeterRegistry registry) {
        Gauge registerErrorOperCount = Gauge.builder(serviceId + ERROR_OPER_COUNT_GAUGE_NAME, serviceAggregates, ServiceAggregates::getErrorOperationsCount)
                .tags(emptyList())
                .description("The value of error operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .register(registry);

        log.info(GAUGE_LOG_PATTERN, serviceId + ERROR_OPER_COUNT_GAUGE_NAME);
        return registerErrorOperCount.getId();
    }

    private Meter.Id registerOvertimeOperCountMetrics(MeterRegistry registry) {
        Gauge registerOvertimeOperCount = Gauge.builder(serviceId + OVERTIME_OPER_COUNT_GAUGE_NAME, serviceAggregates, ServiceAggregates::getOvertimeOperationsCount)
                .tags(emptyList())
                .description("The value of overtime operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .register(registry);

        log.info(GAUGE_LOG_PATTERN, serviceId + OVERTIME_OPER_COUNT_GAUGE_NAME);
        return registerOvertimeOperCount.getId();
    }

}