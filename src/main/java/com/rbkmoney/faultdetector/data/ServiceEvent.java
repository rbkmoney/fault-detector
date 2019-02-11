package com.rbkmoney.faultdetector.data;

import lombok.Data;

@Data
public class ServiceEvent {

    private String requestId;

    private Long startTime;

    private Long endTime;

    private boolean error;

}
