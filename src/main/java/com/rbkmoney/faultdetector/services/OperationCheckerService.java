package com.rbkmoney.faultdetector.services;

import com.rbkmoney.faultdetector.data.ServiceEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class OperationCheckerService {

    @Autowired
    private Map<String, Map<String, ServiceEvent>> serviceEventMap;

    @Value("${operations.event-delay}")
    private long eventDelay;

    @Value("${operations.hovering-delay}")
    private long hoveringDelay;

    @Scheduled(fixedDelayString = "${operations.lifetime}")
    void process() {
        log.debug("Start checking the correctness of the operation time");

        for (String serviceId : serviceEventMap.keySet()) {
            log.debug("Checking the correctness of the operation time for service {}", serviceId);
            Map<String, ServiceEvent> eventMap = serviceEventMap.get(serviceId);
            for (String requestId : eventMap.keySet()) {
                ServiceEvent serviceEvent = eventMap.get(requestId);
                Long startTime = serviceEvent.getStartTime();
                Long endTime = serviceEvent.getEndTime();
                long currentTime = System.currentTimeMillis();
                boolean isOldEvent = endTime != null && (currentTime - endTime) > eventDelay;
                boolean isEventNotFinished = endTime == null && (currentTime - startTime) > hoveringDelay;
                if (isOldEvent || isEventNotFinished) {
                    eventMap.remove(requestId);
                }
            }
        }
        log.debug("Checking the correctness of the operation time was finished");
    }

}
