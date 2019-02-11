package com.rbkmoney.faultdetector.data;

import lombok.Data;

@Data
public class ServiceAvailability {

    private String serviceId;

    private double successRate;

    private double timeoutRate;

}
