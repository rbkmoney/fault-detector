package com.rbkmoney.faultdetector.services;

import com.rbkmoney.faultdetector.data.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationCheckerService {

    private final Map<String, ServiceSettings> serviceSettingsMap;

    private final ServicePreAggregates servicePreAggregates;

    private final ServiceOperations serviceOperations;

    @Scheduled(fixedDelayString = "${operations.revision}")
    void process() {
        log.debug("Start checking the correctness of the operation time");

        // TODO: продумать после какого времени будет ERROR и как будут отстреливаться транзакции
        long currentTimeMillis = System.currentTimeMillis();
        for (String serviceId : servicePreAggregates.getServices()) {
            ServiceSettings settings = serviceSettingsMap.get(serviceId);
            Set<PreAggregates> preAggregatesSet = servicePreAggregates.getPreAggregatesSet(serviceId);
            for (PreAggregates preAggregates : preAggregatesSet) {
                if (currentTimeMillis - preAggregates.getAggregationTime() > settings.getSlidingWindow()) {
                    preAggregatesSet.remove(preAggregates);
                }
            }
        }

        // TODO: зависшие операции после выхода за пределы скользящего окна удаляются.
        //       После начала взаимодействия с сервисами роутинга возможно стоит пересмотреть подход к удалению
        for (String serviceId : serviceOperations.getServices()) {
            ServiceSettings settings = serviceSettingsMap.get(serviceId);
            Map<String, ServiceOperation> serviceEventMap = serviceOperations.getServiceOperationsMap(serviceId);
            for (ServiceOperation event : serviceEventMap.values()) {
                if (currentTimeMillis - event.getStartTime() > settings.getSlidingWindow()) {
                    serviceEventMap.remove(event.getOperationId());
                }
            }
        }

        log.debug("Checking the correctness of the operation time was finished");
    }

}
