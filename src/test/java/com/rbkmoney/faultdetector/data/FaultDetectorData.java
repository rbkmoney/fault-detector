package com.rbkmoney.faultdetector.data;

import com.rbkmoney.damsel.fault_detector.*;
import com.rbkmoney.damsel.fault_detector.Error;

public final class FaultDetectorData {

    public static Operation getStartOperation(String operationId, String startTime) {
        Operation operation = new Operation();
        Start start = new Start();
        start.setTimeStart(startTime);
        operation.setOperationId(operationId);
        OperationState state = new OperationState();
        state.setStart(start);
        operation.setState(state);
        return operation;
    }

    public static Operation getFinishOperation(String operationId, String endTime) {
        Operation operation = new Operation();
        Finish finish = new Finish();
        finish.setTimeEnd(endTime);
        operation.setOperationId(operationId);
        OperationState state = new OperationState();
        state.setFinish(finish);
        operation.setState(state);
        return operation;
    }

    public static Operation getErrorOperation(String operationId, String endTime) {
        Operation operation = new Operation();
        Error error = new Error();
        error.setTimeEnd(endTime);
        operation.setOperationId(operationId);
        OperationState state = new OperationState();
        state.setError(error);
        operation.setState(state);
        return operation;
    }

    private FaultDetectorData() {}

}
