package dev.vality.faultdetector.unit;

import dev.vality.faultdetector.data.PreAggregates;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import static dev.vality.faultdetector.utils.TransformDataUtils.mergePreAggregates;
import static org.junit.Assert.assertEquals;

public class PreAggregatesTest {

    @Test
    public void mergePreAggregatesTest() {
        PreAggregates lastPreAggregates = getTestPreAggregates(
                1000L,
                getTestOperationsSet("oper_1", "oper_2", "oper_3", "oper_4", "oper_5", "oper_6", "oper_7"),
                7,
                1,
                1,
                getTestOperationsSet("oper_1", "oper_2", "oper_3")
        );
        PreAggregates newPreAggregates = getTestPreAggregates(
                1001L,
                getTestOperationsSet("oper_12", "oper_21", "oper_32", "oper_43", "oper_5", "oper_6", "oper_7"),
                7,
                1,
                1,
                getTestOperationsSet("oper_1", "oper_43", "oper_5")
        );

        mergePreAggregates(lastPreAggregates, newPreAggregates);

        assertEquals("The count of operations in pre-aggregates is not equal to expected",
                11, lastPreAggregates.getOperationsCount());
        assertEquals("The count of success operations in pre-aggregates is not equal to expected",
                14, lastPreAggregates.getSuccessOperationsCount());
        assertEquals("The count of error operations in pre-aggregates is not equal to expected",
                2, lastPreAggregates.getErrorOperationsCount());
        assertEquals("The count of running operations in pre-aggregates is not equal to expected",
                1, lastPreAggregates.getRunningOperationsCount());
        assertEquals("The count of overtime operations in pre-aggregates is not equal to expected",
                5, lastPreAggregates.getOvertimeOperationsCount());
    }

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

    private static PreAggregates getTestPreAggregates(long aggTime,
                                                      Set<String> operationsSet,
                                                      int successCount,
                                                      int errorCount,
                                                      int runningCount,
                                                      Set<String> overtimeOperationsSet) {
        PreAggregates preAggregates = new PreAggregates();
        preAggregates.setServiceId("some.service.1");
        preAggregates.setAggregationTime(aggTime);
        preAggregates.getOperationsSet().addAll(operationsSet);
        preAggregates.setSuccessOperationsCount(successCount);
        preAggregates.setErrorOperationsCount(errorCount);
        preAggregates.setRunningOperationsCount(runningCount);
        preAggregates.getOvertimeOperationsSet().addAll(overtimeOperationsSet);
        return preAggregates;
    }

    private static Set<String> getTestOperationsSet(String... operIds) {
        Set<String> overtimeOpers = new HashSet<>();
        for (String operId : operIds) {
            overtimeOpers.add(operId);
        }
        return overtimeOpers;
    }

}
