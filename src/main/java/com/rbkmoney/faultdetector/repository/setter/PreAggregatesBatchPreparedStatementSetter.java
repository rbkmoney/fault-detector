package com.rbkmoney.faultdetector.repository.setter;

import com.rbkmoney.faultdetector.data.PreAggregates;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
public class PreAggregatesBatchPreparedStatementSetter implements BatchPreparedStatementSetter {

    private final List<PreAggregates> batch;

    @Override
    public void setValues(PreparedStatement ps, int i) throws SQLException {
        PreAggregates preAggregates = batch.get(i);
        int l = 1;
        ps.setDate(l++, new Date(preAggregates.getAggregationTime()));
        ps.setString(l++, preAggregates.getServiceId());
        ps.setInt(l++, preAggregates.getOperationsCount());
        ps.setInt(l++, preAggregates.getRunningOperationsCount());
        ps.setInt(l++, preAggregates.getOvertimeOperationsCount());
        ps.setInt(l++, preAggregates.getErrorOperationsCount());
        ps.setInt(l++, preAggregates.getSuccessOperationsCount());
        ps.setInt(l++, preAggregates.getOperationTimeLimit());
        ps.setInt(l++, preAggregates.getSlidingWindow());
        ps.setInt(l++, preAggregates.getPreAggregationSize());
    }

    @Override
    public int getBatchSize() {
        return batch.size();
    }
}
