package com.rbkmoney.faultdetector.services;

import com.rbkmoney.faultdetector.data.ServiceOperations;
import com.rbkmoney.faultdetector.handlers.Handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationPreAggregatorService {

    private final Handler<String> calculatePreAggregatesHandler;

    private final ServiceOperations serviceOperations;

    // TODO: подумать, чтобы время агрегации задавалось пользователем
    // TODO: либо производить агрегацию во время пулинга эаентов из кафки, так как полуаем пакет операций
    @Scheduled(fixedDelayString = "${operations.aggregation-delay}")
    public void process() {
        for (String serviceId : serviceOperations.getServices()) {
            try {
                calculatePreAggregatesHandler.handle(serviceId);
            } catch (Exception e) {
                log.error("An error was received during the pre-aggregation of operations " +
                        "for the service " + serviceId, e);
            }
        }
    }

}
