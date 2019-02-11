package com.rbkmoney.faultdetector.data;

import com.rbkmoney.damsel.fault_detector.Error;
import com.rbkmoney.damsel.fault_detector.Finish;
import com.rbkmoney.damsel.fault_detector.Operation;
import com.rbkmoney.damsel.fault_detector.Start;

public final class FaultDetectorData {

    public static Operation getStartOperation(String startTime) {
        Operation operation = new Operation();
        Start start = new Start();
        start.setTimeStart(startTime);
        operation.setStart(start);
        return operation;
    }

    public static Operation getFinishOperation(String endTime) {
        Operation operation = new Operation();
        Finish finish = new Finish();
        finish.setTimeEnd(endTime);
        operation.setFinish(finish);
        return operation;
    }

    public static Operation getErrorOperation(String endTime) {
        Operation operation = new Operation();
        Error error = new Error();
        error.setTimeEnd(endTime);
        operation.setError(error);
        return operation;
    }

    private FaultDetectorData() {}

}
