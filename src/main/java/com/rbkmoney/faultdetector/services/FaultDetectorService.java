package com.rbkmoney.faultdetector.services;

import com.rbkmoney.damsel.fault_detector.*;
import com.rbkmoney.faultdetector.data.ServiceAggregates;
import com.rbkmoney.faultdetector.data.ServiceOperation;
import com.rbkmoney.faultdetector.data.ServiceOperations;
import com.rbkmoney.faultdetector.data.ServiceSettings;
import com.rbkmoney.faultdetector.handlers.Handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.rbkmoney.faultdetector.utils.SettingsMappingUtils.getServiceSettings;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaultDetectorService implements FaultDetectorSrv.Iface {

    private final Map<String, ServiceAggregates> aggregatesMap;

    private final Map<String, ServiceSettings> serviceConfigMap;

    private final Handler<ServiceOperation> sendOperationHandler;

    private final ServiceOperations serviceOperations;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    public void initService(String serviceId, ServiceConfig serviceConfig) throws TException {
        setServiceSettings(serviceId, serviceConfig);
        serviceOperations.initService(serviceId);
        log.info("Service {} have been initialized", serviceId);
    }

    private void setServiceSettings(String serviceId, ServiceConfig serviceConfig) {
        ServiceSettings serviceSettings = getServiceSettings(serviceConfig);
        serviceConfigMap.put(serviceId, serviceSettings);
    }

    @Override
    public void registerOperation(String serviceId,
                                  Operation operation,
                                  ServiceConfig serviceConfig) throws ServiceNotFoundException, TException {
        log.info("Start registration operation {} for service {}", operation, serviceId);
        if (!serviceConfigMap.containsKey(serviceId) || !serviceOperations.containsService(serviceId)) {
            log.error("Service {} is not initialized", serviceId);
            throw new ServiceNotFoundException();
        }

        setServiceSettings(serviceId, serviceConfig);

        try {
            sendOperationHandler.handle(transformOperation(serviceId, operation));
            log.info("Registration operation {} for service {} finished", operation, serviceId);
        } catch (Exception e) {
            log.error("Error sending data", e);
        }
    }

    private ServiceOperation transformOperation(String serviceId, Operation operation) {
        ServiceOperation serviceOperation = new ServiceOperation();
        serviceOperation.setServiceId(serviceId);
        serviceOperation.setOperationId(operation.getOperationId());
        OperationState operationState = operation.getState();
        if (operationState.isSetStart()) {
            serviceOperation.setStartTime(getTime(operationState.getStart().getTimeStart()));
            serviceOperation.setEndTime(-1L);
        }
        if (operationState.isSetFinish()) {
            serviceOperation.setEndTime(getTime(operationState.getFinish().getTimeEnd()));
        }
        if (operationState.isSetError()) {
            serviceOperation.setEndTime(getTime(operationState.getError().getTimeEnd()));
            serviceOperation.setError(true);
        }
        return serviceOperation;
    }

    private long getTime(String dateString) {
        return LocalDateTime.parse(dateString, formatter).toEpochSecond(ZoneOffset.UTC);
    }

    @Override
    public List<ServiceStatistics> getStatistics(List<String> services) throws TException {
        log.info("Check statictics for the services {}", services);
        List<ServiceStatistics> serviceStatisticsList = new ArrayList<>();

        for (String serviceId : services) {
            ServiceAggregates aggregates = aggregatesMap.get(serviceId);
            if (aggregates != null) {
                ServiceStatistics stat = new ServiceStatistics();
                stat.setServiceId(aggregates.getServiceId());
                stat.setFailureRate(aggregates.getFailureRate());
                stat.setOperationsCount(aggregates.getOperationsCount());
                stat.setErrorOperationsCount(aggregates.getErrorOperationsCount());
                stat.setSuccessOperationsCount(aggregates.getSuccessOperationsCount());
                serviceStatisticsList.add(stat);
            }
        }
        log.debug("Statistic for services: {}", serviceStatisticsList);
        return serviceStatisticsList;
    }

}
