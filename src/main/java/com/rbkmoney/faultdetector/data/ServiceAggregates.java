package com.rbkmoney.faultdetector.data;

import lombok.Data;

//TODO: сделать рефакторинг, как только подключиться хранилище
@Data
public class ServiceAggregates {

    private String serviceId;

    private long aggregateTime;

    private double failureRate;

    private long operationsCount;

    private long runningOperationsCount;

    private long successOperationsCount;

    private long errorOperationsCount;

    private long overtimeOperationsCount;

}
