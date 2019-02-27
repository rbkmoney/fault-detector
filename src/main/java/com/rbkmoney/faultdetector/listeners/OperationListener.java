package com.rbkmoney.faultdetector.listeners;

import com.rbkmoney.faultdetector.data.ServiceOperation;
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
public class OperationListener {

    private final Map<String, Map<String, ServiceOperation>> serviceMap;

    @KafkaListener(topics = "${kafka.topic}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(List<ServiceOperation> serviceOperations) {
        log.debug("Number of operations received from topic: {}", serviceOperations.size());

        for (ServiceOperation serviceOperation : serviceOperations) {
            String serviceId = serviceOperation.getServiceId();
            String operationId = serviceOperation.getOperationId();
            Map<String, ServiceOperation> operationsMap;
            if (serviceMap.get(serviceId) == null) {
                operationsMap = new ConcurrentHashMap<>();
                serviceMap.put(serviceId, operationsMap);
            } else {
                operationsMap = serviceMap.get(serviceId);
            }
            if (serviceOperation.getEndTime() <= 0) {
                operationsMap.put(operationId, serviceOperation);
            } else {
                ServiceOperation operation = operationsMap.get(operationId);
                if (operation == null) {
                    log.warn("Operation with service id {} and operation id {} not found",
                            serviceId, serviceOperation.getOperationId());
                    return;
                } else {
                    operation.setEndTime(serviceOperation.getEndTime());
                    operation.setError(serviceOperation.isError());
                }
            }
        }

        log.debug("{} operations were obtained", serviceOperations.size());
    }

    // TODO: может имеет смысл перекидывать в топик незавершенные операции

}