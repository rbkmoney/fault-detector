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

    @Scheduled(fixedRateString = "${operations.pre-aggregation-period}")
    public void process() {
        for (String serviceId : serviceOperations.getServices()) {
            try {
                calculatePreAggregatesHandler.handle(serviceId);
            } catch (Exception ex) {
                log.error("An error was received during the pre-aggregation of operations " +
                        "for the service {}", serviceId, ex);
            }
        }
        log.info("Operations pre-aggregations was finished");
    }

}
