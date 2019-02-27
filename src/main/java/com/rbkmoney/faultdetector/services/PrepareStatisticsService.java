package com.rbkmoney.faultdetector.services;

import com.rbkmoney.faultdetector.data.ServiceOperation;
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

    private final Map<String, Map<String, ServiceOperation>> serviceOperationMap;

    private final Handler<String> calculateAggregatesHandler;

    @Scheduled(fixedDelayString = "${preparing.delay}")
    public void prepare() throws Exception {
        log.debug("Start processing the services statistics");
        for (String serviceId : serviceOperationMap.keySet()) {
            // TODO: так как подсчет ведется уже из пре агрегатов, то возможно имеет производить
            //       расчет конечного значения на леиу во время вызова
            calculateAggregatesHandler.handle(serviceId);
        }
        log.debug("Processing the services statistics was finished");
    }

}
