package com.rbkmoney.faultdetector.data;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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

    public void addOperation(String operationId) {
        operationsSet.add(operationId);
        operationsCount = operationsSet.size();
    }

    public void addOperations(List<String> operationIds) {
        operationsSet.addAll(operationIds);
        operationsCount = operationsSet.size();
    }

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

    public void addOvertimeOperations(List<String> operationIds) {
        overtimeOperationsSet.addAll(operationIds);
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

    public void clearTempData() {
        operationsSet.clear();
        overtimeOperationsSet.clear();
    }

    public PreAggregates copy() {
        PreAggregates preAggregates = new PreAggregates();
        preAggregates.setAggregationTime(aggregationTime);
        preAggregates.setServiceId(serviceId);
        preAggregates.getOperationsSet().addAll(operationsSet);
        preAggregates.setRunningOperationsCount(runningOperationsCount);
        preAggregates.getOvertimeOperationsSet().addAll(overtimeOperationsSet);
        preAggregates.setErrorOperationsCount(errorOperationsCount);
        preAggregates.setSuccessOperationsCount(successOperationsCount);
        preAggregates.setOperationTimeLimit(operationTimeLimit);
        preAggregates.setSlidingWindow(slidingWindow);
        preAggregates.setPreAggregationSize(preAggregationSize);
        return preAggregates;
    }
}