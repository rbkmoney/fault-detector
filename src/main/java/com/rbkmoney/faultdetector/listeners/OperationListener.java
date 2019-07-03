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
        log.debug("Number of operations received from topic: {}", serviceOperationsList.size());

        for (ServiceOperation serviceOperation : serviceOperationsList) {

            String serviceId = serviceOperation.getServiceId();
            String operationId = serviceOperation.getOperationId();
            serviceOperations.addOperation(serviceId, operationId, serviceOperation);
        }

        // TODO: возможно стоит пересчитывать преагрегаты сразу после обновления мапы с операциями
        log.info("{} operations from kafka were obtained", serviceOperationsList.size());
    }

}