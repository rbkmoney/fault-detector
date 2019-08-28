package com.rbkmoney.faultdetector.unit;

import com.rbkmoney.faultdetector.data.PreAggregates;
import com.rbkmoney.faultdetector.data.ServiceSettings;
import com.rbkmoney.faultdetector.utils.TransformDataUtils;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class PreAggregatesTest {

    @Test
    public void compareTest() {
        Set<PreAggregates> preAggregatesSet = new ConcurrentSkipListSet();
        preAggregatesSet.add(getPreAggregates(1L));
        preAggregatesSet.add(getPreAggregates(3L));
        preAggregatesSet.add(getPreAggregates(2L));
        preAggregatesSet.add(getPreAggregates(5L));
        preAggregatesSet.add(getPreAggregates(4L));
        String aggTimesString = preAggregatesSet.stream()
                .map(agg -> agg.getAggregationTime().toString())
                .collect(Collectors.joining());
        assertEquals("The order of the preaggregates is not equal to the target",
                "12345", aggTimesString);
    }

    private PreAggregates getPreAggregates(Long aggregationTime) {
        PreAggregates preAggregates = new PreAggregates();
        preAggregates.setAggregationTime(aggregationTime);
        return preAggregates;
    }

    @Test
    public void getPreAggregatesDequeBySettingsTest() {

        Deque<PreAggregates> preAggregatesDequeBySettings = TransformDataUtils.getPreAggregatesDequeBySettings(
                getTestSourcePreAggregatesDeque(),
                getTestServiceSettings()
        );

        assertEquals("The count of pre-aggregates is not equal to expected",
                2, preAggregatesDequeBySettings.size());
        PreAggregates resultPreAggregates = preAggregatesDequeBySettings.getFirst();

        assertEquals("The count of operations in pre-aggregates is not equal to expected",
                35, resultPreAggregates.getOperationsCount());
        assertEquals("The count of success operations in pre-aggregates is not equal to expected",
                21, resultPreAggregates.getSuccessOperationsCount());
        assertEquals("The count of success operations in pre-aggregates is not equal to expected",
                4, resultPreAggregates.getErrorOperationsCount());
        assertEquals("The count of running operations in pre-aggregates is not equal to expected",
                4, resultPreAggregates.getRunningOperationsCount());
        assertEquals("The count of overtime operations in pre-aggregates is not equal to expected",
                3, resultPreAggregates.getOvertimeOperationsCount());
    }

    private static Deque<PreAggregates> getTestSourcePreAggregatesDeque() {
        Deque<PreAggregates> deque = new ArrayDeque<>();
        deque.addFirst(getTestPreAggregates(1000L, 10, 7, 1,
                1, getTestOvertimeOperationsSet("oper_1")));
        deque.addFirst(getTestPreAggregates(1001L, 20, 14, 1,
                2, getTestOvertimeOperationsSet("oper_1", "oper_2", "oper_3")));
        deque.addFirst(getTestPreAggregates(1002L, 15, 7, 3,
                4, getTestOvertimeOperationsSet("oper_2")));
        return deque;
    }

    private static PreAggregates getTestPreAggregates(long aggTime, int operCount, int successCount, int errorCount,
                                                      int runningCount, Set<String> overtimeOperationsSet) {
        PreAggregates preAggregates = new PreAggregates();
        preAggregates.setServiceId("some.service.1");
        preAggregates.setAggregationTime(aggTime);
        preAggregates.setOperationsCount(operCount);
        preAggregates.setSuccessOperationsCount(successCount);
        preAggregates.setErrorOperationsCount(errorCount);
        preAggregates.setRunningOperationsCount(runningCount);
        preAggregates.setOvertimeOperationsSet(overtimeOperationsSet);
        return preAggregates;
    }

    private static Set<String> getTestOvertimeOperationsSet(String... operIds) {
        Set<String> overtimeOpers = new HashSet<>();
        for (String operId : operIds) {
            overtimeOpers.add(operId);
        }
        return overtimeOpers;
    }

    private static ServiceSettings getTestServiceSettings() {
        ServiceSettings settings = new ServiceSettings();
        settings.setOperationTimeLimit(10000);
        settings.setPreAggregationSize(2);
        settings.setSlidingWindow(30000);
        return settings;
    }

}
