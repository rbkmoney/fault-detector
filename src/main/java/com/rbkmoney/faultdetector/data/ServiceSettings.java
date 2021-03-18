package com.rbkmoney.faultdetector.data;

import lombok.Data;

@Data
public class ServiceSettings {

    private long operationTimeLimit;

    private long slidingWindow;

    private int preAggregationSize;

}
