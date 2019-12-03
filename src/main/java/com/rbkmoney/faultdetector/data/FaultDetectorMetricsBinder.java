package com.rbkmoney.faultdetector.data;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.rbkmoney.faultdetector.utils.GaugeMetricsUtils.*;
import static java.util.Collections.emptyList;

@Slf4j
@RequiredArgsConstructor
public class FaultDetectorMetricsBinder implements MeterBinder {

    private final Map<String, ServiceAggregates> aggregatesMap;

    private final Map<String, ServiceSettings> serviceConfigMap;

    private final Map<String, List<Gauge>> serviceMetersMap;

    private final String serviceId;

    @Override
    public void bindTo(MeterRegistry registry) {
        List<Gauge> meterIds = new ArrayList<>();
        meterIds.add(registerFailureRateMetrics(registry));
        meterIds.add(registerOperCountMetrics(registry));
        meterIds.add(registerSuccessOperCountMetrics(registry));
        meterIds.add(registerRunningOperCountMetrics(registry));
        meterIds.add(registerErrorOperCountMetrics(registry));
        meterIds.add(registerOvertimeOperCountMetrics(registry));
        meterIds.add(registerConfigSlidingWindow(registry));
        meterIds.add(registerConfigOperationTimeLimit(registry));
        meterIds.add(registerConfigPreAggregationSize(registry));
        meterIds.add(registerCompleteOpersAvgTimeMetrics(registry));
        serviceMetersMap.put(serviceId, meterIds);
        List<String> metersList = registry.getMeters().stream()
                .map(meter -> meter.getId() == null ? "Empty" : meter.getId().getName())
                .collect(Collectors.toList());
        log.info("Metric registry after adding gauges for the service {}: {}", serviceId, metersList);
    }

