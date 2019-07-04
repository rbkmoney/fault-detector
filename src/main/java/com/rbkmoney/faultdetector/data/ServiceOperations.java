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
        ServiceOperation operation = operationsMap.get(operationId);

        if (serviceOperation.getEndTime() <= 0) {
            if (operation == null) {
                operationsMap.put(operationId, serviceOperation);
                log.info("New operation with service id {} and operation id {} was added", serviceId, operationId);
            } else {
                log.info("For operation with service id {} and operation id {} was modified start time from {} to {}",
                        serviceId, operationId, operation.getStartTime(), serviceOperation.getStartTime());
                operation.setStartTime(serviceOperation.getStartTime());
                operationsMap.put(operationId, operation);
            }
        } else {
            if (operation == null) {
                log.warn("Operation with service id {} and operation id {} not found",
                        serviceId, serviceOperation.getOperationId());
            } else {
                operation.setEndTime(serviceOperation.getEndTime());
                operation.setError(serviceOperation.isError());
                log.info("Operation with service id {} and operation id {} was finished with {} status",
                        serviceId, serviceOperation.getOperationId(), serviceOperation.isError() ? "ERROR" : "SUCCESS");
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
