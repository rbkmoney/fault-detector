package dev.vality.faultdetector.handlers;

import dev.vality.faultdetector.data.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.HashSet;
import java.util.Map;

import static ch.qos.logback.core.CoreConstants.EMPTY_STRING;
import static dev.vality.faultdetector.utils.TransformDataUtils.mergePreAggregates;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalculatePreAggregatesHandler implements Handler<String> {

    private final Map<String, ServiceSettings> serviceSettingsMap;

    private final ServicePreAggregates servicePreAggregates;

    private final ServiceOperations serviceOperations;

    private final Handler<String> calculateAggregatesHandler;

    @Value("${operations.useServiceConfigPreAggregationPeriod}")
    private boolean useServiceConfigPreAggregationPeriod;

    private static final long MILLS_IN_SECOND = 1_000L;

    @Override
    public void handle(String serviceId) {
        ServiceSettings settings = serviceSettingsMap.get(serviceId);
        Long currentTimeMillis = System.currentTimeMillis();

        PreAggregates preAggregates = new PreAggregates();
        preAggregates.setAggregationTime(currentTimeMillis);
        preAggregates.setServiceId(serviceId);

        Map<String, ServiceOperation> serviceOperationMap = serviceOperations.getServiceOperationsMap(serviceId);
        if (serviceOperationMap == null || serviceOperationMap.isEmpty()) {
            log.debug("The list of operations for the service {} is empty", serviceId);
            preAggregates.addOperations(new HashSet<>());
            servicePreAggregates.addPreAggregates(serviceId, preAggregates);
        } else {
            preAggregates.addOperations(serviceOperationMap.keySet());

            for (ServiceOperation serviceOperation : serviceOperationMap.values()) {
                String operationId = prepareOperation(serviceOperation, preAggregates, settings, currentTimeMillis);
                serviceOperationMap.remove(operationId);
            }

            if (useServiceConfigPreAggregationPeriod) {
                preparePreAggregatesWithDynamicWindowSize(serviceId, preAggregates, settings);
            } else {
                log.debug("Pre-aggregates for service '{}' : {}. Current settings: {}",
                        serviceId, preAggregates, settings);
                servicePreAggregates.addPreAggregates(serviceId, preAggregates);
            }
        }
        calculateAggregatesHandler.handle(serviceId);
    }

    private void preparePreAggregatesWithDynamicWindowSize(String serviceId,
                                                           PreAggregates preAggregates,
                                                           ServiceSettings settings) {
        Deque<PreAggregates> preAggregatesDeque = servicePreAggregates.getPreAggregatesDeque(serviceId);
        long preAggSize = settings.getPreAggregationSize() * MILLS_IN_SECOND;
        Long currentTimeMillis = preAggregates.getAggregationTime();

        if (preAggregatesDeque != null
                && currentTimeMillis - preAggregatesDeque.getFirst().getAggregationTime() < preAggSize) {
            PreAggregates lastPreAggregates = preAggregatesDeque.getFirst();
            log.debug("Merge pre-aggregates for service '{}' : old - {} and additional - {}", serviceId,
                    lastPreAggregates, preAggregates);
            mergePreAggregates(lastPreAggregates, preAggregates);
            log.debug("Result pre-aggregates for service '{}' after merge: {}", serviceId, lastPreAggregates);
        } else {
            if (preAggregatesDeque != null) {
                preAggregatesDeque.getFirst().clearTempData();
            }
            log.debug("Pre-aggregates for service '{}' : {}. Current settings: {}", serviceId, preAggregates, settings);
            servicePreAggregates.addPreAggregates(serviceId, preAggregates);
        }
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
                preAggregates.addOvertimeOperation(serviceOperation.getOperationId());
            } else {
                // Добавляем в список успешных операций 
                preAggregates.addSuccessOperation();
            }
            preAggregates.addCompleteOperationTime(operExecTime);
            return serviceOperation.getOperationId();
        } else {
            long operExecTime = currentTimeMillis - serviceOperation.getStartTime();
            if (operExecTime > serviceSettings.getOperationTimeLimit()) {
                // Зависшая операция должна учитываться как сбойная, но не удаляться из
                // общего списка, чтобы было понимание сколько операций "висит"
                preAggregates.addOvertimeOperation(serviceOperation.getOperationId());
            } else {
                // Операция еще выполняется и не превысила допустимое на выполнение время
                preAggregates.addRunningOperation();
            }
            return EMPTY_STRING;
        }
    }

}
