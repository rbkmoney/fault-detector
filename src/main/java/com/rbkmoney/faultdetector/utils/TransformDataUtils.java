package com.rbkmoney.faultdetector.utils;

import com.rbkmoney.faultdetector.data.PreAggregates;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransformDataUtils {

    public static void mergePreAggregates(PreAggregates lastPreAggregates, PreAggregates newPreAggregates) {
        lastPreAggregates.setErrorOperationsCount(
                lastPreAggregates.getErrorOperationsCount() + newPreAggregates.getErrorOperationsCount()
        );
        lastPreAggregates.setSuccessOperationsCount(
                lastPreAggregates.getSuccessOperationsCount() + newPreAggregates.getSuccessOperationsCount()
        );
        lastPreAggregates.addOperations(newPreAggregates.getOperationsSet());
        lastPreAggregates.addOvertimeOperations(newPreAggregates.getOvertimeOperationsSet());
        lastPreAggregates.setRunningOperationsCount(newPreAggregates.getRunningOperationsCount());
        lastPreAggregates.setMerged(true);
    }

}
