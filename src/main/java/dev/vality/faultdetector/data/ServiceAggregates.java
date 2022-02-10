package dev.vality.faultdetector.data;

import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

@Data
public class ServiceAggregates {

    private String serviceId;

    private long aggregateTime;

    private double failureRate;

    private AtomicLong operationsCount;

    private AtomicLong runningOperationsCount;

    private AtomicLong successOperationsCount;

    private AtomicLong errorOperationsCount;

    private AtomicLong overtimeOperationsCount;

    private AtomicLong operationsAvgTime;

}
