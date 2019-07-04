package com.rbkmoney.faultdetector.data;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Slf4j
public class ServicePreAggregates {

    private final Map<String, Set<PreAggregates>> servicePreAggregatesMap = new ConcurrentHashMap<>();

    public void addPreAggregates(String serviceId, PreAggregates preAggregates) {
        Set<PreAggregates> preAggregatesSet;
        if (servicePreAggregatesMap.containsKey(serviceId)) {
            preAggregatesSet = servicePreAggregatesMap.get(serviceId);
        } else {
            preAggregatesSet = new ConcurrentSkipListSet();
        }
        preAggregatesSet.add(preAggregates);
        servicePreAggregatesMap.put(serviceId, preAggregatesSet);
        log.info("Pre-aggregates '{}' for service '{}' were added", preAggregates, serviceId);
    }

    public void cleanPreAggregares(String serviceId, ServiceSettings settings) {
        if (servicePreAggregatesMap.containsKey(serviceId)) {
            Set<PreAggregates> preAggregates = servicePreAggregatesMap.get(serviceId);
            if (preAggregates != null) {
                long currentTime = System.currentTimeMillis();
                long slidingWindow = settings.getSlidingWindow();
                log.info("Clean pre-aggregates for service '{}' get started (sliding window - {}). " +
                                "Count of items before clean: {}", serviceId, slidingWindow, preAggregates.size());
                preAggregates.removeIf(preAggregate -> currentTime - preAggregate.getAggregationTime() > slidingWindow);
                log.info("Clean pre-aggregates for service '{}' finished. Count of items after clean: {}",
                        serviceId, preAggregates.size());
            }
        }

        if (servicePreAggregatesMap.get(serviceId).isEmpty()) {
            servicePreAggregatesMap.remove(serviceId);
        }
    }

    public Set<PreAggregates> getPreAggregatesSet(String serviceId) {
        return servicePreAggregatesMap.get(serviceId);
    }

    public Set<String> getServices() {
        return servicePreAggregatesMap.keySet();
    }

}
