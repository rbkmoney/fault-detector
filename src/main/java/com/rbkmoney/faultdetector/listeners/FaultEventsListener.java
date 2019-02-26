package com.rbkmoney.faultdetector.listeners;

import com.rbkmoney.faultdetector.data.ServiceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class FaultEventsListener {

    private final Map<String, Map<String, ServiceEvent>> serviceMap;

    @KafkaListener(topics = "${kafka.topic}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(List<ServiceEvent> serviceEvents) {
        log.debug("serviceEvents size = " + serviceEvents.size());

        for (ServiceEvent serviceEvent : serviceEvents) {
            String serviceId = serviceEvent.getServiceId();
            String operationId = serviceEvent.getOperationId();
            Map<String, ServiceEvent> operationsMap;
            if (serviceMap.get(serviceId) == null) {
                operationsMap = new ConcurrentHashMap<>();
                serviceMap.put(serviceId, operationsMap);
            } else {
                operationsMap = serviceMap.get(serviceId);
            }
            if (serviceEvent.getEndTime() <= 0) {
                operationsMap.put(operationId, serviceEvent);
            } else {
                ServiceEvent event = operationsMap.get(operationId);
                if (event == null) {
                    log.warn("Event with service id {} and operation id {} not found",
                            serviceId, serviceEvent.getOperationId());
                    return;
                } else {
                    event.setEndTime(serviceEvent.getEndTime());
                    event.setError(serviceEvent.isError());
                }
            }
        }
    }

    // TODO: может имеет смысл перекидывать в топик незавершенные операции

}