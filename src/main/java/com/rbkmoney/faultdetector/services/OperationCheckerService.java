package com.rbkmoney.faultdetector.services;

import com.rbkmoney.faultdetector.data.PreAggregates;
import com.rbkmoney.faultdetector.data.ServicePreAggregates;
import com.rbkmoney.faultdetector.data.ServiceSettings;
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

        log.debug("Checking the correctness of the operation time was finished");
    }

}
