package dev.vality.faultdetector.binders;

import dev.vality.faultdetector.data.ServiceAggregates;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.vality.faultdetector.utils.GaugeMetricsUtils.*;
import static java.util.Collections.emptyList;

@Slf4j
@RequiredArgsConstructor
public class FaultDetectorMetricsBinder implements MeterBinder {

    private final Map<String, ServiceAggregates> aggregatesMap;

    private final String serviceId;

    private static final List<Tag> TAGS = getTags();

    @Override
    public void bindTo(MeterRegistry registry) {
        registerFailureRateMetrics(registry);
        registerErrorOperCountMetrics(registry);
        registerOperCountMetrics(registry);
        registerSuccessOperCountMetrics(registry);
        registerRunningOperCountMetrics(registry);
        registerOvertimeOperCountMetrics(registry);
        registerCompleteOpersAvgTimeMetrics(registry);
        log.debug("Metric registry after adding gauges for the service {}", serviceId);
    }

    private void registerFailureRateMetrics(MeterRegistry registry) {
        Gauge.builder(FAILURE_GAUGE_NAME + replacePrefix(serviceId),
                        aggregatesMap,
                        map -> map.get(serviceId) == null
                                ? null
                                : map.get(serviceId).getFailureRate())
                .description("The value of the availability metric for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .tags(TAGS)
                .register(registry);

        log.debug(GAUGE_LOG_PATTERN, FAILURE_GAUGE_NAME + serviceId);
    }

    private void registerOperCountMetrics(MeterRegistry registry) {
        Gauge.builder(OPER_COUNT_GAUGE_NAME + replacePrefix(serviceId),
                        aggregatesMap,
                        map -> map.get(serviceId) == null
                                ? null
                                : map.get(serviceId).getOperationsCount().longValue())
                .description("The value of operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .tags(TAGS)
                .register(registry);

        log.debug(GAUGE_LOG_PATTERN, OPER_COUNT_GAUGE_NAME + serviceId);
    }

    private void registerSuccessOperCountMetrics(MeterRegistry registry) {
        Gauge.builder(SUCCESS_OPER_COUNT_GAUGE_NAME + replacePrefix(serviceId),
                        aggregatesMap,
                        map -> map.get(serviceId) == null
                                ? null
                                : map.get(serviceId).getSuccessOperationsCount().longValue())
                .tags(emptyList())
                .description("The value of success operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .tags(TAGS)
                .strongReference(true)
                .register(registry);

        log.debug(GAUGE_LOG_PATTERN, SUCCESS_OPER_COUNT_GAUGE_NAME + serviceId);
    }

    private void registerRunningOperCountMetrics(MeterRegistry registry) {
        Gauge.builder(RUNNING_OPER_COUNT_GAUGE_NAME + replacePrefix(serviceId),
                        aggregatesMap,
                        map -> map.get(serviceId) == null
                                ? null
                                : map.get(serviceId).getRunningOperationsCount().longValue())
                .tags(emptyList())
                .description("The value of running operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .tags(TAGS)
                .register(registry);

        log.debug(GAUGE_LOG_PATTERN, RUNNING_OPER_COUNT_GAUGE_NAME + serviceId);
    }

    private void registerErrorOperCountMetrics(MeterRegistry registry) {
        Gauge.builder(ERROR_OPER_COUNT_GAUGE_NAME + replacePrefix(serviceId),
                        aggregatesMap,
                        map -> map.get(serviceId) == null
                                ? null
                                : map.get(serviceId).getErrorOperationsCount().longValue())
                .tags(emptyList())
                .description("The value of error operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .tags(TAGS)
                .strongReference(true)
                .register(registry);

        log.debug(GAUGE_LOG_PATTERN, ERROR_OPER_COUNT_GAUGE_NAME + serviceId);
    }

    private void registerOvertimeOperCountMetrics(MeterRegistry registry) {
        Gauge.builder(OVERTIME_OPER_COUNT_GAUGE_NAME + replacePrefix(serviceId),
                        aggregatesMap,
                        map -> map.get(serviceId) == null
                                ? null
                                : map.get(serviceId).getOvertimeOperationsCount().longValue())
                .tags(emptyList())
                .description("The value of overtime operations count for the service " + serviceId)
                .baseUnit(BASE_UNIT)
                .tags(TAGS)
                .register(registry);

        log.debug(GAUGE_LOG_PATTERN, OVERTIME_OPER_COUNT_GAUGE_NAME + serviceId);
    }

    private void registerCompleteOpersAvgTimeMetrics(MeterRegistry registry) {
        Gauge.builder(OPERS_AVG_TIME_GAUGE_NAME + replacePrefix(serviceId),
                        aggregatesMap,
                        map -> map.get(serviceId) == null
                                ? null
                                : map.get(serviceId).getOperationsAvgTime().longValue())
                .tags(emptyList())
                .description("The value of avg operations time for the service " + serviceId)
                .baseUnit(TIME_UNIT)
                .tags(TAGS)
                .register(registry);

        log.debug(GAUGE_LOG_PATTERN, OPERS_AVG_TIME_GAUGE_NAME + serviceId);
    }

    private static String replacePrefix(String serviceId) {
        return serviceId.replace(OLD_SERVICE_PREFIX, NEW_SERVICE_PREFIX);
    }

    private static List<Tag> getTags() {
        List<Tag> tags = new ArrayList<>();
        tags.add(new ImmutableTag("application", "fault-detector"));
        return tags;
    }

}