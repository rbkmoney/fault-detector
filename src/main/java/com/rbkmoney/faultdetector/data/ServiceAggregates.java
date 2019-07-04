package com.rbkmoney.faultdetector.data;

import lombok.Data;

//TODO: сделать рефакторинг, как только подключиться хранилище
@Data
public class ServiceAggregates {

    private String serviceId;

    private long aggregateTime;

    private double failureRate;

    private long operationsCount;

    private long totalOperationsCount;

    private long successOperationsCount;

    private long totalSuccessOperationsCount;

    private long errorOperationsCount;

    private long totalErrorOperationsCount;

    private long overtimeOperationsCount;

    private String overtimeOperationsProgressiveLine;

    private String errorOperationsProgressiveLine;

    private String operationsCountProgressiveLine;

}
