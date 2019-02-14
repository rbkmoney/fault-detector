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
public class PrepareStatisticsService {

    private final Map<String, Map<String, ServiceEvent>> serviceEventMap;

    private final Handler prepareStatisticsHandler;

    @Scheduled(fixedDelayString = "${preparing.delay}")
    public void prepare() throws Exception {
        log.debug("Start processing the services statistics");
        for (String serviceId : serviceEventMap.keySet()) {
            prepareStatisticsHandler.handle(serviceId);
        }
        log.debug("Processing the services statistics was finished");
    }

}
