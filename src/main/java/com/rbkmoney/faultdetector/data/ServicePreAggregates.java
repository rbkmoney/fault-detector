package com.rbkmoney.faultdetector.data;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

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
    }

    public void cleanPreAggregares(String serviceId, ServiceSettings settings) {
        if (servicePreAggregatesMap.containsKey(serviceId)) {
            Set<PreAggregates> preAggregates = servicePreAggregatesMap.get(serviceId);
            long currentTime = System.currentTimeMillis();
            long slidingWindow = settings.getSlidingWindow();
            preAggregates.removeIf(preAggregate -> currentTime - preAggregate.getAggregationTime() > slidingWindow);
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
