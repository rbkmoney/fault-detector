package com.rbkmoney.faultdetector.handlers;

import com.rbkmoney.faultdetector.data.ServiceOperation;
import com.rbkmoney.faultdetector.data.ServicePreAggregates;
import com.rbkmoney.faultdetector.data.ServiceSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalculatePreAggregatesHandler implements Handler<String> {

    private final Map<String, Map<String, ServiceOperation>> serviceMap;

    private final Map<String, Map<Long, ServicePreAggregates>> servicePreAggregatesMap;

    private final Map<String, ServiceSettings> serviceSettingsMap;

    @Override
    public void handle(String serviceId) throws Exception {
        Map<String, ServiceOperation> serviceOperationMap = serviceMap.get(serviceId);
        if (serviceOperationMap == null || serviceOperationMap.isEmpty()) {
            return;
        }

        Map<Long, ServicePreAggregates> preAggregatesMap = servicePreAggregatesMap.get(serviceId);
        if (preAggregatesMap == null) {
            preAggregatesMap = new ConcurrentHashMap<>();
        }

        Long currentTimeMillis = System.currentTimeMillis();

        ServicePreAggregates preAggregates = new ServicePreAggregates();
        preAggregates.setServiceId(serviceId);
        preAggregates.setAggregationTime(currentTimeMillis);
        preAggregates.setOperationsCount(serviceOperationMap.size());

        ServiceSettings serviceSettings = serviceSettingsMap.get(serviceId);
        for (ServiceOperation serviceOperation : serviceOperationMap.values()) {
            prepareOperation(serviceOperation, preAggregates, serviceSettings, serviceOperationMap, currentTimeMillis);
        }

        preAggregatesMap.put(currentTimeMillis, preAggregates);
        servicePreAggregatesMap.put(serviceId, preAggregatesMap);
    }

    private void prepareOperation(ServiceOperation serviceOperation,
                                  ServicePreAggregates preAggregates,
                                  ServiceSettings serviceSettings,
                                  Map<String, ServiceOperation> serviceOperationMap,
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
            serviceOperationMap.remove(serviceOperation.getOperationId());
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
        }
    }

}
