package com.rbkmoney.faultdetector.services;

import com.rbkmoney.damsel.fault_detector.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FaultDetectorHighloader {

    private final FaultDetectorSrv.Iface faultDetectorService;

    private List<String> services = Arrays.asList("serv1", "serv2", "serv3", "serv4", "serv5");

    private static final int LOAD_POOL = 5000;

    @Scheduled(fixedRate = 1000L)
    public void process() throws TException {
        try {
            for (int i = 0; i < LOAD_POOL; i++) {
                String serviceName = services.get(i % 5);
                String operId = "operId_" + i + "_" + Instant.now().toString();
                faultDetectorService.registerOperation(
                        serviceName,
                        createTestOperation(operId),
                        createTestServiceConfig()
                );
            }

            faultDetectorService.getStatistics(services);
        } catch (Throwable th) {
            log.error("Throwable received while processing data from kafka", th);
        }
    }

    private static Operation createTestOperation(String operId) {
        Operation operation= new Operation();
        operation.setOperationId(operId);
        OperationState state = new OperationState();
        state.setStart(new Start(Instant.now().toString()));
        operation.setState(state);
        return operation;
    }

    private static ServiceConfig createTestServiceConfig() {
        ServiceConfig config = new ServiceConfig();
        config.setOperationTimeLimit(300000L);
        config.setPreAggregationSize(1000);
        config.setSlidingWindow(400000L);
        return config;
    }

}
