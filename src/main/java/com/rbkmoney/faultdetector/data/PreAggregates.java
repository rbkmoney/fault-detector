package com.rbkmoney.faultdetector.data;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Data
public class PreAggregates implements Comparable<PreAggregates> {

    private Long aggregationTime;

    private int operationsCount;

    private int runningOperationsCount;

    private int overtimeOperationsCount;

    private int errorOperationsCount;

    private int successOperationsCount;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PreAggregates)) return false;
        PreAggregates that = (PreAggregates) o;
        return Objects.equals(getAggregationTime(), that.getAggregationTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAggregationTime());
    }

    @Override
    public int compareTo(@NotNull PreAggregates preAggregates) {
        if (preAggregates.getAggregationTime() < aggregationTime) {
            return 1;
        } else if (preAggregates.getAggregationTime() == aggregationTime) {
            return 0;
        } else {
            return -1;
        }
    }
}