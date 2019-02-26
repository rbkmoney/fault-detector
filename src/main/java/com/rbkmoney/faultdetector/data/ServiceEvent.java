package com.rbkmoney.faultdetector.data;

import lombok.Data;

@Data
public class ServiceEvent {

    private String serviceId;

    private String operationId;

    private long startTime;

    private long endTime;

    private boolean error;

}
