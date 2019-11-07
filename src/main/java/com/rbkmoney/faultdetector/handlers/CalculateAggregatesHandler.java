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

    private final Map<String, ServiceSettings> serviceSettingsMap;

    @Override
    public void handle(String serviceId) {
        log.info("Start processing the service statistics for service '{}'", serviceId);
        Deque<PreAggregates> preAggregatesDeque = servicePreAggregates.getPreAggregatesDeque(serviceId);

        if (preAggregatesDeque == null || preAggregatesDeque.isEmpty()) {
            return;
        }

        int weight = preAggregatesDeque.size();
        double failureRateSum = 0;
        long weightSum = 0;
        long aggregationTime = System.currentTimeMillis();
        log.info("Count of pre-aggregates for service '{}': {}. Time label: {}", serviceId,
                preAggregatesDeque.size(), aggregationTime);

        for (PreAggregates preAggregates : preAggregatesDeque) {
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
            weight--;
        }

        double failureRate = weightSum == 0 ? 0 : failureRateSum / weightSum;
        log.info("Failure rate for service {} with time label {} = {} (failure rate sum = {}, weight sum = {})",
                serviceId, aggregationTime, failureRate, failureRateSum, weightSum);

        ServiceAggregates serviceAggregates =
                getServiceAggregates(serviceId, failureRate, preAggregatesDeque, aggregationTime);
        serviceAggregatesMap.put(serviceId, serviceAggregates);

        ServiceSettings settings = serviceSettingsMap.get(serviceId);
        //serviceOperations.cleanUnusualOperations(serviceId, settings);
        servicePreAggregates.cleanPreAggregares(serviceId, settings);
        log.info("Processing the service statistics for service '{}' was finished", serviceId);
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

        serviceAggregates.setOperationsCount(lastPreAggregates.getOperationsCount());
        serviceAggregates.setRunningOperationsCount(lastPreAggregates.getRunningOperationsCount());
        serviceAggregates.setErrorOperationsCount(lastPreAggregates.getErrorOperationsCount());
        serviceAggregates.setSuccessOperationsCount(lastPreAggregates.getSuccessOperationsCount());
        serviceAggregates.setOvertimeOperationsCount(lastPreAggregates.getOvertimeOperationsCount());

        log.info("Aggregates for service id '{}': {}", serviceId, serviceAggregates);
        return serviceAggregates;
    }

}
