package dev.vality.faultdetector.data;

import lombok.Data;

@Data
public class ServiceOperation {

    private String serviceId;

    private String operationId;

    private long startTime;

    private long endTime;

    private boolean error;

}
