package com.rbkmoney.faultdetector.handlers;

import com.rbkmoney.faultdetector.data.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalculatePreAggregatesHandler implements Handler<String> {

    private final Map<String, ServiceSettings> serviceSettingsMap;

    private final ServicePreAggregates servicePreAggregates;

    private final ServiceOperations serviceOperations;

    private final Map<String, ServiceSettings> serviceConfigMap;

    private final Handler<String> calculateAggregatesHandler;

    private static final String EMPTY_STRING = "";

    @Override
    public void handle(String serviceId) {
        Map<String, ServiceOperation> serviceOperationMap = serviceOperations.getServiceOperationsMap(serviceId);
        if (serviceOperationMap == null || serviceOperationMap.isEmpty()) {
            //TODO: вопрос на мульт - заполнять пустыми значениями или не делать ничего?
            log.info("The list of operations for the service {} is empty", serviceId);
            return;
        }

        Long currentTimeMillis = System.currentTimeMillis();

        PreAggregates preAggregates = new PreAggregates();
        preAggregates.setAggregationTime(currentTimeMillis);
        preAggregates.setServiceId(serviceId);
        preAggregates.setOperationsCount(serviceOperationMap.size());

        ServiceSettings serviceSettings = serviceSettingsMap.get(serviceId);
        for (ServiceOperation serviceOperation : serviceOperationMap.values()) {
            String operationId = prepareOperation(serviceOperation, preAggregates, serviceSettings, currentTimeMillis);
            serviceOperationMap.remove(operationId);
        }

        ServiceSettings settings = serviceConfigMap.get(serviceId);
        log.info("Pre-aggregates for service '{}' : {}. Current settings: {}", serviceId, preAggregates, settings);
        servicePreAggregates.addPreAggregates(serviceId, preAggregates);
        servicePreAggregates.cleanPreAggregares(serviceId, settings);

        calculateAggregatesHandler.handle(serviceId);
    }

    private String prepareOperation(ServiceOperation serviceOperation,
                                  PreAggregates preAggregates,
                                  ServiceSettings serviceSettings,
                                  long currentTimeMillis) {
        if (serviceOperation.getEndTime() > 0) {
            long operExecTime = serviceOperation.getEndTime() - serviceOperation.getStartTime();
            if (serviceOperation.isError()) {
                // Добавляем сбойную операцию в преагрегат и удаляем из списка операций
                preAggregates.addErrorOperation();
            } else if (operExecTime > serviceSettings.getOperationTimeLimit()) {
                // Добавляем завершившуюся зависшую операцию в агрегат.
                // Так как операция уже завершилась она удаляется из списка
                preAggregates.addOvertimeOperation();
            } else {
                // Добавляем в список выполняющихся
                preAggregates.addSuccessOperation();
            }
            return serviceOperation.getOperationId();
        } else {
            long operExecTime = currentTimeMillis - serviceOperation.getStartTime();
            if (operExecTime > serviceSettings.getOperationTimeLimit()) {
                // Зависшая операция должна учитываться как сбойная, но не удаляться из
                // общего списка, чтобы было понимание сколько операций "висит"
                preAggregates.addOvertimeOperation();
            } else {
                // Операция еще выполняется и не превысила допустимое на выполнение время
                preAggregates.addRunningOperation();
            }
            return EMPTY_STRING;
        }
    }

}
