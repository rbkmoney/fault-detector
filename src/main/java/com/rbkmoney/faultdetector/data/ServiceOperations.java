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
        if (operationsMap == null) {
            log.error("OperationsMap for service '{}' not found. Service should be re-initialized", serviceId);
            return;
        }

        ServiceOperation operation = operationsMap.get(operationId);

        if (serviceOperation.getEndTime() <= 0) {
            if (operation == null) {
                operationsMap.put(operationId, serviceOperation);
                log.info("New operation '{}' was added to OperationsMap (service id {})", serviceOperation, serviceId);
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

    public void cleanUnusualOperations(String serviceId, ServiceSettings settings) {
        if (settings == null || serviceId == null) {
            log.error("Service settings cannot be null!");
        }
        long currentTimeMillis = System.currentTimeMillis();
        Map<String, ServiceOperation> serviceEventMap = getServiceOperationsMap(serviceId);
        log.info("Start cleaning unusual operations. Total operations for service {}: {}",
                serviceId, serviceEventMap == null ? 0 : serviceEventMap.size());
        int count = 0;
        for (ServiceOperation event : serviceEventMap.values()) {
            if (currentTimeMillis - event.getStartTime() > settings.getSlidingWindow()) {
                serviceEventMap.remove(event.getOperationId());
                count++;
            }
        }
        log.info("Removed {} operations from map for service id {}", count, serviceId);
    }

}
