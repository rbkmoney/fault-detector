package dev.vality.faultdetector.services;

import dev.vality.faultdetector.data.ServiceOperations;
import dev.vality.faultdetector.data.ServiceSettings;
import dev.vality.faultdetector.handlers.Handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationPreAggregatorService {

    private final Handler<String> calculatePreAggregatesHandler;

    private final ServiceOperations serviceOperations;

    private final Map<String, ServiceSettings> serviceSettingsMap;

    @Scheduled(fixedRateString = "${operations.preAggregationPeriod}")
    public void process() {
        for (String serviceId : serviceOperations.getServices()) {
            try {
                serviceOperations.cleanUnusualOperations(serviceId, serviceSettingsMap.get(serviceId));
                calculatePreAggregatesHandler.handle(serviceId);
            } catch (Exception ex) {
                log.error("An error was received during the pre-aggregation of operations " +
                        "for the service {}", serviceId, ex);
            }
        }
        log.debug("Operations pre-aggregations was finished");
    }

}
