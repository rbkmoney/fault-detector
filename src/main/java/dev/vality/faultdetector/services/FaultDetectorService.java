package dev.vality.faultdetector.services;

import dev.vality.damsel.fault_detector.*;
import dev.vality.faultdetector.data.ServiceAggregates;
import dev.vality.faultdetector.data.ServiceOperation;
import dev.vality.faultdetector.data.ServiceOperations;
import dev.vality.faultdetector.data.ServiceSettings;
import dev.vality.faultdetector.handlers.Handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static dev.vality.faultdetector.utils.SettingsMappingUtils.getServiceSettings;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaultDetectorService implements FaultDetectorSrv.Iface {

    private final Map<String, ServiceAggregates> aggregatesMap;

    private final Map<String, ServiceSettings> serviceConfigMap;

    private final Handler<ServiceOperation> sendOperationHandler;

    private final ServiceOperations serviceOperations;

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
    public void registerOperation(String serviceId, Operation operation, ServiceConfig serviceConfig)
            throws ServiceNotFoundException, TException {
        if (!serviceConfigMap.containsKey(serviceId) || !serviceOperations.containsService(serviceId)) {
            log.warn("Service {} is not initialized. It will be re-initialized", serviceId);
            initService(serviceId, serviceConfig);
        }
        if (isEmptyOperation(operation)) {
            log.error("Received empty operation for service '{}'", serviceId);
        } else {
            try {
                setServiceSettings(serviceId, serviceConfig);
                sendOperationHandler.handle(transformOperation(serviceId, operation));
                log.info("Registration operation '{}' for service '{}' finished", operation, serviceId);
            } catch (Exception e) {
                log.error("Error while registration operation", e);
            }
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
        return Instant.parse(dateString).toEpochMilli();
    }

    @Override
    public List<ServiceStatistics> getStatistics(List<String> services) throws TException {
        log.info("Check statistics for the services: {}", services);
        List<ServiceStatistics> serviceStatisticsList = new CopyOnWriteArrayList<>();

        try {
            for (String serviceId : services) {

                ServiceAggregates aggregates = aggregatesMap.get(serviceId);
                if (aggregates != null) {
                    ServiceStatistics stat = new ServiceStatistics();
                    stat.setServiceId(aggregates.getServiceId());
                    stat.setFailureRate(aggregates.getFailureRate());
                    stat.setOperationsCount(aggregates.getOperationsCount().longValue());
                    stat.setErrorOperationsCount(aggregates.getErrorOperationsCount().longValue());
                    stat.setSuccessOperationsCount(aggregates.getSuccessOperationsCount().longValue());
                    serviceStatisticsList.add(stat);
                }
            }
        } catch (Exception ex) {
            log.error("Received error during processing of statistics", ex);
        }

        log.info("Statistics for the services: {}", serviceStatisticsList);
        return serviceStatisticsList;
    }

    private static boolean isEmptyOperation(Operation operation) {
        return (operation == null || operation.getOperationId() == null || operation.getState() == null);
    }

}
