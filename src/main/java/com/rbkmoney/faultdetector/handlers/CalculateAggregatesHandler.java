package com.rbkmoney.faultdetector.handlers;

import com.rbkmoney.faultdetector.data.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalculateAggregatesHandler implements Handler<String> {

    private final Map<String, ServiceAggregates> serviceAggregatesMap;

    private final ServicePreAggregates servicePreAggregates;

    private final Map<String, ServiceSettings> serviceConfigMap;

    @Override
    public void handle(String serviceId) {
        log.info("Start processing the service statistics for service '{}'", serviceId);
        Deque<PreAggregates> preAggregatesDeque = servicePreAggregates.getPreAggregatesSet(serviceId);

        if (preAggregatesDeque == null || preAggregatesDeque.isEmpty()) {
            return;
        }

        Deque<PreAggregates> dequeBySettings =
                getPreAggregatesDequeBySettings(preAggregatesDeque, serviceConfigMap.get(serviceId));

        int weight = dequeBySettings.size();
        double failureRateSum = 0;
        long weightSum = 0;
        long aggregationTime = System.currentTimeMillis();
        log.info("Count of pre-aggregates for service '{}': {}. Time label: {}", serviceId,
                dequeBySettings.size(), aggregationTime);

        for (PreAggregates preAggregates : dequeBySettings) {
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

        ServiceAggregates serviceAggregates = getServiceAggregates(serviceId, failureRate, dequeBySettings, aggregationTime);
        serviceAggregatesMap.put(serviceId, serviceAggregates);

        log.info("Processing the service statistics for service '{}' was finished", serviceId);
    }

    private Deque<PreAggregates> getPreAggregatesDequeBySettings(Deque<PreAggregates> preAggregatesDeque,
                                                                 ServiceSettings settings) {
        int preAggregationSize = settings.getPreAggregationSize();
        if (preAggregationSize == 1) {
            return preAggregatesDeque;
        }

        Deque<PreAggregates> newPreAggregatesDeque = new ConcurrentLinkedDeque<>();

        Iterator<PreAggregates> preAggregatesIterator = preAggregatesDeque.iterator();
        while (preAggregatesIterator.hasNext()) {
            PreAggregates newPreAggregates = preAggregatesIterator.next().copy();
            for (int i = 0; i < preAggregationSize - 1; i++) {
                if (preAggregatesIterator.hasNext()) {
                    PreAggregates nextPreAggregates = preAggregatesIterator.next();
                    newPreAggregates.setOperationsCount(
                            newPreAggregates.getOperationsCount() + nextPreAggregates.getOperationsCount());
                    newPreAggregates.setErrorOperationsCount(
                            newPreAggregates.getErrorOperationsCount() + nextPreAggregates.getErrorOperationsCount());
                    newPreAggregates.setSuccessOperationsCount(
                            newPreAggregates.getSuccessOperationsCount() + nextPreAggregates.getSuccessOperationsCount());
                    newPreAggregates.getOvertimeOperationsSet().addAll(newPreAggregates.getOvertimeOperationsSet());
                }
            }
            newPreAggregatesDeque.addLast(newPreAggregates);
        }
        return newPreAggregatesDeque;
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
