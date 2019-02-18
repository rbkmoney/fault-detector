package com.rbkmoney.faultdetector.handlers;

import com.rbkmoney.faultdetector.data.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrepareStatisticsHandler implements Handler<String> {

    private final Map<String, ServiceAggregates> serviceAggregatesMap;

    private final Map<String, Map<String, ServiceEvent>> serviceEventMap;

    private final Map<String, ServiceSettings> serviceConfigMap;

    @Value("${operations.lifetime}")
    private long lifetime;

    @Value("${operations.delta}")
    private int delta;

    @Override
    public void handle(String serviceId) throws Exception {
        log.debug("Start processing the service statistics for service {}", serviceId);

        ServiceSettings serviceSettings = serviceConfigMap.get(serviceId);
        if (serviceSettings == null) {
            throw new Exception("Config for service " + serviceId + " not found");
        }
        Collection<ServiceEvent> events = serviceEventMap.get(serviceId).values();
        log.debug("Total count of events for service {}: {}", serviceId, events.size());

        ServiceAggregates serviceAggregates = prepareFailureRate(serviceId, events, serviceSettings);
        serviceAggregatesMap.put(serviceId, serviceAggregates);
        log.debug("Processing the service statistics for service {} was finished", serviceId);
    }

    private ServiceAggregates prepareFailureRate(String serviceId,
                                                 Collection<ServiceEvent> serviceEvents,
                                                 ServiceSettings serviceSettings) {
        double operationTimeout = getOperationTimeout(serviceSettings, serviceEvents);

        long currentTime = System.currentTimeMillis();
        long slidingWindow = serviceSettings.getSlidingWindow();
        double remainingTime = slidingWindow - operationTimeout;
        List<ProcessedServiceEvent> processedServiceEventList = new ArrayList<>();

        for (ServiceEvent serviceEvent : serviceEvents) {

            long endTime = serviceEvent.getEndTime() == null ? currentTime : serviceEvent.getEndTime();
            long currentOperationTimeout = endTime - serviceEvent.getStartTime();

            if (serviceEvent.isError()) {
                ProcessedServiceEvent processErrorEvent =
                        processErrorEvent(operationTimeout, currentOperationTimeout, remainingTime);
                processedServiceEventList.add(processErrorEvent);
            } else if (currentOperationTimeout > operationTimeout) {
                double overrunTime = currentOperationTimeout - operationTimeout;
                ProcessedServiceEvent processedServiceEvent =
                        processTimeoutEvent(serviceEvent, overrunTime, remainingTime, slidingWindow, currentTime);
                processedServiceEventList.add(processedServiceEvent);
            }
        }

        double failWeightSum = getServiceEventsWeightSum(processedServiceEventList);
        double failRate = failWeightSum / serviceEvents.size();

        return fillServiceAggregates(serviceId, serviceEvents, processedServiceEventList, failRate);
    }

    private ServiceAggregates fillServiceAggregates(String serviceId,
                                                    Collection<ServiceEvent> serviceEvents,
                                                    List<ProcessedServiceEvent> processedServiceEventList,
                                                    double failRate) {
        ServiceAggregates aggregates = new ServiceAggregates();
        aggregates.setServiceId(serviceId);
        aggregates.setOperationsCount(serviceEvents.size());
        aggregates.setErrorOperationsCount(getServiceEventListSize(processedServiceEventList, EventType.FAIL));
        aggregates.setOvertimeOperationsCount(getServiceEventListSize(processedServiceEventList, EventType.OVERTIME));
        aggregates.setHoveringOpertionsCount(getServiceEventListSize(processedServiceEventList, EventType.HOVERING));
        aggregates.setFailureRate(failRate);
        return aggregates;
    }

    private ProcessedServiceEvent processErrorEvent(double operationTimeout,
                                                    long currentOperationTimeout,
                                                    double remainingTime) {
        ProcessedServiceEvent processedServiceEvent = new ProcessedServiceEvent();
        double eventWeight;
        if (currentOperationTimeout > operationTimeout) {
            double overrunTime = currentOperationTimeout - operationTimeout;
            eventWeight = 1 - overrunTime / remainingTime;
        } else {
            eventWeight = 1;
        }
        processedServiceEvent.setWeight(eventWeight);
        processedServiceEvent.setEventType(EventType.FAIL);
        return processedServiceEvent;
    }

    private ProcessedServiceEvent processTimeoutEvent(ServiceEvent serviceEvent,
                                                      double overrunTime,
                                                      double remainingTime,
                                                      long slidingWindow,
                                                      long currentTime) {
        ProcessedServiceEvent processedServiceEvent = new ProcessedServiceEvent();
        if (serviceEvent.getEndTime() == null) {
            // Рассчет веса для операции, которая превысила время выполнения и еще выполняется
            double eventWeight = overrunTime / remainingTime;
            processedServiceEvent.setWeight(eventWeight);
            processedServiceEvent.setEventType(EventType.HOVERING);
        } else {
            // Рассчет веса для успешной операции, которая превысила лимит
            long remainingLifetime = slidingWindow - (currentTime - serviceEvent.getStartTime());
            double eventWeight = (overrunTime / remainingTime) * (1 - remainingLifetime / slidingWindow);
            processedServiceEvent.setWeight(eventWeight);
            processedServiceEvent.setEventType(EventType.OVERTIME);
        }
        return processedServiceEvent;
    }

    private double getOperationTimeout(ServiceSettings serviceSettings,
                                       Collection<ServiceEvent> serviceEvents) {
        if (serviceSettings.getHoveringOperationErrorDelay() == null) {
            LongSummaryStatistics timeStatistics = serviceEvents.stream()
                    .filter(event -> event.getEndTime() != null && !event.isError())
                    .collect(Collectors.summarizingLong(event -> event.getEndTime() - event.getStartTime()));
            double deltaValue = delta / 100;
            return timeStatistics.getAverage() + timeStatistics.getAverage() * deltaValue;
        } else {
            return serviceSettings.getHoveringOperationErrorDelay();
        }
    }

    private long getServiceEventListSize(List<ProcessedServiceEvent> serviceEventList, EventType type) {
         return serviceEventList.stream()
                 .filter(event -> event.getEventType() == type)
                 .count();
    }

    private double getServiceEventsWeightSum(List<ProcessedServiceEvent> processedServiceEventList) {
        DoubleSummaryStatistics summaryStatistics = processedServiceEventList.stream()
                .map(event -> event.getWeight())
                .collect(Collectors.summarizingDouble(Double::doubleValue));

        return summaryStatistics.getSum();
    }

}
