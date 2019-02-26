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

    private final Map<String, Map<Long, ServicePreAggregates>> servicePreAggregatesMap;

    @Override
    public void handle(String serviceId) throws Exception {
        log.debug("Start processing the service statistics for service {}", serviceId);

        Map<Long, ServicePreAggregates> preAggregatesMap = servicePreAggregatesMap.get(serviceId);
        if (preAggregatesMap == null || preAggregatesMap.isEmpty()) {
            return;
        }

        List<Long> aggregatesKeys = preAggregatesMap.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        int weight = 0;
        double failureRateSum = 0;
        long weightSum = 0;
        for (Long aggregateKey : aggregatesKeys) {
            weight++;
            ServicePreAggregates preAggregates = preAggregatesMap.get(aggregateKey);
            int overtimeOperationsCount = preAggregates.getOvertimeOperationsCount();
            int errorOperationsCount = preAggregates.getErrorOperationsCount();
            double failureOperationsCount = errorOperationsCount + overtimeOperationsCount;
            failureRateSum += (failureOperationsCount / preAggregates.getOperationsCount()) * weight;
            weightSum += weight;
        }

        double failureRate = failureRateSum / weightSum;

        ServiceAggregates serviceAggregates = new ServiceAggregates();
        serviceAggregates.setServiceId(serviceId);
        serviceAggregates.setFailureRate(failureRate);

        long totalSuccessOpers = preAggregatesMap.values().stream()
                .mapToLong(aggr -> aggr.getSuccessOperationsCount())
                .sum();
        serviceAggregates.setSuccessOperationsCount(totalSuccessOpers);

        long totalErrorOpers = preAggregatesMap.values().stream()
                .mapToLong(aggr -> aggr.getErrorOperationsCount())
                .sum();
        serviceAggregates.setErrorOperationsCount(totalErrorOpers);

        ServicePreAggregates lastPreAggregates = preAggregatesMap.values().stream()
                .max(Comparator.comparingLong(agg -> agg.getAggregationTime()))
                .orElse(null);
        serviceAggregates.setOperationsCount(lastPreAggregates.getRunningOperationsCount() +
                lastPreAggregates.getOvertimeOperationsCount() + totalSuccessOpers + totalErrorOpers);

        serviceAggregatesMap.put(serviceId, serviceAggregates);
        log.debug("Processing the service statistics for service {} was finished", serviceId);
    }

}
