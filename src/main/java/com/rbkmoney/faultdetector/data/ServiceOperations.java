package com.rbkmoney.faultdetector.data;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ServiceOperations {

    private final Map<String, Map<String, ServiceOperation>> serviceMap = new ConcurrentHashMap<>();

    public void initService(String serviceId) {
        serviceMap.put(serviceId, new ConcurrentHashMap<>());
        log.info("Service {} have been initialized", serviceId);
    }

    public boolean containsService(String serviceId) {
        return serviceMap.containsKey(serviceId);
    }

    public void addOperation(String serviceId, String operationId, ServiceOperation serviceOperation) {
        Map<String, ServiceOperation> operationsMap = serviceMap.get(serviceId);

        if (serviceOperation.getEndTime() <= 0) {
            operationsMap.put(operationId, serviceOperation);
        } else {
            ServiceOperation operation = operationsMap.get(operationId);
            if (operation == null) {
                log.warn("Operation with service id {} and operation id {} not found",
                        serviceId, serviceOperation.getOperationId());
            } else {
                operation.setEndTime(serviceOperation.getEndTime());
                operation.setError(serviceOperation.isError());
            }
        }

    }

    public Map<String, ServiceOperation> getServiceOperationsMap(String serviceId) {
        if (serviceMap.containsKey(serviceId)) {
            return serviceMap.get(serviceId);
        } else {
            Map<String, ServiceOperation> operationsMap = new ConcurrentHashMap<>();
            serviceMap.put(serviceId, operationsMap);
            return operationsMap;
        }
    }

    public int getServicesCount() {
        return serviceMap.size();
    }

    public Set<String> getServices() {
        return serviceMap.keySet();
    }

}
