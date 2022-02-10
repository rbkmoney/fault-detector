package dev.vality.faultdetector.data;

import lombok.extern.slf4j.Slf4j;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
public class ServicePreAggregates {

    private final Map<String, Deque<PreAggregates>> servicePreAggregatesMap = new ConcurrentHashMap<>();

    public void addPreAggregates(String serviceId, PreAggregates preAggregates) {
        Deque<PreAggregates> preAggregatesSet;
        if (servicePreAggregatesMap.containsKey(serviceId)) {
            preAggregatesSet = servicePreAggregatesMap.get(serviceId);
        } else {
            preAggregatesSet = new ConcurrentLinkedDeque<>();
        }
        preAggregatesSet.addFirst(preAggregates);
        servicePreAggregatesMap.put(serviceId, preAggregatesSet);
        log.debug("New pre-aggregates '{}' for service '{}' were added. Count of pre-aggregates: {} ",
                preAggregates, serviceId, servicePreAggregatesMap.get(serviceId).size());
    }

    public void cleanPreAggregares(String serviceId, ServiceSettings settings) {
        if (servicePreAggregatesMap.containsKey(serviceId)) {
            Deque<PreAggregates> preAggregates = servicePreAggregatesMap.get(serviceId);
            log.debug("Total pre-aggregates before clean operation for service {}: {}",
                    serviceId, preAggregates == null ? 0 : preAggregates.size());
            if (preAggregates != null) {
                long currentTime = System.currentTimeMillis();
                long slidingWindow = settings.getSlidingWindow();
                log.debug("Clean pre-aggregates for service '{}' get started (sliding window - {}). " +
                        "Count of items before clean: {}", serviceId, slidingWindow, preAggregates.size());
                preAggregates.removeIf(preAggregate -> currentTime - preAggregate.getAggregationTime() > slidingWindow);
                log.debug("Clean pre-aggregates for service '{}' finished. Count of items after clean: {}",
                        serviceId, preAggregates.size());
            }
        } else {
            log.error("Pre-aggregates for clean operation for service {} not found", serviceId);
        }

        if (servicePreAggregatesMap.get(serviceId) != null && servicePreAggregatesMap.get(serviceId).isEmpty()) {
            servicePreAggregatesMap.remove(serviceId);
        }
    }

    public Deque<PreAggregates> getPreAggregatesDeque(String serviceId) {
        return servicePreAggregatesMap.get(serviceId);
    }

}
