package dev.vality.faultdetector.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GaugeMetricsUtils {

    public static final String GAUGE_PREFEX = "fd.aggregates.";

    public static final String FAILURE_GAUGE_NAME = GAUGE_PREFEX + "failure.rate_";

    public static final String OPER_COUNT_GAUGE_NAME = GAUGE_PREFEX + "operations.count_";

    public static final String SUCCESS_OPER_COUNT_GAUGE_NAME = GAUGE_PREFEX + "success.count_";

    public static final String RUNNING_OPER_COUNT_GAUGE_NAME = GAUGE_PREFEX + "running.count_";

    public static final String ERROR_OPER_COUNT_GAUGE_NAME = GAUGE_PREFEX + "error.count_";

    public static final String OVERTIME_OPER_COUNT_GAUGE_NAME = GAUGE_PREFEX + "overtime.count_";

    public static final String OPERS_AVG_TIME_GAUGE_NAME = GAUGE_PREFEX + "operations.avg_";

    public static final String BASE_UNIT = "value";

    public static final String TIME_UNIT = "ms";

    public static final String OLD_SERVICE_PREFIX = "hellgate_service.adapter_availability.";

    public static final String NEW_SERVICE_PREFIX = "provider_id_";

    public static final String GAUGE_LOG_PATTERN = "Gauge {} was added";

}
