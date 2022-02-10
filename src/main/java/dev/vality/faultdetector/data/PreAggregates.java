package dev.vality.faultdetector.data;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@Data
public class PreAggregates implements Comparable<PreAggregates> {

    private Long aggregationTime;

    private String serviceId;

    private int operationsCount;

    private final Set<String> operationsSet = new HashSet<>();

    private int runningOperationsCount;

    private int overtimeOperationsCount;

    private final Set<String> overtimeOperationsSet = new HashSet<>();

    private int errorOperationsCount;

    private int successOperationsCount;

    private int operationTimeLimit;

    private int slidingWindow;

    private int preAggregationSize;

    private boolean isMerged;

    private List<Long> completeOperationsTimings = new CopyOnWriteArrayList<>();

    public void addOperations(Set<String> operationIds) {
        operationsSet.addAll(operationIds);
        operationsCount = operationsSet.size();
    }

    public void addRunningOperation() {
        runningOperationsCount++;
    }

    public void addOvertimeOperation(String operationId) {
        overtimeOperationsSet.add(operationId);
        overtimeOperationsCount = overtimeOperationsSet.size();
    }

    public void addOvertimeOperations(Set<String> operationIds) {
        overtimeOperationsSet.addAll(operationIds);
        overtimeOperationsCount = overtimeOperationsSet.size();
    }

    public void addErrorOperation() {
        errorOperationsCount++;
    }

    public void addSuccessOperation() {
        successOperationsCount++;
    }

    public void addCompleteOperationTime(Long operationTime) {
        completeOperationsTimings.add(operationTime);
    }

    public double getCompleteOperationsAvgTime() {
        return completeOperationsTimings.stream()
                .mapToDouble(Long::longValue)
                .average()
                .orElse(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PreAggregates)) {
            return false;
        }
        PreAggregates that = (PreAggregates) o;
        return Objects.equals(getAggregationTime(), that.getAggregationTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAggregationTime());
    }

    @Override
    public int compareTo(@NotNull PreAggregates preAggregates) {
        if (preAggregates.getAggregationTime() < aggregationTime) {
            return 1;
        } else if (preAggregates.getAggregationTime().equals(aggregationTime)) {
            return 0;
        } else {
            return -1;
        }
    }

    public void clearTempData() {
        operationsSet.clear();
        overtimeOperationsSet.clear();
    }

    @Override
    public String toString() {
        return "PreAggregates{" +
                "aggregationTime=" + aggregationTime +
                ", serviceId='" + serviceId +
                ", operationsCount=" + operationsCount +
                ", runningOperationsCount=" + runningOperationsCount +
                ", overtimeOperationsCount=" + overtimeOperationsCount +
                ", errorOperationsCount=" + errorOperationsCount +
                ", successOperationsCount=" + successOperationsCount +
                ", completeOperationsAvgTime=" + getCompleteOperationsAvgTime() +
                ", operationTimeLimit=" + operationTimeLimit +
                ", slidingWindow=" + slidingWindow +
                ", preAggregationSize=" + preAggregationSize +
                ", isMerged=" + isMerged +
                '}';
    }
}