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

    private final ServiceSettings serviceSettings;

    private static final String GAUGE_PREFEX = "1fd.aggregates.";

    private static final String FAILURE_GAUGE_NAME = GAUGE_PREFEX + "failure.rate_";

    private static final String OPER_COUNT_GAUGE_NAME = GAUGE_PREFEX + "operations.count_";

    private static final String SUCCESS_OPER_COUNT_GAUGE_NAME = GAUGE_PREFEX + "success.count_";

    private static final String ERROR_OPER_COUNT_GAUGE_NAME = GAUGE_PREFEX + "error.count_";

    private static final String OVERTIME_OPER_COUNT_GAUGE_NAME = GAUGE_PREFEX + "overtime.count_";

    private static final String CONFIG_SLIDING_WINDOW_GAUGE_NAME = GAUGE_PREFEX + "config.slidingWindow_";

    private static final String CONFIG_OPERATION_TIME_LIMIT_GAUGE_NAME = GAUGE_PREFEX + "config.operationTimeLimit_";

    private static final String CONFIG_PRE_AGGREGATION_SIZE_GAUGE_NAME = GAUGE_PREFEX + "config.preAggregationSize_";

    private static final String BASE_UNIT = "value";

    private static final String TIME_UNIT = "ms";

    private static final String GAUGE_LOG_PATTERN = "Gauge {} was added";

    @Override
    public void bindTo(MeterRegistry registry) {
        List<Meter.Id> meterIds = new ArrayList<>();
        meterIds.add(registerFailureRateMetrics(registry));
        meterIds.add(registerOperCountMetrics(registry));
        meterIds.add(registerSuccessOperCountMetrics(registry));
        meterIds.add(registerErrorOperCountMetrics(registry));
        meterIds.add(registerOvertimeOperCountMetrics(registry));
        meterIds.add(registerConfigSlidingWindow(registry));
        meterIds.add(registerConfigOperationTimeLimit(registry));
        meterIds.add(registerConfigPreAggregationSize(registry));
        serviceMetersMap.put(serviceId, meterIds);
        List<String> metersList = registry.getMeters().stream()
                .map(meter -> meter.getId() == null ? "Empty" : meter.getId().getName())
                .collect(Collectors.toList());
        log.info("Metric registry after adding gauges for the service {}: {}", serviceId, metersList);
    }

    private Meter.Id registerFailureRateMetrics(MeterRegistry registry) {
        Gauge registerFailureRate = Gauge.builder(FAILURE_GAUGE_NAME + serviceId, serviceAggregates, ServiceAggregates::getFailureRate)
                .tags(emptyList())
                .description("The value of the availability metric for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .register(registry);

        log.info(GAUGE_LOG_PATTERN, FAILURE_GAUGE_NAME + serviceId);
        return registerFailureRate.getId();
    }

    private Meter.Id registerOperCountMetrics(MeterRegistry registry) {
        Gauge registerOperCount = Gauge.builder(OPER_COUNT_GAUGE_NAME + serviceId, serviceAggregates, ServiceAggregates::getOperationsCount)
                .tags(emptyList())
                .description("The value of operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .register(registry);

        log.info(GAUGE_LOG_PATTERN, OPER_COUNT_GAUGE_NAME + serviceId);
        return registerOperCount.getId();
    }

    private Meter.Id registerSuccessOperCountMetrics(MeterRegistry registry) {
        Gauge registerSuccessOperCount = Gauge.builder(SUCCESS_OPER_COUNT_GAUGE_NAME + serviceId, serviceAggregates, ServiceAggregates::getSuccessOperationsCount)
                .tags(emptyList())
                .description("The value of success operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .register(registry);

        log.info(GAUGE_LOG_PATTERN, SUCCESS_OPER_COUNT_GAUGE_NAME + serviceId);
        return registerSuccessOperCount.getId();
    }

    private Meter.Id registerErrorOperCountMetrics(MeterRegistry registry) {
        Gauge registerErrorOperCount = Gauge.builder(ERROR_OPER_COUNT_GAUGE_NAME + serviceId, serviceAggregates, ServiceAggregates::getErrorOperationsCount)
                .tags(emptyList())
                .description("The value of error operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .register(registry);

        log.info(GAUGE_LOG_PATTERN, ERROR_OPER_COUNT_GAUGE_NAME + serviceId);
        return registerErrorOperCount.getId();
    }

    private Meter.Id registerOvertimeOperCountMetrics(MeterRegistry registry) {
        Gauge registerOvertimeOperCount = Gauge.builder(OVERTIME_OPER_COUNT_GAUGE_NAME + serviceId, serviceAggregates, ServiceAggregates::getOvertimeOperationsCount)
                .tags(emptyList())
                .description("The value of overtime operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .register(registry);

        log.info(GAUGE_LOG_PATTERN, OVERTIME_OPER_COUNT_GAUGE_NAME + serviceId);
        return registerOvertimeOperCount.getId();
    }

    private Meter.Id registerConfigSlidingWindow(MeterRegistry registry) {
        Gauge registerConfigSlidingWindowSize =
                Gauge.builder(CONFIG_SLIDING_WINDOW_GAUGE_NAME + serviceId,
                              serviceSettings,
                              ServiceSettings::getSlidingWindow)
                        .tags(emptyList())
                        .description("The value of sliding window for the service " + serviceId)
                        .baseUnit(TIME_UNIT)
                        .register(registry);

        log.info(GAUGE_LOG_PATTERN, CONFIG_SLIDING_WINDOW_GAUGE_NAME + serviceId);
        return registerConfigSlidingWindowSize.getId();
    }

    private Meter.Id registerConfigOperationTimeLimit(MeterRegistry registry) {
        Gauge registerConfigOperationTimeLimitSize =
                Gauge.builder(CONFIG_OPERATION_TIME_LIMIT_GAUGE_NAME + serviceId,
                              serviceSettings,
                              ServiceSettings::getOperationTimeLimit)
                        .tags(emptyList())
                        .description("The size of operation time limit for the service " + serviceId)
                        .baseUnit(TIME_UNIT)
                        .register(registry);

        log.info(GAUGE_LOG_PATTERN, CONFIG_OPERATION_TIME_LIMIT_GAUGE_NAME + serviceId);
        return registerConfigOperationTimeLimitSize.getId();
    }

    private Meter.Id registerConfigPreAggregationSize(MeterRegistry registry) {
        Gauge registerConfigPreAggregationSize =
                Gauge.builder(CONFIG_PRE_AGGREGATION_SIZE_GAUGE_NAME + serviceId,
                              serviceSettings,
                              ServiceSettings::getPreAggregationSize)
                        .tags(emptyList())
                        .description("The value of pre-aggregation size for the service " + serviceId)
                        .baseUnit(TIME_UNIT)
                        .register(registry);

        log.info(GAUGE_LOG_PATTERN, CONFIG_PRE_AGGREGATION_SIZE_GAUGE_NAME + serviceId);
        return registerConfigPreAggregationSize.getId();
    }

}