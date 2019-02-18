package com.rbkmoney.faultdetector.data;

import lombok.Data;

@Data
public class ProcessedServiceEvent {

    private EventType eventType;

    private double weight;

}
