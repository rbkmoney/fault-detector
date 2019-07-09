package com.rbkmoney.faultdetector.handlers;

import com.rbkmoney.faultdetector.data.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalculateAggregatesHandler implements Handler<String> {

    private final Map<String, ServiceAggregates> serviceAggregatesMap;

    private final ServicePreAggregates servicePreAggregates;

    @Override
    public void handle(String serviceId) {
        log.info("Start processing the service statistics for service '{}'", serviceId);
        Set<PreAggregates> preAggregatesSet = servicePreAggregates.getPreAggregatesSet(serviceId);

        if (preAggregatesSet == null || preAggregatesSet.isEmpty()) {
            return;
        }

        int weight = 0;
        double failureRateSum = 0;
        long weightSum = 0;
        long aggregationTime = System.currentTimeMillis();
        log.info("Count of pre-aggregates for service '{}': {}. Time label: {}", serviceId,
                preAggregatesSet.size(), aggregationTime);

        for (PreAggregates preAggregates : preAggregatesSet) {
            weight++;
            int overtimeOperationsCount = preAggregates.getOvertimeOperationsCount();
            int errorOperationsCount = preAggregates.getErrorOperationsCount();
            int failureOperationsCount = errorOperationsCount + overtimeOperationsCount;
            double failureRate = ((double) failureOperationsCount / preAggregates.getOperationsCount()) * weight;
            failureRateSum += failureRate;
            weightSum += weight;
            log.debug("Step pre-aggregation {} for the service '{}'. Params: overtimeOperationsCount - {}, " +
                            "errorOperationsCount - {}, operationsCount - {}, failureRate - {}, failureRateSum - {}, " +
                            "weightSum - {}. Time label: {} ", weight, serviceId, overtimeOperationsCount,
                    errorOperationsCount, preAggregates.getOperationsCount(), failureRate, failureRateSum, weightSum,
                    aggregationTime);
        }

        double failureRate = weightSum == 0 ? 0 : failureRateSum / weightSum;

        ServiceAggregates serviceAggregates = getServiceAggregates(serviceId, failureRate, preAggregatesSet, aggregationTime);
        serviceAggregatesMap.put(serviceId, serviceAggregates);
        log.info("Processing the service statistics for service '{}' was finished", serviceId);
    }

    private ServiceAggregates getServiceAggregates(String serviceId,
                                                   double failureRate,
                                                   Set<PreAggregates> preAggregatesSet,
                                                   long aggregationTime) {
        ServiceAggregates serviceAggregates = new ServiceAggregates();
        serviceAggregates.setServiceId(serviceId);
        serviceAggregates.setAggregateTime(aggregationTime);
        serviceAggregates.setFailureRate(failureRate);

        long totalSuccessOpers = preAggregatesSet.stream()
                .mapToLong(aggr -> aggr.getSuccessOperationsCount())
                .sum();
        serviceAggregates.setTotalSuccessOperationsCount(totalSuccessOpers);

        long totalErrorOpers = preAggregatesSet.stream()
                .mapToLong(aggr -> aggr.getErrorOperationsCount())
                .sum();
        serviceAggregates.setTotalErrorOperationsCount(totalErrorOpers);

        PreAggregates lastPreAggregates = preAggregatesSet.stream()
                .max(Comparator.comparingLong(agg -> agg.getAggregationTime()))
                .orElse(new PreAggregates());
        serviceAggregates.setTotalOperationsCount(lastPreAggregates.getRunningOperationsCount() +
                lastPreAggregates.getOvertimeOperationsCount() + totalSuccessOpers + totalErrorOpers);
        serviceAggregates.setOperationsCount(lastPreAggregates.getOperationsCount());
        serviceAggregates.setErrorOperationsCount(lastPreAggregates.getErrorOperationsCount());
        serviceAggregates.setSuccessOperationsCount(lastPreAggregates.getSuccessOperationsCount());
        serviceAggregates.setOvertimeOperationsCount(lastPreAggregates.getOvertimeOperationsCount());

        String delimiter = ", ";
        serviceAggregates.setOperationsCountProgressiveLine(getOperationsCountProgressiveLine(preAggregatesSet, delimiter));
        serviceAggregates.setErrorOperationsProgressiveLine(getErrorOperationsProgressiveLine(preAggregatesSet, delimiter));
        serviceAggregates.setOvertimeOperationsProgressiveLine(getOvertimeOperationsProgressiveLine(preAggregatesSet, delimiter));

        log.info("Aggregates for service id '{}': {}", serviceId, serviceAggregates);
        return serviceAggregates;
    }

    private String getOvertimeOperationsProgressiveLine(Set<PreAggregates> preAggregatesSet, String delimiter) {
        return preAggregatesSet.stream()
                .map(agg -> String.valueOf(agg.getOperationsCount()))
                .collect(Collectors.joining(delimiter));
    }

    private String getErrorOperationsProgressiveLine(Set<PreAggregates> preAggregatesSet, String delimiter) {
        return preAggregatesSet.stream()
                .map(agg -> String.valueOf(agg.getErrorOperationsCount()))
                .collect(Collectors.joining(delimiter));
    }

    private String getOperationsCountProgressiveLine(Set<PreAggregates> preAggregatesSet, String delimiter) {
        return preAggregatesSet.stream()
                .map(agg -> String.valueOf(agg.getOperationsCount()))
                .collect(Collectors.joining(delimiter));
    }

}
