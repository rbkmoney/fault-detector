package com.rbkmoney.faultdetector.listeners;

import com.rbkmoney.faultdetector.data.ServiceOperation;
import com.rbkmoney.faultdetector.data.ServiceOperations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationListener {

    private final ServiceOperations serviceOperations;

    @KafkaListener(topics = "${kafka.topic}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(List<ServiceOperation> serviceOperationsList) {
        log.trace("Number of operations received from topic: {}", serviceOperationsList.size());

        try {
            for (ServiceOperation serviceOperation : serviceOperationsList) {

                String serviceId = serviceOperation.getServiceId();
                String operationId = serviceOperation.getOperationId();

                log.info("Operation with service id '{}' and operation id '{}' obtained from kafka", serviceId, operationId);
                serviceOperations.addOperation(serviceId, operationId, serviceOperation);
            }
        } catch (Exception ex) {
            log.error("Error received while processing data from kafka", ex);
        }
        log.info("{} operations from kafka were obtained", serviceOperationsList.size());
    }

}