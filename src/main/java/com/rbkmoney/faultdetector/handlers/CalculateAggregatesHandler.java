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
    public void handle(String serviceId) throws Exception {
        log.debug("Start processing the service statistics for service {}", serviceId);

        Set<PreAggregates> preAggregatesSet = servicePreAggregates.getPreAggregatesSet(serviceId);

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
            double failureOperationsCount = errorOperationsCount + overtimeOperationsCount;
            failureRateSum += (failureOperationsCount / preAggregates.getOperationsCount()) * weight;
            weightSum += weight;
        }

        double failureRate = failureRateSum / weightSum;
        ServiceAggregates serviceAggregates = getServiceAggregates(serviceId, failureRate, preAggregatesSet);
        serviceAggregatesMap.put(serviceId, serviceAggregates);
        log.debug("Processing the service statistics for service {} was finished", serviceId);
    }

    private ServiceAggregates getServiceAggregates(String serviceId,
                                                   double failureRate,
                                                   Set<PreAggregates> preAggregatesSet) {
        ServiceAggregates serviceAggregates = new ServiceAggregates();
        serviceAggregates.setServiceId(serviceId);
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
                .orElse(null);
        serviceAggregates.setOperationsCount(lastPreAggregates.getRunningOperationsCount() +
                lastPreAggregates.getOvertimeOperationsCount() + totalSuccessOpers + totalErrorOpers);
        return serviceAggregates;
    }

}
