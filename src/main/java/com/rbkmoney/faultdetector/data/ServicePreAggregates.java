package com.rbkmoney.faultdetector.data;

import lombok.Data;

@Data
public class ServicePreAggregates {

    private String serviceId;

    private Long aggregationTime;

    private int operationsCount;

    private int runningOperationsCount;

    private int overtimeOperationsCount;

    private int errorOperationsCount;

    private int successOperationsCount;

}