    private Gauge registerFailureRateMetrics(MeterRegistry registry) {
        Gauge registerFailureRate =
                Gauge.builder(FAILURE_GAUGE_NAME + replacePrefix(serviceId),
                        aggregatesMap,
                        map -> map.get(serviceId) == null ? null : map.get(serviceId).getFailureRate())
                .description("The value of the availability metric for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .strongReference(true)
                .register(registry);

        log.info(GAUGE_LOG_PATTERN, FAILURE_GAUGE_NAME + serviceId);
        return registerFailureRate;
    }

    private Gauge registerOperCountMetrics(MeterRegistry registry) {
        Gauge registerOperCount =
                Gauge.builder(OPER_COUNT_GAUGE_NAME + replacePrefix(serviceId),
                        aggregatesMap,
                        map -> map.get(serviceId) == null ? null : map.get(serviceId).getOperationsCount())
                .description("The value of operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .strongReference(true)
                .register(registry);

        log.info(GAUGE_LOG_PATTERN, OPER_COUNT_GAUGE_NAME + serviceId);
        return registerOperCount;
    }

    private Gauge registerSuccessOperCountMetrics(MeterRegistry registry) {
        Gauge registerSuccessOperCount =
                Gauge.builder(SUCCESS_OPER_COUNT_GAUGE_NAME + replacePrefix(serviceId),
                        aggregatesMap,
                        map -> map.get(serviceId) == null ? null : map.get(serviceId).getSuccessOperationsCount())
                .tags(emptyList())
                .description("The value of success operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .strongReference(true)
                .register(registry);

        log.info(GAUGE_LOG_PATTERN, SUCCESS_OPER_COUNT_GAUGE_NAME + serviceId);
        return registerSuccessOperCount;
    }

    private Gauge registerRunningOperCountMetrics(MeterRegistry registry) {
        Gauge registerRunningOperCount =
                Gauge.builder(RUNNING_OPER_COUNT_GAUGE_NAME + replacePrefix(serviceId),
                        aggregatesMap,
                        map -> map.get(serviceId) == null ? null : map.get(serviceId).getRunningOperationsCount())
                .tags(emptyList())
                .description("The value of running operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .strongReference(true)
                .register(registry);

        log.info(GAUGE_LOG_PATTERN, RUNNING_OPER_COUNT_GAUGE_NAME + serviceId);
        return registerRunningOperCount;
    }

    private Gauge registerErrorOperCountMetrics(MeterRegistry registry) {
        Gauge registerErrorOperCount =
                Gauge.builder(ERROR_OPER_COUNT_GAUGE_NAME + replacePrefix(serviceId),
                        aggregatesMap,
                        map -> map.get(serviceId) == null ? null : map.get(serviceId).getErrorOperationsCount())
                .tags(emptyList())
                .description("The value of error operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .strongReference(true)
                .register(registry);

        log.info(GAUGE_LOG_PATTERN, ERROR_OPER_COUNT_GAUGE_NAME + serviceId);
        return registerErrorOperCount;
    }

    private Gauge registerOvertimeOperCountMetrics(MeterRegistry registry) {
        Gauge registerOvertimeOperCount =
                Gauge.builder(OVERTIME_OPER_COUNT_GAUGE_NAME + replacePrefix(serviceId),
                        aggregatesMap,
                        map -> map.get(serviceId) == null ? null : map.get(serviceId).getOvertimeOperationsCount())
                .tags(emptyList())
                .description("The value of overtime operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .strongReference(true)
                .register(registry);

        log.info(GAUGE_LOG_PATTERN, OVERTIME_OPER_COUNT_GAUGE_NAME + serviceId);
        return registerOvertimeOperCount;
    }

    private Gauge registerCompleteOpersAvgTimeMetrics(MeterRegistry registry) {
        Gauge registerCompleteOpersAvgTimeMetrics =
                Gauge.builder(OPERS_AVG_TIME_GAUGE_NAME + replacePrefix(serviceId),
                        aggregatesMap,
                        map -> map.get(serviceId) == null ? null : map.get(serviceId).getOperationsAvgTime())
                .tags(emptyList())
                .description("The value of avg operations time for the service " + serviceId)
                .baseUnit(TIME_UNIT)
                .strongReference(false)
                .register(registry);

        log.info(GAUGE_LOG_PATTERN, OPERS_AVG_TIME_GAUGE_NAME + serviceId);
        return registerCompleteOpersAvgTimeMetrics;
    }

    private Gauge registerConfigSlidingWindow(MeterRegistry registry) {
        Gauge registerConfigSlidingWindowSize =
                Gauge.builder(CONFIG_SLIDING_WINDOW_GAUGE_NAME + replacePrefix(serviceId),
                              serviceConfigMap,
                              map -> map.get(serviceId) == null ? null : map.get(serviceId).getSlidingWindow())
                        .tags(emptyList())
                        .description("The value of sliding window for the service " + serviceId)
                        .baseUnit(TIME_UNIT)
                        .strongReference(false)
                        .register(registry);

        log.info(GAUGE_LOG_PATTERN, CONFIG_SLIDING_WINDOW_GAUGE_NAME + serviceId);
        return registerConfigSlidingWindowSize;
    }

    private Gauge registerConfigOperationTimeLimit(MeterRegistry registry) {
        Gauge registerConfigOperationTimeLimitSize =
                Gauge.builder(CONFIG_OPERATION_TIME_LIMIT_GAUGE_NAME + replacePrefix(serviceId),
                              serviceConfigMap,
                              map -> map.get(serviceId) == null ? null : map.get(serviceId).getOperationTimeLimit())
                        .tags(emptyList())
                        .description("The size of operation time limit for the service " + serviceId)
                        .baseUnit(TIME_UNIT)
                        .strongReference(false)
                        .register(registry);

        log.info(GAUGE_LOG_PATTERN, CONFIG_OPERATION_TIME_LIMIT_GAUGE_NAME + serviceId);
        return registerConfigOperationTimeLimitSize;
    }

    private Gauge registerConfigPreAggregationSize(MeterRegistry registry) {
        Gauge registerConfigPreAggregationSize =
                Gauge.builder(CONFIG_PRE_AGGREGATION_SIZE_GAUGE_NAME + replacePrefix(serviceId),
                              serviceConfigMap,
                              map -> map.get(serviceId) == null ? null : map.get(serviceId).getPreAggregationSize())
                        .tags(emptyList())
                        .description("The value of pre-aggregation size for the service " + serviceId)
                        .baseUnit(TIME_UNIT)
                        .strongReference(false)
                        .register(registry);

        log.info(GAUGE_LOG_PATTERN, CONFIG_PRE_AGGREGATION_SIZE_GAUGE_NAME + serviceId);
        return registerConfigPreAggregationSize;
    }

    private static String replacePrefix(String serviceId) {
        return serviceId.replace(OLD_SERVICE_PREFIX, NEW_SERVICE_PREFIX);
    }

}