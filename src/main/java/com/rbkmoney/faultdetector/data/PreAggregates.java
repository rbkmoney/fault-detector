package com.rbkmoney.faultdetector.data;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
public class PreAggregates implements Comparable<PreAggregates> {

    private Long aggregationTime;

    private String serviceId;

    private int operationsCount;

    private int runningOperationsCount;

    private Set<String> overtimeOperationsSet = new HashSet<>();

    private int errorOperationsCount;

    private int successOperationsCount;

    private int operationTimeLimit;

    private int slidingWindow;

    private int preAggregationSize;

    public void addRunningOperation() {
        runningOperationsCount++;
    }

    public void addOvertimeOperation(String operationId) {
        overtimeOperationsSet.add(operationId);
    }

    public int getOvertimeOperationsCount() {
        return overtimeOperationsSet.size();
    }

    public void addErrorOperation() {
        errorOperationsCount++;
    }

    public void addSuccessOperation() {
        successOperationsCount++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PreAggregates)) return false;
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
        } else if (preAggregates.getAggregationTime() == aggregationTime) {
            return 0;
        } else {
            return -1;
        }
    }

    public PreAggregates copy() {
        PreAggregates preAggregates = new PreAggregates();
        preAggregates.setAggregationTime(aggregationTime);
        preAggregates.setServiceId(serviceId);
        preAggregates.setOperationsCount(operationsCount);
        preAggregates.setRunningOperationsCount(runningOperationsCount);
        preAggregates.setOvertimeOperationsSet(overtimeOperationsSet);
        preAggregates.setErrorOperationsCount(errorOperationsCount);
        preAggregates.setSuccessOperationsCount(successOperationsCount);
        preAggregates.setOperationTimeLimit(operationTimeLimit);
        preAggregates.setSlidingWindow(slidingWindow);
        preAggregates.setPreAggregationSize(preAggregationSize);
        return preAggregates;
    }
}