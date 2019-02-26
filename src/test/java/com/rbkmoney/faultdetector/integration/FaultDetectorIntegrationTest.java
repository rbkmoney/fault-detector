package com.rbkmoney.faultdetector.integration;

import com.rbkmoney.damsel.fault_detector.Error;
import com.rbkmoney.damsel.fault_detector.*;
import com.rbkmoney.faultdetector.services.FaultDetectorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.rbkmoney.faultdetector.data.FaultDetectorData.getStartOperation;
import static org.junit.Assert.assertEquals;

@Slf4j
public class FaultDetectorIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private FaultDetectorService faultDetectorService;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");

    @Test
    public void serviceTest() throws TException, ParseException, InterruptedException {
        String serviceId = "service_one";
        int initOperationsCount = 100;
        int successOperationsCount = 80;
        int errorOperationsCount = 20;
        int initOperationsDelta = 3000;
        ServiceConfig serviceConfig = getServiceConfig();
        faultDetectorService.initService(serviceId, serviceConfig);
        List<Operation> startOperations =
                sendStartOperations(serviceId, serviceConfig, initOperationsCount, initOperationsDelta);
        sendFinishOperations(serviceId, serviceConfig, startOperations, successOperationsCount);
        sendErrorOperations(serviceId, serviceConfig, startOperations, errorOperationsCount);
        Thread.sleep(15000);

        List<String> services = new ArrayList<>();
        services.add(serviceId);
        List<ServiceStatistics> statistics = faultDetectorService.getStatistics(services);

        assertEquals("The number of statistics items received is not equal to the expected " +
                "number", 1, statistics.size());

        ServiceStatistics serviceStatistics = statistics.get(0);

        assertEquals("The number of operations is not equal to expected",
                initOperationsCount, serviceStatistics.getOperationsCount());
        assertEquals("The number of success operations is not equal to expected",
                successOperationsCount, serviceStatistics.getSuccessOperationsCount());
        assertEquals("The number of error operations is not equal to expected",
                errorOperationsCount, serviceStatistics.getErrorOperationsCount());
        Double expectedFailureRate = 0.2;
        Double receivedFailureRate = serviceStatistics.getFailureRate();
        assertEquals("Failure rate is not equal to expected", expectedFailureRate, receivedFailureRate);

    }

    private List<Operation> sendStartOperations(String serviceId,
                                                ServiceConfig serviceConfig,
                                                int operationsCount,
                                                int delta) throws TException {
        long startTimeMills = System.currentTimeMillis();
        List<Operation> operations = new ArrayList<>();
        for (int i = 1; i <= operationsCount; i++) {
            long operStartTime = startTimeMills + delta / i;
            String operationId = "oper_id_" + i;
            String startTime = simpleDateFormat.format(new Date(operStartTime));
            Operation startOperation = getStartOperation(operationId, startTime);
            faultDetectorService.registerOperation(serviceId, startOperation, serviceConfig);
            operations.add(startOperation);
        }
        return operations;
    }

    private void sendFinishOperations(String serviceId,
                                      ServiceConfig serviceConfig,
                                      List<Operation> initOperations,
                                      int countFinishOperations) throws TException, ParseException {
        if (initOperations.size() < countFinishOperations) {
            log.error("Count of finish operations is less than the source operations array");
            return;
        }

        int executionTime = (int) serviceConfig.getOperationTimeLimit() / 2;
        for (int i = 0; i < countFinishOperations; i++) {
            sendOneRandomFinishOperation(serviceId, serviceConfig, initOperations, executionTime, false);
        }
    }

    private void sendErrorOperations(String serviceId,
                                     ServiceConfig serviceConfig,
                                     List<Operation> initOperations,
                                     int countErrorOperations) throws TException, ParseException {
        if (initOperations.size() < countErrorOperations) {
            log.error("Count of error operations is less than the source operations array");
            return;
        }

        int executionTime = (int) serviceConfig.getOperationTimeLimit() / 2;
        for (int i = 0; i < countErrorOperations; i++) {
            sendOneRandomFinishOperation(serviceId, serviceConfig, initOperations, executionTime, true);
        }
    }

    private void sendOneRandomFinishOperation(String serviceId,
                                              ServiceConfig serviceConfig,
                                              List<Operation> initOperations,
                                              int executionTime,
                                              boolean isError) throws TException, ParseException {
        int initOperationsSize = initOperations.size();
        int currentOperationNumber = (int) Math.floor(initOperationsSize * Math.random());
        Operation startOperation = initOperations.get(currentOperationNumber);
        Operation finishOperation = new Operation();
        finishOperation.setOperationId(startOperation.getOperationId());
        long timeStart = simpleDateFormat.parse(startOperation.getState().getStart().getTimeStart()).getTime();
        String finishOperationTime = simpleDateFormat.format(new Date(timeStart + executionTime));
        if (isError) {
            Error error = new Error();
            error.setTimeEnd(finishOperationTime);
            OperationState state = new OperationState();
            state.setError(error);
            finishOperation.setState(state);
        } else {
            Finish finish = new Finish();
            finish.setTimeEnd(finishOperationTime);
            OperationState state = new OperationState();
            state.setFinish(finish);
            finishOperation.setState(state);
        }
        initOperations.remove(currentOperationNumber);
        faultDetectorService.registerOperation(serviceId, finishOperation, serviceConfig);
    }

    private static ServiceConfig getServiceConfig() {
        ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.setOperationTimeLimit(60000);
        serviceConfig.setSlidingWindow(90000);
        serviceConfig.setPreAggregationSize(1);
        return serviceConfig;
    }

}
