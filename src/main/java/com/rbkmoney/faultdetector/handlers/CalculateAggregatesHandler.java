package com.rbkmoney.faultdetector.handlers;

import com.rbkmoney.faultdetector.data.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalculateAggregatesHandler implements Handler<String> {

    private final Map<String, ServiceAggregates> serviceAggregatesMap;

    private final ServicePreAggregates servicePreAggregates;

    @Override
    public void handle(String serviceId) {
        log.debug("Start processing the service statistics for service {}", serviceId);

        Set<PreAggregates> preAggregatesSet = servicePreAggregates.getPreAggregatesSet(serviceId);
        log.debug("Count of pre-aggregates for service {}", preAggregatesSet.size());

        if (preAggregatesSet == null || preAggregatesSet.isEmpty()) {
            return;
        }

        int weight = 0;
        double failureRateSum = 0;
        long weightSum = 0;
        for (PreAggregates preAggregates : preAggregatesSet) {
            weight++;
            int overtimeOperationsCount = preAggregates.getOvertimeOperationsCount();
            int errorOperationsCount = preAggregates.getErrorOperationsCount();
            int failureOperationsCount = errorOperationsCount + overtimeOperationsCount;
            failureRateSum += ((double) failureOperationsCount / preAggregates.getOperationsCount()) * weight;
            weightSum += weight;
            if (failureOperationsCount > 0) {
                log.debug("For the service '{}' in the pre-aggregate '{}': overtimeOperationsCount - {}, " +
                                "errorOperationsCount - {}, total - {}. A weight of the failure rate: {}",
                        serviceId, preAggregates.getAggregationTime(), overtimeOperationsCount,
                        errorOperationsCount, preAggregates.getOperationsCount(), weight);
            }
        }

        double failureRate = weightSum == 0 ? 0 : failureRateSum / weightSum;

        ServiceAggregates serviceAggregates = getServiceAggregates(serviceId, failureRate, preAggregatesSet);
        serviceAggregatesMap.put(serviceId, serviceAggregates);
        log.info("Processing the service statistics for service {} was finished", serviceId);
    }

    private ServiceAggregates getServiceAggregates(String serviceId,
                                                   double failureRate,
                                                   Set<PreAggregates> preAggregatesSet) {
        ServiceAggregates serviceAggregates = new ServiceAggregates();
        serviceAggregates.setServiceId(serviceId);
        serviceAggregates.setAggregateTime(System.currentTimeMillis());
        serviceAggregates.setFailureRate(failureRate);

        long totalSuccessOpers = preAggregatesSet.stream()
                .mapToLong(aggr -> aggr.getSuccessOperationsCount())
                .sum();
        serviceAggregates.setSuccessOperationsCount(totalSuccessOpers);

        long totalErrorOpers = preAggregatesSet.stream()
                .mapToLong(aggr -> aggr.getErrorOperationsCount())
                .sum();
        serviceAggregates.setErrorOperationsCount(totalErrorOpers);

        PreAggregates lastPreAggregates = preAggregatesSet.stream()
                .max(Comparator.comparingLong(agg -> agg.getAggregationTime()))
                .orElse(new PreAggregates());
        serviceAggregates.setOperationsCount(lastPreAggregates.getRunningOperationsCount() +
                lastPreAggregates.getOvertimeOperationsCount() + totalSuccessOpers + totalErrorOpers);
        log.info("Aggregates for service {}: {}", serviceId, serviceAggregates);
        return serviceAggregates;
    }

}
