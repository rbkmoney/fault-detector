package com.rbkmoney.faultdetector.integration;

import com.rbkmoney.faultdetector.FaultDetectorApplication;
import com.rbkmoney.faultdetector.data.ServiceAvailability;
import com.rbkmoney.faultdetector.data.ServiceEvent;
import com.rbkmoney.faultdetector.services.FaultDetectorService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
    private Map<String, ServiceAvailability> availabilityMap;

    @Autowired
    private Map<String, Map<String, ServiceEvent>> serviceEventMap;

    @Autowired
    private FaultDetectorService faultDetectorService;

    private static final String SERVICE_ID = "service_1";

    private static final long TIME_DELTA = 10000;

    @Test
    public void registerOperationTest() throws Exception {
        String startTime = String.valueOf(System.currentTimeMillis());
        faultDetectorService.registerOperation(SERVICE_ID, "1", getStartOperation(startTime));
        String finishTime = startTime + TIME_DELTA;
        faultDetectorService.registerOperation(SERVICE_ID, "1", getFinishOperation(finishTime));

        startTime = String.valueOf(System.currentTimeMillis());
        faultDetectorService.registerOperation(SERVICE_ID, "2", getStartOperation(startTime));
        finishTime = startTime + TIME_DELTA;
        faultDetectorService.registerOperation(SERVICE_ID, "2", getFinishOperation(finishTime));

        startTime = String.valueOf(System.currentTimeMillis());
        faultDetectorService.registerOperation(SERVICE_ID, "3", getStartOperation(startTime));
        finishTime = startTime + TIME_DELTA * 2;
        faultDetectorService.registerOperation(SERVICE_ID, "3", getErrorOperation(finishTime));

        assertEquals("The number of services is less than expected", 1, serviceEventMap.size());
        assertEquals("The number of operations is less than expected", 3, serviceEventMap.get(SERVICE_ID).size());

        Thread.sleep(10000);

        assertEquals("The number of services in the availability map is less than expected",
                1, availabilityMap.size());

    }

}
