package dev.vality.faultdetector.listeners;

import dev.vality.faultdetector.data.ServiceOperation;
import dev.vality.faultdetector.data.ServiceOperations;
import dev.vality.faultdetector.data.ServiceSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationListener {

    private final ServiceOperations serviceOperations;

    private final Map<String, ServiceSettings> serviceSettingsMap;

    @KafkaListener(topics = "${kafka.topic}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(List<ServiceOperation> serviceOperationsList) {
        log.trace("Number of operations received from topic: {}", serviceOperationsList.size());

        try {
            Set<String> serviceIds = new HashSet<>();
            for (ServiceOperation serviceOperation : serviceOperationsList) {

                String serviceId = serviceOperation.getServiceId();
                String operationId = serviceOperation.getOperationId();

                log.debug("Operation with service id '{}' and operation '{}' obtained from kafka",
                        serviceId, serviceOperation);
                serviceOperations.addOperation(serviceId, operationId, serviceOperation);
                serviceIds.add(serviceId);
            }
            serviceIds.forEach(serviceId ->
                    serviceOperations.cleanUnusualOperations(serviceId, serviceSettingsMap.get(serviceId)));
        } catch (Exception ex) {
            log.error("Error received while processing data from kafka", ex);
        }
        log.debug("{} operations from kafka were obtained", serviceOperationsList.size());
    }

}