package com.rbkmoney.faultdetector.services;

import com.rbkmoney.faultdetector.data.ServiceEvent;
import com.rbkmoney.faultdetector.data.ServicePreAggregates;
import com.rbkmoney.faultdetector.data.ServiceSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationCheckerService {

    private final Map<String, ServiceSettings> serviceSettingsMap;

    private final Map<String, Map<Long, ServicePreAggregates>> servicePreAggregatesMap;

    private final Map<String, Map<String, ServiceEvent>> serviceMap;

    @Scheduled(fixedDelayString = "${operations.revision}")
    void process() {
        log.debug("Start checking the correctness of the operation time");

        // TODO: продумать после какого времени будет ERROR и как будут отстреливаться транзакции
        long currentTimeMillis = System.currentTimeMillis();
        for (String serviceId : servicePreAggregatesMap.keySet()) {
            ServiceSettings settings = serviceSettingsMap.get(serviceId);
            Map<Long, ServicePreAggregates> preAggregatesMap = servicePreAggregatesMap.get(serviceId);
            for (ServicePreAggregates preAggregates : preAggregatesMap.values()) {
                if (currentTimeMillis - preAggregates.getAggregationTime() > settings.getSlidingWindow()) {
                    preAggregatesMap.remove(preAggregates.getAggregationTime());
                }
            }
        }

        // TODO: во время агрегации удалить зависшие операции
        for (String serviceId : serviceMap.keySet()) {
            ServiceSettings settings = serviceSettingsMap.get(serviceId);
            Map<String, ServiceEvent> serviceEventMap = serviceMap.get(serviceId);
            for (ServiceEvent event : serviceEventMap.values()) {
                if (currentTimeMillis - event.getStartTime() > settings.getSlidingWindow()) {
                    serviceEventMap.remove(event.getOperationId());
                }
            }
        }

        log.debug("Checking the correctness of the operation time was finished");
    }

}
