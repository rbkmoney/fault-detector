package com.rbkmoney.faultdetector.services;

import com.rbkmoney.damsel.fault_detector.*;
import com.rbkmoney.faultdetector.data.ServiceAvailability;
import com.rbkmoney.faultdetector.data.ServiceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Service
public class FaultDetectorService implements FaultDetectorSrv.Iface {

    private final Map<String, ServiceAvailability> availabilityMap;

    private final Map<String, Map<String, ServiceEvent>> serviceEventMap;

    @Override
    public void registerOperation(String serviceId, String requestId, Operation operation) throws TException {
        log.debug("Start registration operation {} for service id {} and request id {}",
                operation, serviceId, requestId);
        Map<String, ServiceEvent> requestEventMap = serviceEventMap.get(serviceId);
        if (requestEventMap == null) {
            requestEventMap = new ConcurrentHashMap<>();
        }

        if (operation.isSetStart()) {
            ServiceEvent event = new ServiceEvent();
            event.setRequestId(requestId);
            event.setStartTime(new Long(operation.getStart().getTimeStart()));
            requestEventMap.put(requestId, event);
            log.debug("New event {} was added", event);
        }
        if (operation.isSetFinish() || operation.isSetError()) {
            ServiceEvent event = requestEventMap.get(requestId);
            if (event == null) {
                log.warn("Start event with service id {} and request id {} not found");
                return;
            }
            if (operation.isSetFinish()) {
                event.setEndTime(new Long(operation.getFinish().getTimeEnd()));
            } else {
                event.setEndTime(new Long(operation.getError().getTimeEnd()));
                event.setError(true);
            }
            log.debug("Event {} was updated", event);
        }
        serviceEventMap.put(serviceId, requestEventMap);

        log.debug("Registration operation {} for service id {} and request id {} finished",
                operation, serviceId, requestId);
    }

    @Override
    public List<Availability> checkAvailability(List<String> services) throws TException {
        log.debug("Check availability for the services {}", services);
        List<Availability> availabilityResponse = new ArrayList<>();

        for (String serviceId : services) {
            ServiceAvailability availability = availabilityMap.get(serviceId);
            if (availability != null) {
                Availability avail = new Availability();
                avail.setServiceId(availability.getServiceId());
                avail.setSuccessRate(availability.getSuccessRate());
                avail.setTimeoutRate(availability.getTimeoutRate());
                availabilityResponse.add(avail);
            }
        }
        log.debug("Availability for services: {}", availabilityResponse);
        return availabilityResponse;
    }

    @Override
    public void setServiceStatistics(String serviceId, ServiceConfig serviceConfig) throws TException {
        log.debug("Set service statistics for service id {}", serviceId);
        ServiceAvailability availability = new ServiceAvailability();
        double configValue = serviceConfig.getValue();
        // TODO: вопрос правильности выставления параметров
        // TODO: по факту необходимо настраивать параметры определения работоспособности сервиса,
        //       а не финальный результат. Например, среднее время выполнения операции
        switch (serviceConfig.getType()) {
            case SUCCESS_RATE:
                availability.setSuccessRate(configValue);
                log.debug("Set success rate: {}", configValue);
                break;
            case TIMEOUT_RATE:
                availability.setTimeoutRate(configValue);
                log.debug("Set timeout rate: {}", configValue);
                break;
            case ALL:
                availability.setTimeoutRate(configValue);
                availability.setSuccessRate(configValue);
                log.debug("Set all rates: {}", configValue);
                break;
        }
        availabilityMap.put(serviceId, availability);
    }

}
