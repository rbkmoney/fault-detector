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

    @Override
    public void handle(String serviceId) throws Exception {
        Map<String, ServiceOperation> serviceOperationMap = serviceOperations.getServiceOperationsMap(serviceId);
        if (serviceOperationMap == null || serviceOperationMap.isEmpty()) {
            log.debug("The list of operations for the service {} is empty", serviceId);
            return;
        }

        Long currentTimeMillis = System.currentTimeMillis();

        PreAggregates preAggregates = new PreAggregates();
        preAggregates.setAggregationTime(currentTimeMillis);
        preAggregates.setOperationsCount(serviceOperationMap.size());

        ServiceSettings serviceSettings = serviceSettingsMap.get(serviceId);
        for (ServiceOperation serviceOperation : serviceOperationMap.values()) {
            String operationId = prepareOperation(serviceOperation, preAggregates, serviceSettings, currentTimeMillis);
            serviceOperationMap.remove(operationId);
        }

        servicePreAggregates.addPreAggregates(serviceId, preAggregates);
    }

    private String prepareOperation(ServiceOperation serviceOperation,
                                  PreAggregates preAggregates,
                                  ServiceSettings serviceSettings,
                                  long currentTimeMillis) {
        if (serviceOperation.getEndTime() > 0) {
            long operExecTime = serviceOperation.getEndTime() - serviceOperation.getStartTime();
            if (serviceOperation.isError()) {
                // Добавляем сбойную операцию в преагрегат и удаляем из списка операций
                int errorOperationsCount = preAggregates.getErrorOperationsCount() + 1;
                preAggregates.setErrorOperationsCount(errorOperationsCount);
            } else if (operExecTime > serviceSettings.getOperationTimeLimit()) {
                // Добавляем завершившуюся зависшую операцию в агрегат.
                // Так как операция уже завершилась она удаляется из списка
                int overtimeOperationsCount = preAggregates.getOvertimeOperationsCount() + 1;
                preAggregates.setOvertimeOperationsCount(overtimeOperationsCount);
            } else {
                // Добавляем в список выполняющихся
                int successOperationsCount = preAggregates.getSuccessOperationsCount() + 1;
                preAggregates.setSuccessOperationsCount(successOperationsCount);
            }
            return serviceOperation.getOperationId();
        } else {
            long operExecTime = currentTimeMillis - serviceOperation.getStartTime();
            if (operExecTime > serviceSettings.getOperationTimeLimit()) {
                // Зависшая операция должна учитываться как сбойная, но не удаляться из
                // общего списка, чтобы было понимание сколько операций "висит"
                int hoveringOperationsCount = preAggregates.getOvertimeOperationsCount() + 1;
                preAggregates.setOvertimeOperationsCount(hoveringOperationsCount);
            } else {
                // Операция еще выполняется и не превысила допустимое на выполнение время
                int runningOperationsCount = preAggregates.getRunningOperationsCount() + 1;
                preAggregates.setRunningOperationsCount(runningOperationsCount);
            }
            return null;
        }
    }

}
