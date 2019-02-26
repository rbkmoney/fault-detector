package com.rbkmoney.faultdetector.services;

import com.rbkmoney.faultdetector.data.ServiceEvent;
import com.rbkmoney.faultdetector.handlers.Handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationAggregatorService {

    private final Map<String, Map<String, ServiceEvent>> serviceMap;

    private final Handler<String> calculatePreAggregatesHandler;

    // TODO: подумать, чтобы время агрегации задавалось полизователем
    @Scheduled(fixedDelayString = "${operations.aggregation-delay}")
    public void process() {
        for (String serviceId : serviceMap.keySet()) {
            try {
                calculatePreAggregatesHandler.handle(serviceId);
            } catch (Exception e) {
                log.error("An error was received during the pre-aggregation of events for the service " + serviceId, e);
            }
        }
    }

}
