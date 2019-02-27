package com.rbkmoney.faultdetector.integration;

import com.rbkmoney.faultdetector.data.ServiceOperation;
import com.rbkmoney.faultdetector.data.ServiceSettings;
import com.rbkmoney.faultdetector.handlers.Handler;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class KafkaIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private Handler<ServiceOperation> SendOperationHandler;

    @Autowired
    private Map<String, Map<String, ServiceOperation>> serviceMap;

    @Autowired
    private Map<String, ServiceSettings> serviceSettingsMap;

    @Value("${kafka.topic}")
    private String topicName;

    @Test
    public void kafkaTest() throws Exception {
        serviceSettingsMap.put("1", getServiceSettings());
        serviceSettingsMap.put("2", getServiceSettings());

        SendOperationHandler.handle(getStartServiceEvent("1", "1"));
        SendOperationHandler.handle(getStartServiceEvent("2", "1"));
        Thread.sleep(1000);
        SendOperationHandler.handle(getFinishServiceEvent("1", "1"));
        Thread.sleep(1000);
        SendOperationHandler.handle(getErrorServiceEvent("2", "1"));
        Thread.sleep(45000);
        assertEquals("Count of operations not equal to expected", 2, serviceMap.size());
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

    private static ServiceSettings getServiceSettings() {
        ServiceSettings serviceSettings = new ServiceSettings();
        serviceSettings.setOperationTimeLimit(10000L);
        serviceSettings.setSlidingWindow(50000L);
        serviceSettings.setPreAggregationSize(1);
        return serviceSettings;
    }

}
