package com.rbkmoney.faultdetector.integration;

import com.rbkmoney.damsel.fault_detector.ServiceConfig;
import com.rbkmoney.faultdetector.FaultDetectorApplication;
import com.rbkmoney.faultdetector.data.ServiceAggregates;
import com.rbkmoney.faultdetector.data.ServiceEvent;
import com.rbkmoney.faultdetector.services.FaultDetectorService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static com.rbkmoney.faultdetector.data.FaultDetectorData.*;
import static org.junit.Assert.assertEquals;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = FaultDetectorApplication.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FaultDetectorApplicationTest {

    @Autowired
    private Map<String, ServiceAggregates> availabilityMap;

    @Autowired
    private Map<String, Map<String, ServiceEvent>> serviceEventMap;

    @Autowired
    private FaultDetectorService faultDetectorService;

    private static final String SERVICE_ID = "service_1";

    private static final long TIME_DELTA = 10000;

    @Test
    public void registerOperationTest() throws Exception {

        ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.setOperationLifetime(10000);
        serviceConfig.setSlidingWindow(5000);
        serviceConfig.setTimeoutDelta((short) 15);
        serviceConfig.setHoveringOperationErrorDelay(2500);

        faultDetectorService.initService(SERVICE_ID, serviceConfig);

        String startTime = String.valueOf(System.currentTimeMillis());
        faultDetectorService.registerOperation(SERVICE_ID, getStartOperation("1", startTime), serviceConfig);
        String finishTime = startTime + TIME_DELTA;
        faultDetectorService.registerOperation(SERVICE_ID, getFinishOperation("1", finishTime), serviceConfig);

        startTime = String.valueOf(System.currentTimeMillis());
        faultDetectorService.registerOperation(SERVICE_ID, getStartOperation("2", startTime), serviceConfig);
        finishTime = startTime + TIME_DELTA;
        faultDetectorService.registerOperation(SERVICE_ID, getFinishOperation("2", finishTime), serviceConfig);

        startTime = String.valueOf(System.currentTimeMillis());
        faultDetectorService.registerOperation(SERVICE_ID, getStartOperation("3", startTime), serviceConfig);
        finishTime = startTime + TIME_DELTA * 2;
        faultDetectorService.registerOperation(SERVICE_ID, getErrorOperation("3", finishTime), serviceConfig);

        assertEquals("The number of services is less than expected", 1, serviceEventMap.size());
        assertEquals("The number of operations is less than expected", 3, serviceEventMap.get(SERVICE_ID).size());

        Thread.sleep(15000);

        assertEquals("The number of services in the availability map is less than expected",
                1, availabilityMap.size());

    }
}
