package com.rbkmoney.faultdetector.repository.setter;

import com.rbkmoney.faultdetector.data.ServiceAggregates;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
public class AggregatesBatchPreparedStatementSetter implements BatchPreparedStatementSetter {

    private final List<ServiceAggregates> batch;

    @Override
    public void setValues(PreparedStatement ps, int i) throws SQLException {
        ServiceAggregates aggregates = batch.get(i);
        int l = 1;
        ps.setDate(l++, new Date(aggregates.getAggregateTime()));
        ps.setString(l++, aggregates.getServiceId());
        ps.setDouble(l++, aggregates.getFailureRate());
        ps.setLong(l++, aggregates.getOperationsCount());
        ps.setLong(l++, aggregates.getSuccessOperationsCount());
        ps.setLong(l++, aggregates.getErrorOperationsCount());
        ps.setLong(l++, aggregates.getOvertimeOperationsCount());
    }

    @Override
    public int getBatchSize() {
        return batch.size();
    }

}
