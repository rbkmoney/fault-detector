package com.rbkmoney.faultdetector.integration;

import com.rbkmoney.damsel.fault_detector.ServiceConfig;
import com.rbkmoney.faultdetector.data.ServiceOperation;
import com.rbkmoney.faultdetector.data.ServiceOperations;
import com.rbkmoney.faultdetector.handlers.Handler;
import com.rbkmoney.faultdetector.services.FaultDetectorService;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import static org.junit.Assert.assertEquals;

@Ignore
public class KafkaIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private Handler<ServiceOperation> SendOperationHandler;

    @Autowired
    private ServiceOperations serviceOperations;

    @Autowired
    private FaultDetectorService faultDetectorService;

    @Value("${kafka.topic}")
    private String topicName;

    @Test
    public void kafkaTest() throws Exception {
        faultDetectorService.initService("1", getServiceConfig());
        faultDetectorService.initService("2", getServiceConfig());

        SendOperationHandler.handle(getStartServiceEvent("1", "1"));
        SendOperationHandler.handle(getStartServiceEvent("2", "1"));
        Thread.sleep(1000);
        SendOperationHandler.handle(getFinishServiceEvent("1", "1"));
        Thread.sleep(1000);
        SendOperationHandler.handle(getErrorServiceEvent("2", "1"));
        Thread.sleep(45000);
        assertEquals("Count of operations not equal", 2, serviceOperations.getServicesCount());
    }

    private ServiceOperation getStartServiceEvent(String serviceId, String operationId) {
        ServiceOperation event = new ServiceOperation();
        event.setServiceId(serviceId);
        event.setOperationId(operationId);
        event.setStartTime(System.currentTimeMillis());
        return event;
    }

    private ServiceOperation getFinishServiceEvent(String serviceId, String operationId) {
        return getEndlessServiceEvent(serviceId, operationId, false);
    }

    private ServiceOperation getErrorServiceEvent(String serviceId, String operationId) {
        return getEndlessServiceEvent(serviceId, operationId, true);
    }

    private ServiceOperation getEndlessServiceEvent(String serviceId, String operationId, boolean isError) {
        ServiceOperation event = new ServiceOperation();
        event.setServiceId(serviceId);
        event.setOperationId(operationId);
        event.setEndTime(System.currentTimeMillis());
        event.setError(isError);
        return event;
    }

    private static ServiceConfig getServiceConfig() {
        ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.setOperationTimeLimit(10000L);
        serviceConfig.setSlidingWindow(50000L);
        serviceConfig.setPreAggregationSize(1);
        return serviceConfig;
    }

}
