package com.rbkmoney.faultdetector.services;

import com.rbkmoney.damsel.fault_detector.*;
import com.rbkmoney.faultdetector.data.ServiceAggregates;
import com.rbkmoney.faultdetector.data.ServiceEvent;
import com.rbkmoney.faultdetector.data.ServiceSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.rbkmoney.faultdetector.utils.SettingsUtils.getServiceSettings;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaultDetectorService implements FaultDetectorSrv.Iface {

    private final Map<String, ServiceAggregates> aggregatesMap;

    private final Map<String, Map<String, ServiceEvent>> serviceMap;

    private final Map<String, ServiceSettings> serviceConfigMap;

    @Override
    public void initService(String serviceId, ServiceConfig serviceConfig) throws TException {
        setServiceSettings(serviceId, serviceConfig);
        serviceMap.put(serviceId, new ConcurrentHashMap<>());
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
        if (serviceConfigMap.get(serviceId) == null || serviceMap.get(serviceId) == null) {
            log.error("Service {} is not initialized", serviceId);
            throw new ServiceNotFoundException();
        }
        setServiceSettings(serviceId, serviceConfig);

        Map<String, ServiceEvent> serviceEventsMap = serviceMap.get(serviceId);

        OperationState operationState = operation.getState();
        if (operationState.isSetStart()) {
            ServiceEvent event = new ServiceEvent();
            String operationId = operation.getOperationId();
            event.setOperationId(operationId);
            event.setStartTime(new Long(operationState.getStart().getTimeStart()));
            serviceEventsMap.put(operationId, event);
            log.debug("New event {} was added", event);
        }

        if (operationState.isSetFinish() || operationState.isSetError()) {
            ServiceEvent event = serviceEventsMap.get(operation.getOperationId());
            if (event == null) {
                log.warn("Event with service id {} and operation id {} not found",
                        serviceId, operation.getOperationId());
                return;
            }
            if (operationState.isSetFinish()) {
                event.setEndTime(new Long(operationState.getFinish().getTimeEnd()));
            } else {
                event.setEndTime(new Long(operationState.getError().getTimeEnd()));
                event.setError(true);
            }
            log.debug("Event {} was updated", event);
        }
        log.info("Registration operation {} for service {} finished", operation, serviceId);
    }

    @Override
    public List<ServiceStatistics> getStatistics(List<String> services) throws TException {
        log.debug("Check statictics for the services {}", services);
        List<ServiceStatistics> serviceStatisticsList = new ArrayList<>();

        for (String serviceId : services) {
            ServiceAggregates aggregates = aggregatesMap.get(serviceId);
            if (aggregates != null) {
                ServiceStatistics stat = new ServiceStatistics();
                stat.setServiceId(aggregates.getServiceId());
                stat.setFailureRate(aggregates.getFailureRate());
                serviceStatisticsList.add(stat);
            }
        }
        log.debug("Statistic for services: {}", serviceStatisticsList);
        return serviceStatisticsList;
    }

}
