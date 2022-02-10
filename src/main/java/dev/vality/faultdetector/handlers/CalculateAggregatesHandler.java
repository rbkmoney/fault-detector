package dev.vality.faultdetector.handlers;

import dev.vality.faultdetector.data.PreAggregates;
import dev.vality.faultdetector.data.ServiceAggregates;
import dev.vality.faultdetector.data.ServicePreAggregates;
import dev.vality.faultdetector.data.ServiceSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalculateAggregatesHandler implements Handler<String> {

    private final Map<String, ServiceAggregates> serviceAggregatesMap;

    private final ServicePreAggregates servicePreAggregates;

    private final Map<String, ServiceSettings> serviceSettingsMap;

    @Override
    public void handle(String serviceId) {
        log.debug("Start processing the service statistics for service '{}'", serviceId);
        Deque<PreAggregates> preAggregatesDeque = servicePreAggregates.getPreAggregatesDeque(serviceId);

        if (preAggregatesDeque == null || preAggregatesDeque.isEmpty()) {
            return;
        }

        int weight = preAggregatesDeque.size();
        double failureRateSum = 0;
        long weightSum = 0;
        long totalErrorOpersCount = 0;
        long totalSuccessOpersCount = 0;
        long totalSumOpersCount = 0;
        long aggregationTime = System.currentTimeMillis();
        log.debug("Count of pre-aggregates for service '{}': {}. Time label: {}", serviceId,
                preAggregatesDeque.size(), aggregationTime);

        for (PreAggregates preAggregates : preAggregatesDeque) {
            int overtimeOperationsCount = preAggregates.getOvertimeOperationsCount();
            int errorOperationsCount = preAggregates.getErrorOperationsCount();
            int failureOperationsCount = errorOperationsCount + overtimeOperationsCount;
            int totalOperationsCount = failureOperationsCount + preAggregates.getSuccessOperationsCount();
            double failureRate = totalOperationsCount == 0
                    ? 0
                    : ((double) failureOperationsCount / totalOperationsCount) * weight;
            failureRateSum += failureRate;
            weightSum += weight;
            totalErrorOpersCount += errorOperationsCount;
            totalSuccessOpersCount += preAggregates.getSuccessOperationsCount();
            totalSumOpersCount += totalOperationsCount;

            log.debug("Step pre-aggregation {} for the service '{}'. Params: overtimeOperationsCount - {}, " +
                            "errorOperationsCount - {}, operationsCount - {}, failureRate - {}, failureRateSum - {}, " +
                            "weightSum - {}. Time label: {} ", weight, serviceId, overtimeOperationsCount,
                    errorOperationsCount, preAggregates.getOperationsCount(), failureRate, failureRateSum, weightSum,
                    aggregationTime);
            weight--;
        }

        double failureRate = weightSum == 0 ? 0 : failureRateSum / weightSum;
        log.debug("Failure rate for service {} with time label {} = {} (failure rate sum = {}, weight sum = {}, " +
                        "total operations count for sliding window {}," +
                        " total success operations count for sliding window {}, " +
                        "total error operations count for sliding window {})",
                serviceId, aggregationTime, failureRate, failureRateSum, weightSum, totalSumOpersCount,
                totalSuccessOpersCount, totalErrorOpersCount);

        ServiceAggregates serviceAggregates =
                getServiceAggregates(serviceId, failureRate, preAggregatesDeque, aggregationTime);
        serviceAggregatesMap.put(serviceId, serviceAggregates);

        ServiceSettings settings = serviceSettingsMap.get(serviceId);
        servicePreAggregates.cleanPreAggregares(serviceId, settings);
        //clearUnusedAggregates();
        log.debug("Processing the service statistics for service '{}' was finished", serviceId);
    }

    private ServiceAggregates getServiceAggregates(String serviceId,
                                                   double failureRate,
                                                   Deque<PreAggregates> preAggregatesDeque,
                                                   long aggregationTime) {
        ServiceAggregates serviceAggregates = new ServiceAggregates();
        serviceAggregates.setServiceId(serviceId);
        serviceAggregates.setAggregateTime(aggregationTime);
        serviceAggregates.setFailureRate(failureRate);

        PreAggregates lastPreAggregates = preAggregatesDeque.stream()
                .max(Comparator.comparingLong(PreAggregates::getAggregationTime))
                .orElse(new PreAggregates());

        serviceAggregates.setOperationsCount(new AtomicLong(lastPreAggregates.getOperationsCount()));
        serviceAggregates.setRunningOperationsCount(new AtomicLong(lastPreAggregates.getRunningOperationsCount()));
        serviceAggregates.setErrorOperationsCount(new AtomicLong(lastPreAggregates.getErrorOperationsCount()));
        serviceAggregates.setSuccessOperationsCount(new AtomicLong(lastPreAggregates.getSuccessOperationsCount()));
        serviceAggregates.setOvertimeOperationsCount(new AtomicLong(lastPreAggregates.getOvertimeOperationsCount()));
        serviceAggregates.setOperationsAvgTime(new AtomicLong((long) lastPreAggregates.getCompleteOperationsAvgTime()));

        log.debug("Last aggregates for service id '{}' by aggregation time {}: {} (activity statistics for last " +
                "pre-aggregates)", serviceId, aggregationTime, serviceAggregates);
        return serviceAggregates;
    }

    private void clearUnusedAggregates() {
        for (String serviceId : serviceAggregatesMap.keySet()) {
            ServiceSettings serviceSettings = serviceSettingsMap.get(serviceId);
            ServiceAggregates serviceAggregates = serviceAggregatesMap.get(serviceId);
            if (serviceSettings != null && serviceAggregates != null) {
                long slidingWindow = serviceSettings.getSlidingWindow();
                if (System.currentTimeMillis() - serviceAggregates.getAggregateTime() > slidingWindow) {
                    serviceAggregatesMap.remove(serviceId);
                }
            }
        }
    }

}
