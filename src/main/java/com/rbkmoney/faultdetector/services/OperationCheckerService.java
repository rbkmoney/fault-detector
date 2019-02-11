package com.rbkmoney.faultdetector.services;

import com.rbkmoney.faultdetector.data.ServiceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationCheckerService {

    private final Map<String, Map<String, ServiceEvent>> serviceEventMap;

    @Scheduled(fixedDelayString = "${operations.lifetime}")
    void process() {
        log.debug("Start checking the correctness of the operation time");
        long eventDelay = 120000;
        long hoveringDelay = 240000;
        for (String serviceId : serviceEventMap.keySet()) {
            log.debug("Checking the correctness of the operation time for service {}", serviceId);
            Map<String, ServiceEvent> eventMap = serviceEventMap.get(serviceId);
            for (String requestId : eventMap.keySet()) {
                ServiceEvent serviceEvent = eventMap.get(requestId);
                long currentTime = System.currentTimeMillis();
                Long startTime = serviceEvent.getStartTime();
                Long endTime = serviceEvent.getEndTime();
                if ((endTime != null && (currentTime - endTime) > eventDelay) ||
                        (endTime == null && (currentTime - startTime) > hoveringDelay)) {
                    eventMap.remove(requestId);
                }
            }
        }
        log.debug("Checking the correctness of the operation time was finished");
    }

}
