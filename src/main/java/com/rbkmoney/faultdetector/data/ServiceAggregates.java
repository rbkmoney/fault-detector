package com.rbkmoney.faultdetector.data;

import lombok.Data;

@Data
public class ServiceAggregates {

    private String serviceId;

    private double failureRate;

    private long operationsCount;

    private long successOperationsCount;

    private long errorOperationsCount;

    private long overtimeOperationsCount;

}
