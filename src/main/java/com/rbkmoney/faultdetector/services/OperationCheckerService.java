package com.rbkmoney.faultdetector.services;

import com.rbkmoney.faultdetector.data.ServiceEvent;
import com.rbkmoney.faultdetector.data.ServiceSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class OperationCheckerService {

    private final Map<String, Map<String, ServiceEvent>> serviceEventMap;

    private final Map<String, ServiceSettings> serviceSettingsMap;

    @Scheduled(fixedDelayString = "${operations.lifetime}")
    void process() {
        log.debug("Start checking the correctness of the operation time");

        for (String serviceId : serviceEventMap.keySet()) {
            log.debug("Checking the correctness of the operation time for service {}", serviceId);
            Map<String, ServiceEvent> eventMap = serviceEventMap.get(serviceId);
            for (String operationId : eventMap.keySet()) {
                ServiceEvent serviceEvent = eventMap.get(operationId);
                ServiceSettings serviceSettings = serviceSettingsMap.get(serviceId);
                long currentTime = serviceEvent.getEndTime() == null ?
                        System.currentTimeMillis() : serviceEvent.getEndTime();
                if ((currentTime - serviceEvent.getStartTime()) > serviceSettings.getOperationLifetime()) {
                    eventMap.remove(operationId);
                }
            }
        }
        log.debug("Checking the correctness of the operation time was finished");
    }

}
