package com.rbkmoney.faultdetector.utils;

import com.rbkmoney.faultdetector.data.PreAggregates;
import com.rbkmoney.faultdetector.data.ServiceSettings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransformDataUtils {

    public static Deque<PreAggregates> getPreAggregatesDequeBySettings(Deque<PreAggregates> preAggregatesDeque,
                                                                       ServiceSettings settings) {
        int preAggregationSize = settings.getPreAggregationSize();
        if (preAggregationSize == 1) {
            return preAggregatesDeque;
        }

        Deque<PreAggregates> newPreAggregatesDeque = new ConcurrentLinkedDeque<>();

        Iterator<PreAggregates> preAggregatesIterator = preAggregatesDeque.iterator();
        while (preAggregatesIterator.hasNext()) {
            PreAggregates newPreAggregates = preAggregatesIterator.next().copy();
            for (int i = 0; i < preAggregationSize - 1; i++) {
                if (preAggregatesIterator.hasNext()) {
                    PreAggregates nextPreAggregates = preAggregatesIterator.next();
                    newPreAggregates.setOperationsCount(
                            newPreAggregates.getOperationsCount() + nextPreAggregates.getOperationsCount());
                    newPreAggregates.setErrorOperationsCount(
                            newPreAggregates.getErrorOperationsCount() + nextPreAggregates.getErrorOperationsCount());
                    newPreAggregates.setSuccessOperationsCount(
                            newPreAggregates.getSuccessOperationsCount() + nextPreAggregates.getSuccessOperationsCount());
                    newPreAggregates.getOvertimeOperationsSet().addAll(nextPreAggregates.getOvertimeOperationsSet());
                }
            }
            newPreAggregatesDeque.addLast(newPreAggregates);
        }
        return newPreAggregatesDeque;
    }

}
