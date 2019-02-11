package com.rbkmoney.faultdetector.handlers;

import com.rbkmoney.faultdetector.data.ServiceAvailability;
import com.rbkmoney.faultdetector.data.ServiceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrepareStatisticsHandler implements Handler {

    private final Map<String, ServiceAvailability> availabilityMap;

    private final Map<String, Map<String, ServiceEvent>> serviceEventMap;
    // TODO: для каждого сервиса должна быть возможность перезадать этот параметр, т.к.
    //       время выполнения операции может существенно отличаться
    private long eventErrorTimeout = 30000;

    @Override
    public void handle(String serviceId) {
        log.debug("Start processing the service statistics for service {}", serviceId);
        ServiceAvailability availability = new ServiceAvailability();
        availability.setServiceId(serviceId);
        Collection<ServiceEvent> events = serviceEventMap.get(serviceId).values();
        log.debug("Total count of events for service {}: {}", serviceId, events.size());
        double successRate = prepareSuccessRate(events);
        log.debug("Success rate for service {}: {}", serviceId, successRate);
        availability.setSuccessRate(successRate);
        double timeoutRate = prepareTimeoutRate(events);
        log.debug("Timeout rate for service {}: {}", serviceId, timeoutRate);
        availability.setTimeoutRate(timeoutRate);
        availabilityMap.put(serviceId, availability);
        log.debug("Processing the service statistics for service {} was finished", serviceId);
    }

    private double prepareSuccessRate(Collection<ServiceEvent> events) {
        long currentTime = System.currentTimeMillis();

        List<ServiceEvent> errorServiceEvents = events.stream()
                .filter(event -> event.isError() || (currentTime - event.getStartTime()) > eventErrorTimeout)
                .collect(Collectors.toList());

        int totalEventCount = events.size();
        int errorEventCount = errorServiceEvents.size();
        // TODO: существует три типа операций: активные, завершенные и сбойные. Сейчас активные транзакции
        //       разделяются на группы согласно таймауту на операцию для сервиса. Возможно формула расчета
        //       должна быть немного иной. Например, по истечении определенного времени "вес" такой операции
        //       в общем результате уменьшается. Затем в зависимости от количества таких операций они или
        //       исключаются из статистики по сервису, либо остаются на все интересующее время
        return (double) (totalEventCount - errorEventCount)/totalEventCount;
    }

    private double prepareTimeoutRate(Collection<ServiceEvent> events) {

        LongSummaryStatistics timeStatistics = events.stream()
                .filter(event -> event.getEndTime() != null && !event.isError())
                .collect(Collectors.summarizingLong(event -> event.getEndTime() - event.getStartTime()));

        double timeoutAvg = timeStatistics.getAverage();

        List<ServiceEvent> timeoutErrorEvents = events.stream()
                .filter(event -> event.getEndTime() != null &&
                        (event.getEndTime() - event.getStartTime() > timeoutAvg))
                .collect(Collectors.toList());

        int totalEventCount = events.size();
        int timeoutErrorCount = timeoutErrorEvents.size();

        return (double) (totalEventCount - timeoutErrorCount)/timeoutErrorCount;
    }

}
